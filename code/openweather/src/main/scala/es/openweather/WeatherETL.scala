package es.openweather

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import java.util.Calendar

object WeatherETL {

  def main(args: Array[String]): Unit = {
    
    //execute as
    //spark-submit --master 'spark://master:7077'   --class "es.openweather.WeatherETL"   --total-executor-cores 2 --driver-class-path ../postgresql-42.1.4.jar  target/scala-2.11/openweather-assembly-0.1.jar 
    
    val spark = SparkSession.builder.appName("Weather:ETL").getOrCreate()
    val parquetFileDF = spark.read.parquet("/dataset/streaming-openweahter.parquet")

    parquetFileDF.createOrReplaceTempView("parquetFile")

    val DF = spark.sql("SELECT * FROM parquetFile")

    DatasetToPostgres.process(spark, DF)

  }

}

object DatasetToPostgres {

  def process(spark: SparkSession, df: DataFrame): Unit = {
    // Write to Postgres
    val connectionProperties = new java.util.Properties
    connectionProperties.put("user", "workshop")
    connectionProperties.put("password", "w0rkzh0p")
    connectionProperties.put("driver","org.postgresql.Driver")
    val jdbcUrl = s"jdbc:postgresql://postgres:5432/workshop"

    df.select("id","City","Temp","WindSpeed","Pressure","Humidity","Visibility","Year","Month","Day","Hour","Lat","Lon").
      write.
      mode(SaveMode.Append).
      jdbc(jdbcUrl, "weather", connectionProperties)

  }
}