package es.openweather

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{ OutputMode, ProcessingTime }
import org.apache.spark.sql.types._
import java.util.Calendar

object WeatherETL {

  def main(args: Array[String]): Unit = {
    
    val spark = SparkSession.builder.appName("Weather:ETL").getOrCreate()
    val parquetFileDF = spark.read.parquet("/dataset/streaming-openweahter.parquet")

    parquetFileDF.createOrReplaceTempView("parquetFile")

    val DF = spark.sql("SELECT * FROM parquetFile")

    DF.show

  }

}