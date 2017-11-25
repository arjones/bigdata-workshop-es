package es.arjon

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{OutputMode, ProcessingTime}
import org.apache.spark.sql.types._


object StreamingETL extends App {
  if (args.length < 2) {
    System.err.println(
      s"""
         |Usage: StreamingETL <brokers> <topics>
         |  <brokers> is a list of one or more Kafka brokers
         |  <topics> is a list of one or more kafka topics to consume from
         |
         |  StreamingETL kafka:9092 stocks
        """.stripMargin)
    System.exit(1)
  }

  val Array(brokers, topics) = args
  val spark = SparkSession.
    builder.
    appName("Stocks:StreamingETL").
    getOrCreate()

  //  val brokers = "kafka:9092"
  //  val topics = "stocks"


  // Create DataSet representing the stream of input lines from kafka
  //  https://databricks.com/blog/2017/04/26/processing-data-in-apache-kafka-with-structured-streaming-in-apache-spark-2-2.html
  val jsons = spark.
    readStream.
    format("kafka").
    option("kafka.bootstrap.servers", brokers).
    option("subscribe", topics).
    load()
  //    option("startingOffsets", "earliest").

  jsons.printSchema

  val schema = StructType(Seq(
    StructField("symbol", StringType, nullable = false),
    StructField("timestamp", TimestampType, nullable = false),
    StructField("price", DoubleType, nullable = false)
  ))

  import org.apache.spark.sql.functions._
  import spark.implicits._

  val jsonOptions = Map("timestampFormat" -> "yyyy-MM-dd'T'HH:mm'Z'")
  val stocksJson = jsons.
    select(from_json($"value".cast("string"), schema, jsonOptions).as("values"))

  stocksJson.printSchema

  val stocks = stocksJson.select($"values.*")

  stocks.printSchema

  // Write to Parquet
  stocks.
    withColumn("year", year($"timestamp")).
    withColumn("month", month($"timestamp")).
    withColumn("day", dayofmonth($"timestamp")).
    withColumn("hour", hour($"timestamp")).
    withColumn("minute", minute($"timestamp")).
    writeStream.
    format("parquet").
    partitionBy("year", "month", "day", "hour", "minute").
    option("startingOffsets", "earliest").
    option("checkpointLocation", "/dataset/checkpoint").
    option("path", "/dataset/streaming.parquet").
    trigger(ProcessingTime("30 seconds")).
    start()

  // There is no JDBC sink for now!
  // https://databricks.com/blog/2017/04/04/real-time-end-to-end-integration-with-apache-kafka-in-apache-sparks-structured-streaming.html
  //

  // Using as an ordinary DF
  val avgPricing = stocks.
    groupBy($"symbol").
    agg(avg($"price").as("avg_price"))

  // Start running the query that prints the running results to the console
  val query = avgPricing.writeStream.
    outputMode(OutputMode.Complete).
    format("console").
    trigger(ProcessingTime("10 seconds")).
    start()

  // Have all the aggregates in an in-memory table
  //  avgPricing
  //    .writeStream
  //    .queryName("avgPricing")    // this query name will be the table name
  //    .outputMode("complete")
  //    .format("memory")
  //    .start()
  //
  //  spark.sql("select * from avgPricing").show()   // interactively query in-memory table

  query.awaitTermination()
}
