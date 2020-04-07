# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshop de Big Data

## Machine Learning Lib
Usando un dataset de [Credito Alemán](https://archive.ics.uci.edu/ml/datasets/Statlog+(German+Credit+Data)) se entrenará un algoritmo de [Clasificación Random Forest](https://spark.apache.org/docs/2.4.4/ml-classification-regression.html#random-forest-classifier) y se buscará predecir el valor `Creditable` que significa **brindar credito**.

## Codigo
* [Analisis de risco de credito](code/credit-risk-analysis) (credit-risk-analysis)

## Realizar el entrenamiento
La clase [CreditRiskTrain.scala](code/credit-risk-analysis/src/main/scala/es/arjon/CreditRiskTrain.scala) hace las transformaciones de los datos de entrada para generar el modelo de Random Forest. También intentamos mejorar el modelo utilizando [CrossValidator](https://spark.apache.org/docs/2.4.4/ml-tuning.html#cross-validation)

```bash
# Compilar el proyecto
cd code/credit-risk-analysis
sbt clean assembly

# Conectarse al SparkMaster y hacer submit del proyecto de Entrenamiento
docker exec -it master bash
cd /app/credit-risk-analysis
spark-submit \
  --class es.arjon.CreditRiskTrain \
  --master 'spark://master:7077' \
  target/scala-2.11/credit-risk-analysis-assembly-0.1.jar \
  /dataset/credit-risk/germancredit.csv \
  /dataset/credit-risk.model

# va tomar 4+ minutos para concluir el entrenamiento
```

Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

# Chequeá el modelo entrenado
```bash
ls -la /dataset/credit-risk.model
```

## Realizando predicciones
El archivo `/dataset/credit-risk/germancredit-user-input.csv` simula entrada de usuarios con sus respectivas que son enviadas al modelo para prediccion.

```bash
spark-submit \
  --class es.arjon.CreditRiskAnalysis \
  --master 'spark://master:7077' \
  target/scala-2.11/credit-risk-analysis-assembly-0.1.jar \
  /dataset/credit-risk/germancredit-user-input.csv \
  /dataset/credit-risk.model
```

Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

### Desafío 🤓
Modificar el codigo para tomar la entrada de **Kafka** y escribir en **Postgres**


## Más información
* [Predicting Loan Credit Risk using Apache Spark Machine Learning Random Forests](https://mapr.com/blog/predicting-loan-credit-risk-using-apache-spark-machine-learning-random-forests/)
* [Original: Analysis of German Credit Data](https://onlinecourses.science.psu.edu/stat857/node/215)

____
Gustavo Arjones &copy; 2017-2020
