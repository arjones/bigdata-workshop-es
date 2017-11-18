package es.arjon

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import scala.util.Try

case class Stock(name: String, dateTime: String, open: Double, high: Double, low: Double, close: Double)

object Stock {
  def fromCSV(name: String, line: String): Option[Stock] = {
    val v = line.split(",")

    Try {
      Stock(
        name,
        dateTime = v(0),
        open = v(1).toDouble,
        high = v(2).toDouble,
        low = v(3).toDouble,
        close = v(4).toDouble
      )
    }.toOption

  }
}

object RunAll {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.
      builder.
      appName("Stocks:ETL").
      getOrCreate()

    val stocksDS = ReadStockCSV.process(spark, "dataset/stocks-small")
    val lookup = ReadSymbolLookup.process(spark, "dataset/yahoo-symbols-201709.csv")

    // For implicit conversions like converting RDDs to DataFrames
    import org.apache.spark.sql.functions._
    import spark.implicits._

    val ds = stocksDS.
      withColumn("full_date", unix_timestamp($"dateTime", "yyyy-MM-dd").cast("timestamp")).
      filter("full_date >= \"2016-01-01\"").
      withColumn("year", year($"full_date")).
      withColumn("month", month($"full_date")).
      withColumn("day", dayofmonth($"full_date")).
      drop($"dateTime").
      withColumnRenamed("name", "symbol").
      join(lookup, Seq("symbol"))

    ds.show()

    DatasetToParquet.process(spark, ds,
      destinationFolder = "dataset/output.parquet")

    DatasetToPostgres.process(spark, ds)

    spark.stop()
  }
}

object ReadStockCSV {
  def process(spark: SparkSession, originFolder: String) = {

    // Using SparkContext to use RDD
    val sc = spark.sparkContext
    val files = sc.wholeTextFiles(originFolder, minPartitions = 40)

    val stocks = files.map { case (filename, content) =>
      content.split("\n").flatMap { line =>
        val quote = new java.io.File(filename).
          getName.
          replace(".us.txt", "").
          toUpperCase

        Stock.fromCSV(quote, line)
      }
    }.
      flatMap(e => e).
      cache

    import spark.implicits._

    stocks.toDS.as[Stock]
  }
}

object ReadSymbolLookup {
  def process(spark: SparkSession, file: String) = {
    import spark.implicits._
    spark.read.
      option("header", true).
      option("inferSchema", "true").
      csv(file).
      select($"Ticker", $"Category Name").
      withColumnRenamed("Ticker", "symbol").
      withColumnRenamed("Category Name", "category")
  }
}

object DatasetToParquet {
  def process(spark: SparkSession, df: DataFrame, destinationFolder: String): Unit = {
    // https://stackoverflow.com/questions/43731679/how-to-save-a-partitioned-parquet-file-in-spark-2-1
    df.
      write.
      mode("overwrite").
      partitionBy("year", "month", "day").
      parquet(destinationFolder)
  }
}

object DatasetToPostgres {

  def process(spark: SparkSession, df: DataFrame): Unit = {
    // Write to Postgres
    val connectionProperties = new java.util.Properties
    connectionProperties.put("user", "workshop")
    connectionProperties.put("password", "w0rkzh0p")
    val jdbcUrl = s"jdbc:postgresql://postgres:5432/workshop"

    df.
      drop("year", "month", "day"). // drop unused columns
      write.
      mode(SaveMode.Append).
      jdbc(jdbcUrl, "stocks", connectionProperties)

  }
}

// TODO: Read compressed
// option("codec", "org.apache.hadoop.io.compress.GzipCodec").
