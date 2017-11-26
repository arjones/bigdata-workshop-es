package es.arjon

import org.apache.spark.ml.classification.RandomForestClassificationModel
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession

object CreditRiskAnalysis extends DatasetUtil {

  def main(args: Array[String]): Unit = {
    implicit val spark = SparkSession.
      builder.
      appName("CreditRisk").
      getOrCreate()

    import spark.implicits._
    val df = loadData("data/germancredit-new.csv")

    val featureCols = Array("balance", "duration", "history", "purpose", "amount",
      "savings", "employment", "instPercent", "sexMarried", "guarantors",
      "residenceDuration", "assets", "age", "concCredit", "apartment",
      "credits", "occupation", "dependents", "hasPhone", "foreign")

    val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    val df2 = assembler.transform(df)
    //    df2.select($"features").show(false)

    val labelIndexer = new StringIndexer().setInputCol("creditability").setOutputCol("label")
    val df3 = labelIndexer.fit(df2).transform(df2)
    //    df3.select($"features", $"label", $"creditability").show(30, false)

    val model = RandomForestClassificationModel.load("data/credit.model")
    val predictions = model.transform(df3)
    predictions.select($"label", $"creditability", $"amount").show(false)
  }


}
