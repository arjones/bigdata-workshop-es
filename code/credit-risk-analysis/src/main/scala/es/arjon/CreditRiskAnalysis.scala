package es.arjon

import java.util.logging.{Level, Logger}

import org.apache.spark.ml.classification._
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.ml.{Pipeline, PipelineModel, PipelineStage}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql._

// Heavily inspired on
// https://mapr.com/blog/predicting-loan-credit-risk-using-apache-spark-machine-learning-random-forests/
object CreditRiskAnalysis {
  val logger = Logger.getLogger("CreditRiskAnalysis")

  // when using console add this
  // implicit val ss = spark
  def loadData(csv: String)(implicit spark: SparkSession) = {
    import org.apache.spark.sql.types._

    val schema = StructType(Seq(
      StructField("creditability", DoubleType, nullable = false),
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


  def main(args: Array[String]) {
    implicit val spark = SparkSession.
      builder.
      appName("CreditRisk").
      getOrCreate()

    import spark.implicits._

    val creditDF = loadData("data/germancredit.csv")
    creditDF.createOrReplaceTempView("credit")
    creditDF.printSchema
    creditDF.show

    spark.sql("SELECT creditability, avg(balance) as avg_balance, avg(amount) as avg_amount, avg(duration) as avg_duration  FROM credit GROUP BY creditability").show

    creditDF.describe("balance").show
    creditDF.groupBy("creditability").avg("balance").show

    val featureCols = Array("balance", "duration", "history", "purpose", "amount",
      "savings", "employment", "instPercent", "sexMarried", "guarantors",
      "residenceDuration", "assets", "age", "concCredit", "apartment",
      "credits", "occupation", "dependents", "hasPhone", "foreign")

    val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    val df2 = assembler.transform(creditDF)
    df2.select($"features").show(false)

    val labelIndexer = new StringIndexer().setInputCol("creditability").setOutputCol("label")
    val df3 = labelIndexer.fit(df2).transform(df2)
    df3.select($"features", $"label").show(30, false)

    val splitSeed = 5043
    val Array(trainingData, testData) = df3.randomSplit(Array(0.7, 0.3), splitSeed)

    val classifier = new RandomForestClassifier().
      setImpurity("gini").
      setMaxDepth(3).
      setNumTrees(20).
      setFeatureSubsetStrategy("auto").
      setSeed(5043)

    val model = classifier.fit(trainingData)
    println(model.toDebugString)

    println("=" * 30)
    println("Before pipeline fitting\n")
    val predictions = model.transform(testData)

    val evaluator = new BinaryClassificationEvaluator().setLabelCol("label")
    val accuracy = evaluator.evaluate(predictions)
    println(f"Accuracy: $accuracy%2.3f")
    logger.warning(f"Accuracy: $accuracy%2.3f")
    printPredictionMetrics(predictions)


    // Let's try to do better
    val paramGrid = new ParamGridBuilder().
      addGrid(classifier.maxBins, Array(20, 40)).
      addGrid(classifier.maxDepth, Array(2, 10)).
      addGrid(classifier.numTrees, Array(10, 60)).
      addGrid(classifier.impurity, Array("entropy", "gini")).
      build()

    val steps: Array[PipelineStage] = Array(classifier)
    val pipeline = new Pipeline().setStages(steps)

    val cv = new CrossValidator().
      setEstimator(pipeline).
      setEvaluator(evaluator).
      setEstimatorParamMaps(paramGrid).
      setNumFolds(10)

    val pipelineFittedModel = cv.fit(trainingData)

    val predictions2 = pipelineFittedModel.transform(testData)
    val accuracy2 = evaluator.evaluate(predictions2)
    println("=" * 30)
    println("AFTER pipeline fitting\n")
    println(f"Accuracy: $accuracy2%2.3f")

    val bestModel = pipelineFittedModel.bestModel.asInstanceOf[PipelineModel].stages(0)
    val params = bestModel.extractParamMap

    println(
      s"""
         |The best model found was:
         |${bestModel}
         |
        |Using params:
         |${params}
         |
      """.stripMargin)

    printPredictionMetrics(predictions2)


    // Save the model to latter use
    model.write.overwrite().save("data/credit.model")

    // load it again
    // val sameModel = RandomForestClassificationModel.load("data/credit.model")

  }

  def printPredictionMetrics(predictions: DataFrame)(implicit spark: SparkSession) {
    // Extract PREDICTED and CORRECT (label) values
    import spark.implicits._
    val predictionAndObservations = predictions.select('prediction, 'label)
    val rdd = predictionAndObservations.rdd.map(r => (r.getDouble(0), r.getDouble(1)))

    // Calculate the Quality Metrics
    val rm = new RegressionMetrics(rdd)
    val msg =
      s""""
         |MSE:           ${rm.meanSquaredError}
         |MAE:           ${rm.meanAbsoluteError}
         |RMSE Squared:  ${rm.rootMeanSquaredError}
         |R Squared:     ${rm.r2}
         |Exp. Variance: ${rm.explainedVariance}
         |
      """.stripMargin

    println(msg)
    logger.log(Level.WARNING, msg)
  }
}

