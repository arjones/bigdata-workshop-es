package es.arjon

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import scala.util.Try

case class Stock(name: String,
                 dateTime: String,
                 open: Double,
                 high: Double,
                 low: Double,
                 close: Double)

object Stock {
  def fromCSV(symbol: String, line: String): Option[Stock] = {
    val v = line.split(",")

    try {
      Some(
        Stock(
          symbol,
          dateTime = v(0),
          open = v(1).toDouble,
          high = v(2).toDouble,
          low = v(3).toDouble,
          close = v(4).toDouble
        )
      )

    } catch {
      case ex: Exception => {
        println(s"Failed to process $symbol, with input $line, with ${ex.toString}")
        None
      }
    }

  }
}


object RunAll {
  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      System.err.println(
        s"""
           |Usage: RunAll <dataset folder> <lookup file> <output folder>
           |  <dataset folder> folder where stocks data is located
           |  <lookup file> file containing lookup information
           |  <output folder> folder to write parquet data
           |
           |RunAll /dataset/stocks-small /dataset/yahoo-symbols-201709.csv /dataset/output.parquet
        """.stripMargin)
      System.exit(1)
    }

    val Array(stocksFolder, lookupSymbol, outputFolder) = args


    val spark = SparkSession.
      builder.
      appName("Stocks:ETL").
      getOrCreate()

    val stocksDS = ReadStockCSV.process(spark, stocksFolder)
    val lookup = ReadSymbolLookup.process(spark, lookupSymbol)

    // For implicit conversions like converting RDDs to DataFrames
    import org.apache.spark.sql.functions._
    import spark.implicits._

    val ds = stocksDS.
      withColumn("full_date", unix_timestamp($"dateTime", "yyyy-MM-dd").cast("timestamp")).
      filter("full_date >= \"2017-09-01\"").
      withColumn("year", year($"full_date")).
      withColumn("month", month($"full_date")).
      withColumn("day", dayofmonth($"full_date")).
      drop($"dateTime").
      withColumnRenamed("name", "symbol").
      join(lookup, Seq("symbol"))

    // https://weishungchung.com/2016/08/21/spark-analyzing-stock-price/
    val movingAverageWindow20 = Window.partitionBy($"symbol").orderBy("full_date").rowsBetween(-20, 0)
    val movingAverageWindow50 = Window.partitionBy($"symbol").orderBy("full_date").rowsBetween(-50, 0)
    val movingAverageWindow100 = Window.partitionBy($"symbol").orderBy("full_date").rowsBetween(-100, 0)

    // Calculate the moving average
    val stocksMA = ds.
      withColumn("ma20", avg($"close").over(movingAverageWindow20)).
      withColumn("ma50", avg($"close").over(movingAverageWindow50)).
      withColumn("ma100", avg($"close").over(movingAverageWindow100))

    stocksMA.show(100)

    DatasetToParquet.process(spark, stocksMA, outputFolder)

    DatasetToPostgres.process(spark, stocksMA)

    spark.stop()
  }
}

object ReadStockCSV {
  def process(spark: SparkSession, originFolder: String) = {

    // Using SparkContext to use RDD
    val sc = spark.sparkContext
    val files = sc.wholeTextFiles(originFolder, minPartitions = 40)

    val stocks = files.map { case (filename, content) =>
      val symbol = new java.io.File(filename).
        getName.
        split('.')(0).
        toUpperCase

      content.split("\n").flatMap { line =>
        Stock.fromCSV(symbol, line)
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
      option("inferSchema", true).
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
