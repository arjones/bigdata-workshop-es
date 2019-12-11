package es.arjon

import java.util.Properties

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.SaveMode



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
    //option("startingOffsets", "earliest").
    load()
  

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
    select(from_json($"value".cast("string"), schema, jsonOptions).as("content"))

  stocksJson.printSchema

  val stocks = stocksJson.select($"content.*")

  stocks.printSchema

  // Write to Parquet
  val query = stocks.
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
    trigger(Trigger.ProcessingTime("30 seconds")).
    start()
  query.awaitTermination()

  // AverageStocksToPostgres.process(spark, stocks)

  // Using as an ordinary DF
  // val avgPricing = stocks.
  //   groupBy($"symbol").
  //   agg(avg($"price").as("avg_price"))


  // avgPricing.printSchema

  // Start running the query that prints the running results to the console
  // val query = avgPricing.writeStream.
  //   outputMode(OutputMode.Complete).
  //   format("console").
  //   trigger(Trigger.ProcessingTime("10 seconds")).
  //   start()
  // query.awaitTermination()

  // // Have all the aggregates in an in-memory table
  // val query = avgPricing
  //    .writeStream
  //    .queryName("avgPricing")    // this query name will be the table name
  //    .outputMode("complete")
  //    .format("memory")
  //    .trigger(Trigger.ProcessingTime("10 seconds"))
  //    .start()
  
  // while (true) {
  //   Thread.sleep(10 * 1000)     
  //   // interactively query in-memory table
  //   spark.sql("select * from avgPricing").show()
  //   //println(query.lastProgress)
  // }

  // query.awaitTermination()
}


object AverageStocksToPostgres {

  def process(spark: SparkSession, stocks: DataFrame): Unit = {

  import org.apache.spark.sql.functions._
  import spark.implicits._ 
  
  val avgPricing = stocks.
    withWatermark("timestamp", "60 seconds").
    groupBy( window($"timestamp", "30 seconds"),
      $"symbol").
    agg(avg($"price").as("avg_price"))


  avgPricing.printSchema

  val connectionProperties = new Properties()
  connectionProperties.put("user", "workshop")
  connectionProperties.put("password", "w0rkzh0p")
  connectionProperties.put("driver", "org.postgresql.Driver")
  

  val winToString = udf{(window:GenericRowWithSchema) => window.mkString("-")}

  val processAvgTickers = avgPricing.
    withColumn("window", winToString($"window")).
    writeStream.
    foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        batchDF.write.mode(SaveMode.Append).jdbc(s"jdbc:postgresql://postgres:5432/workshop", "workshop.test_streaming_inserts_avg_price", connectionProperties)
    }.
    trigger(Trigger.ProcessingTime("10 seconds")).
    start()

  processAvgTickers.awaitTermination()    

  }
}