package es.openweather

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{OutputMode, ProcessingTime}
import org.apache.spark.sql.types._

object WeatherConsumer extends App {
  if (args.length < 2) {
    System.err.println(
      s"""
         |Usage: WeatherConsumer <brokers> <topics>
         |  <brokers> is a list of one or more Kafka brokers
         |  <topics> is a list of one or more kafka topics to consume from
         |
         |  StreamingETL kafka:9092 stocks
        """.stripMargin)
    System.exit(1)
  }
  val Array(brokers, topics) = args
  //  val brokers = "kafka:9092"
  //  val topics = "OpenWeather"
  val spark = SparkSession.builder.appName("Weather:Streaming").getOrCreate()
  val jsons = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", brokers)
    .option("subscribe", topics)
    .load()
  jsons.printSchema

  val schema = StructType(Seq(
    StructField("coord", new StructType().add("lon", FloatType).add("lat", FloatType), nullable = false),
    StructField("weather", new StructType().add("id", LongType).add("main", StringType), nullable = false),
    StructField("main", new StructType()
      .add("temp", FloatType)
      .add("pressure", FloatType)
      .add("humidity", IntegerType)
      .add("temp_max", FloatType)
      .add("temp_min", FloatType),
      nullable = false),
    StructField("visibility", IntegerType, nullable = false),
    StructField("dt", LongType, nullable = false),
    StructField("sys", new StructType().add("sunrise", LongType).add("sunset", LongType), nullable = false)
  ))

  import org.apache.spark.sql.functions._
  import spark.implicits._

  val jsonOptions = Map("timestampFormat" -> "yyyy-MM-dd'T'HH:mm'Z'")

  val weatherJSON = jsons.select(from_json($"value".cast("string"), schema, jsonOptions).as("values"))
  weatherJSON.printSchema

  val weather = weatherJSON.select($"values.*")

  weather.printSchema
}
