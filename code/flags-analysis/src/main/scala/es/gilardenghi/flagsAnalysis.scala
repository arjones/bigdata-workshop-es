package es.gilardenghi

import org.apache.spark.ml.classification.RandomForestClassificationModel
import org.apache.spark.sql.SparkSession

object flagsAnalysis extends DatasetUtil {

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: flagsAnalysis <datasource> <model>
           |  <datasource> CSV dataset to PREDICT flag
           |  <model> path to the model
           |
           |  flagsAnalysis /dataset/flags/flags-user-input.csv /dataset/flags.model
        """.stripMargin)
      System.exit(1)
    }

    val Array(datasource, modelPath) = args

    //  implicit val ss = spark
    implicit val spark = SparkSession.
      builder.
      appName("Flags").
      getOrCreate()

    val df = loadUserInputData(datasource)
    val dfVector = vectorizeInput(df)

    val model = RandomForestClassificationModel.load(modelPath)
    val predictions = model.transform(dfVector)

    import spark.implicits._

    // show results
    println("=" * 30)
    println("Prediction are:")
    predictions.select($"testid", $"prediction").show()

    // save to CSV file
    predictions.select($"testid", $"prediction")
      .write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .option("delimiter",";")
      .save(datasource + ".predictions.csv")
  }
}
