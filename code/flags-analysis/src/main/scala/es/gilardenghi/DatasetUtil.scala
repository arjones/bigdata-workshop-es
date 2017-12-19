package es.gilardenghi

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, SparkSession}

trait DatasetUtil {

  // when using console add this
  // implicit val ss = spark
  def loadTrainData(csv: String)(implicit spark: SparkSession) = {
    import org.apache.spark.sql.types._

    // Dataset: https://archive.ics.uci.edu/ml/datasets/Flags
    // This data file contains details of various nations and their flags.

    // With this data you can try things like predicting the religion of a country from its size and the colours in its flag.
    // religion: 0=Catholic, 1=Other Christian, 2=Muslim, 3=Buddhist, 4=Hindu, 5=Ethnic, 6=Marxist, 7=Others

    val schema = StructType(Seq(
      StructField("landmass", DoubleType, nullable = false),
      StructField("zone", DoubleType, nullable = false),
      StructField("area", DoubleType, nullable = false),
      StructField("population", DoubleType, nullable = false),
      StructField("language", DoubleType, nullable = false),
      StructField("religion", DoubleType, nullable = false),
      StructField("bars", DoubleType, nullable = false),
      StructField("stripes", DoubleType, nullable = false),
      StructField("colours", DoubleType, nullable = false),
      StructField("red", DoubleType, nullable = false),
      StructField("green", DoubleType, nullable = false),
      StructField("blue", DoubleType, nullable = false),
      StructField("gold", DoubleType, nullable = false),
      StructField("white", DoubleType, nullable = false),
      StructField("black", DoubleType, nullable = false),
      StructField("orange", DoubleType, nullable = false),
      StructField("mainhue", DoubleType, nullable = false),
      StructField("circles", DoubleType, nullable = false),
      StructField("crosses", DoubleType, nullable = false),
      StructField("saltires", DoubleType, nullable = false),
      StructField("quarters", DoubleType, nullable = false),
      StructField("sunstars", DoubleType, nullable = false),
      StructField("crescent", DoubleType, nullable = false),
      StructField("triangle", DoubleType, nullable = false),
      StructField("icon", DoubleType, nullable = false),
      StructField("animate", DoubleType, nullable = false),
      StructField("text", DoubleType, nullable = false),
      StructField("topleft", DoubleType, nullable = false),
      StructField("botright", DoubleType, nullable = false)
    ))

    spark.read.
      option("header", false).
      schema(schema).
      csv(csv)
  }

  // predicting the religion of a country
  def loadUserInputData(csv: String)(implicit spark: SparkSession) = {
    import org.apache.spark.sql.types._
    val schema = StructType(Seq(
      StructField("landmass", DoubleType, nullable = false),
      StructField("zone", DoubleType, nullable = false),
      StructField("area", DoubleType, nullable = false),
      StructField("population", DoubleType, nullable = false),
      StructField("language", DoubleType, nullable = false),
      StructField("testId", StringType, nullable = false), // TEST ID to identify the PREDICTED ANSWER
      //StructField("religion", DoubleType, nullable = false),
      StructField("bars", DoubleType, nullable = false),
      StructField("stripes", DoubleType, nullable = false),
      StructField("colours", DoubleType, nullable = false),
      StructField("red", DoubleType, nullable = false),
      StructField("green", DoubleType, nullable = false),
      StructField("blue", DoubleType, nullable = false),
      StructField("gold", DoubleType, nullable = false),
      StructField("white", DoubleType, nullable = false),
      StructField("black", DoubleType, nullable = false),
      StructField("orange", DoubleType, nullable = false),
      StructField("mainhue", DoubleType, nullable = false),
      StructField("circles", DoubleType, nullable = false),
      StructField("crosses", DoubleType, nullable = false),
      StructField("saltires", DoubleType, nullable = false),
      StructField("quarters", DoubleType, nullable = false),
      StructField("sunstars", DoubleType, nullable = false),
      StructField("crescent", DoubleType, nullable = false),
      StructField("triangle", DoubleType, nullable = false),
      StructField("icon", DoubleType, nullable = false),
      StructField("animate", DoubleType, nullable = false),
      StructField("text", DoubleType, nullable = false),
      StructField("topleft", DoubleType, nullable = false),
      StructField("botright", DoubleType, nullable = false)
    ))

    spark.read.
      option("header", false).
      schema(schema).
      csv(csv)
  }

  def vectorizeInput(df: DataFrame)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._
    val featureCols = Array("landmass", "zone", "area", "population", "language",
      "bars", "stripes", "colours", "red", "green", "blue", "gold", "white", "black",
      "orange", "mainhue", "circles", "crosses", "saltires",
      "quarters", "sunstars", "crescent", "triangle", "icon",
      "animate", "text", "topleft", "botright")

    val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    val out = assembler.transform(df)
    //out.select('features).show(truncate = false)

    out
  }
}
