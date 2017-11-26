# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshop de Big Data

## Structured Streaming Processing
El simulador publica información sobre acciones y sus precios en una cola Kafka que es consumida por Spark.

## Codigo
* [Analisis de acciones de EEUU](code/us-stock-analysis) (US Stocks)

## Iniciar el simulador de acciones
Dentro del mismo package tenemos la clase del simulador [FakeStockPriceGenerator](./code/us-stock-analysis/src/main/scala/es/arjonFakeStockPriceGenerator.scala)

```bash
# Compilar el similador
cd code/us-stock-analysis
sbt clean assembly

# Ejecutarlos dentro de un Worker
docker exec -it wksp_worker1_1 bash
cd /app/us-stock-analysis
java -cp target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  "es.arjon.FakeStockPriceGenerator" kafka:9092 stocks
```

## Chequear el contenido de Kafka

```bash
docker exec -it wksp_kafka_1 bash

/opt/kafka_2.11-0.10.1.0/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic stocks --from-beginning

# apretar CTRL+C para salir
```

## Submit de un job
Conectarse al Spark-Master y hacer submit del programa

```bash
docker exec -it wksp_master_1 bash

cd /app/us-stock-analysis
spark-submit --master 'spark://master:7077' \
  --class "es.arjon.StreamingETL" \
  --total-executor-cores 2 \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  kafka:9092 stocks
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

## En otra consola, acceder al dataset de Streaming
```bash
docker exec -it wksp_master_1 bash
spark-shell --total-executor-cores 2
```

```scala
import spark.implicits._
val df = spark.read.parquet("/dataset/streaming.parquet")
df.show
```

## Más información
* [Real-time Streaming ETL with Structured Streaming in Apache Spark 2.1](https://databricks.com/blog/2017/01/19/real-time-streaming-etl-structured-streaming-apache-spark-2-1.html)
* [Processing Data in Apache Kafka with Structured Streaming in Apache Spark 2.2](https://databricks.com/blog/2017/04/26/processing-data-in-apache-kafka-with-structured-streaming-in-apache-spark-2-2.html)
* [Real-Time End-to-End Integration with Apache Kafka in Apache Spark’s Structured Streaming](https://databricks.com/blog/2017/04/04/real-time-end-to-end-integration-with-apache-kafka-in-apache-sparks-structured-streaming.html)

# Siga leyendo
* [MLlib](README-ml.md)

____
Gustavo Arjones &copy; 2017
