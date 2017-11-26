package es.arjon

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, SparkSession}

trait DatasetUtil {

  // when using console add this
  // implicit val ss = spark
  def loadTrainData(csv: String)(implicit spark: SparkSession) = {
    import org.apache.spark.sql.types._

    val schema = StructType(Seq(
      StructField("creditability", StringType, nullable = false),
      StructField("balance", DoubleType, nullable = false),
      StructField("duration", DoubleType, nullable = false),
      StructField("history", DoubleType, nullable = false),
      StructField("purpose", DoubleType, nullable = false),
      StructField("amount", DoubleType, nullable = false),
      StructField("savings", DoubleType, nullable = false),
      StructField("employment", DoubleType, nullable = false),
      StructField("instPercent", DoubleType, nullable = false),
      StructField("sexMarried", DoubleType, nullable = false),
      StructField("guarantors", DoubleType, nullable = false),
      StructField("residenceDuration", DoubleType, nullable = false),
      StructField("assets", DoubleType, nullable = false),
      StructField("age", DoubleType, nullable = false),
      StructField("concCredit", DoubleType, nullable = false),
      StructField("apartment", DoubleType, nullable = false),
      StructField("credits", DoubleType, nullable = false),
      StructField("occupation", DoubleType, nullable = false),
      StructField("dependents", DoubleType, nullable = false),
      StructField("hasPhone", DoubleType, nullable = false),
      StructField("foreign", DoubleType, nullable = false)
    ))

    spark.read.
      option("header", false).
      schema(schema).
      csv(csv)
  }

  def loadUserInputData(csv: String)(implicit spark: SparkSession) = {
    import org.apache.spark.sql.types._
    val schema = StructType(Seq(
      StructField("userId", StringType, nullable = false), // USER ID to identify the PREDICTED ANSWER
      StructField("balance", DoubleType, nullable = false),
      StructField("duration", DoubleType, nullable = false),
      StructField("history", DoubleType, nullable = false),
      StructField("purpose", DoubleType, nullable = false),
      StructField("amount", DoubleType, nullable = false),
      StructField("savings", DoubleType, nullable = false),
      StructField("employment", DoubleType, nullable = false),
      StructField("instPercent", DoubleType, nullable = false),
      StructField("sexMarried", DoubleType, nullable = false),
      StructField("guarantors", DoubleType, nullable = false),
      StructField("residenceDuration", DoubleType, nullable = false),
      StructField("assets", DoubleType, nullable = false),
      StructField("age", DoubleType, nullable = false),
      StructField("concCredit", DoubleType, nullable = false),
      StructField("apartment", DoubleType, nullable = false),
      StructField("credits", DoubleType, nullable = false),
      StructField("occupation", DoubleType, nullable = false),
      StructField("dependents", DoubleType, nullable = false),
      StructField("hasPhone", DoubleType, nullable = false),
      StructField("foreign", DoubleType, nullable = false)
    ))

    spark.read.
      option("header", false).
      schema(schema).
      csv(csv)
  }

  def vectorizeInput(df: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val featureCols = Array("balance", "duration", "history", "purpose", "amount",
      "savings", "employment", "instPercent", "sexMarried", "guarantors",
      "residenceDuration", "assets", "age", "concCredit", "apartment",
      "credits", "occupation", "dependents", "hasPhone", "foreign")

    val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    val out = assembler.transform(df)
    out.select('features).show(truncate = false)

    out
  }
}
