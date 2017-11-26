# Credit Risk Analysis
## Spark Machine Learning (Random Forest) 

```bash
sbt clean assembly

spark-submit \
    --class es.arjon.CreditRiskTrain \
    --master 'local[*]' \
    target/scala-2.11/credit-risk-analysis-assembly-0.1.jar


spark-submit \
    --class es.arjon.CreditRiskAnalysis \
    --master 'local[*]' \
    target/scala-2.11/credit-risk-analysis-assembly-0.1.jar
```

# Acknowledge
The original author of this tutorial is **Carol McDonald <caroljmcdonald@gmail.com>** for the MapR article: [Predicting Loan Credit Risk using Apache Spark Machine Learning Random Forests](https://mapr.com/blog/predicting-loan-credit-risk-using-apache-spark-machine-learning-random-forests/)
