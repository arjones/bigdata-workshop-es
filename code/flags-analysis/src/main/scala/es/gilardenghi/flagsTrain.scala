package es.gilardenghi

import es.gilardenghi.flagsAnalysis.vectorizeInput
import org.apache.spark.ml.classification._
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.ml.{Pipeline, PipelineModel, PipelineStage}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

// Heavily inspired on
// https://mapr.com/blog/predicting-loan-credit-risk-using-apache-spark-machine-learning-random-forests/
object flagsTrain extends DatasetUtil {
  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: flagsTrain <datasource> <model>
           |  <datasource> CSV dataset to learn from
           |  <model> path to save model to
           |
           |  flagsTrain /dataset/flags/flags.csv /dataset/flags.model
        """.stripMargin)
      System.exit(1)
    }

    val Array(datasource, modelPath) = args

    // When using Spark-Shell:
    // implicit val ss = spark
    implicit val spark = SparkSession.
      builder.
      appName("Flags").
      getOrCreate()

    import spark.implicits._

    val flagsDF = loadTrainData(datasource)
    val dfVector = vectorizeInput(flagsDF)

    // Convert Strings into Label Identifiers (Double)
    val labelIndexer = new StringIndexer().setInputCol("religion").setOutputCol("label")

    // Add Label Identifiers field to the DF
    val dfLabeled = labelIndexer.fit(dfVector).transform(dfVector)

    val splitSeed = 5043
    val Array(trainingData, testData) = dfLabeled.randomSplit(Array(0.7, 0.3), splitSeed)

    val classifier = new RandomForestClassifier().
      setImpurity("gini").
      setMaxDepth(3).
      setNumTrees(20).
      setFeatureSubsetStrategy("auto").
      setSeed(5043)

    // Save the model to latter use
    val model = classifier.fit(trainingData)
    model.write.overwrite().save(modelPath)

    // show info
    println("=" * 30)
    println("Prediction are:")
    val predictions = model.transform(testData)
    val evaluator = new BinaryClassificationEvaluator().setLabelCol("label")
    val accuracy = evaluator.evaluate(predictions)

    println(f"Accuracy: $accuracy%2.3f")
    printPredictionMetrics(predictions)
  }

  def printPredictionMetrics(predictions: DataFrame)(implicit spark: SparkSession) {
    // Extract PREDICTED and CORRECT (label) values
    import spark.implicits._
    val predictionAndObservations = predictions.select('prediction, 'label)
    val rdd = predictionAndObservations.rdd.map(r => (r.getDouble(0), r.getDouble(1)))

    // Calculate the Quality Metrics
    val rm = new RegressionMetrics(rdd)
    val msg =
      s"""
         |MSE:           ${rm.meanSquaredError}
         |MAE:           ${rm.meanAbsoluteError}
         |RMSE Squared:  ${rm.rootMeanSquaredError}
         |R Squared:     ${rm.r2}
         |Exp. Variance: ${rm.explainedVariance}
         |
      """.stripMargin

    println(msg)
  }
}
