package es.openweather

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{ OutputMode, ProcessingTime }
import org.apache.spark.sql.types._
import java.util.Calendar

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

  val schema = StructType(Seq(
    StructField("id", LongType, nullable = false),
    StructField("name", StringType, nullable = false),
    StructField("visibility", IntegerType, nullable = false),
    StructField("wind", new StructType().add("speed", FloatType).add("deg", FloatType), nullable = false),
    StructField("dt", LongType, nullable = false),
    StructField("coord", new StructType().add("lon", FloatType).add("lat", FloatType), nullable = false),
    StructField("sys", new StructType().add("sunrise", LongType).add("sunset", LongType), nullable = false),
    StructField("main", new StructType()
      .add("temp", FloatType, nullable = false)
      .add("pressure", FloatType, nullable = false)
      .add("humidity", IntegerType, nullable = false)
      .add("temp_max", FloatType, nullable = false)
      .add("temp_min", FloatType, nullable = false), nullable = false)
  //    ESTO NO ANDA. Me base en https://stackoverflow.com/questions/39485374/how-to-create-schema-array-in-data-frame-with-spark
  //    StructField("weather", ArrayType(StructType(Array(
  //      StructField("id", LongType, nullable = false),
  //      StructField("main", LongType, nullable = false)
  //    ))), nullable = false)
  ))

  import org.apache.spark.sql.functions._
  import spark.implicits._

  val jsonOptions = Map("timestampFormat" -> "yyyy-MM-dd'T'HH:mm'Z'")
  val weatherJSON = jsons.select(from_json($"value".cast("string"), schema, jsonOptions).as("values"))
  val weather = weatherJSON.select($"values.*")
  private val CITIES_FILE = "/dataset/openweather/cityList.csv"
  final case class CitiesCSV(
    ID: String,
    City: String,
    lat: String,
    lon: String,
    countryCode: String)

  val cities = spark.read
    .option("header", "true")
    .option("charset", "UTF8")
    .csv(CITIES_FILE)
    .as[CitiesCSV]
  weather.printSchema

  
  val weatherOutput = weather.
    join(cities, "ID").
    withColumn("id", $"id").
    withColumn("City", $"City").
    withColumn("Lon", $"lon").
    withColumn("Lat", $"lat").
    withColumn("WindSpeed", $"wind.speed").
    withColumn("Temp", $"main.temp").
    withColumn("Pressure", $"main.pressure").
    withColumn("Humidity", $"main.humidity").
    withColumn("Visibility", $"visibility").
    withColumn("full_date", current_timestamp()).
    withColumn("Year", year($"full_date")).
    withColumn("Month", month($"full_date")).
    withColumn("Day", dayofmonth($"full_date")).
    withColumn("Hour", hour($"full_date")).
    drop("full_date")
    
    weatherOutput.toDF.writeStream.
    format("parquet").
    partitionBy("id","Year","Month","Day","Hour").
    option("startingOffsets", "earliest").
    option("checkpointLocation", "/dataset/checkpoint-openweather").
    option("path", "/dataset/streaming-openweahter.parquet").
    trigger(ProcessingTime("40 seconds")).
    start()

  val avgWindSpeed = weather.join(cities, "ID").
    groupBy($"City").
    agg(avg($"wind.speed"))

  val avgTemp = weatherOutput.
    groupBy($"City",$"Hour").
    agg(avg($"Temp"))
    
  val query = avgTemp.writeStream.
    outputMode(OutputMode.Complete).
    format("console").
    trigger(ProcessingTime("10 seconds")).
    start()

  query.awaitTermination()

}
