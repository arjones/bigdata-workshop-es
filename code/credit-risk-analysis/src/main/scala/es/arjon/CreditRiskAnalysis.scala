package es.arjon

import org.apache.spark.ml.classification.RandomForestClassificationModel
import org.apache.spark.sql.SparkSession

object CreditRiskAnalysis extends DatasetUtil {

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: CreditRiskAnalysis <datasource> <model>
           |  <datasource> CSV dataset to PREDICT credit
           |  <model> path to the model
           |
           |  CreditRiskAnalysis /dataset/credit-risk/germancredit-user-input.csv /dataset/credit-risk.model
        """.stripMargin)
      System.exit(1)
    }

    //    val Array(datasource, modelPath) = Array("/dataset/credit-risk/germancredit-user-input.csv",
    //      "/dataset/credit-risk.model")
    val Array(datasource, modelPath) = args

    //    implicit val ss = spark
    implicit val spark = SparkSession.
      builder.
      appName("CreditRisk").
      getOrCreate()

    val df = loadUserInputData(datasource)
    val dfVector = vectorizeInput(df)

    val model = RandomForestClassificationModel.load(modelPath)
    val predictions = model.transform(dfVector)

    import spark.implicits._

    println("=" * 30)
    println("Prediction are:")
    predictions.select($"userId", $"amount", $"prediction").show(false)
  }


}
