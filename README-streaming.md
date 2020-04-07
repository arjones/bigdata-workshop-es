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
docker exec -it worker1 bash
cd /app/us-stock-analysis
java -cp target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  "es.arjon.FakeStockPriceGenerator" kafka:9092 stocks
```

## Chequear el contenido de Kafka

```bash
docker exec -it kafka bash

/opt/kafka_2.11-0.10.1.0/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic stocks --from-beginning

# apretar CTRL+C para salir
```

## Submit de un job
Conectarse al Spark-Master y hacer submit del programa

**NOTA:** Utilizar `--total-executor-cores` con la mitad de cores de tu computadora, ej: si tiene 4 cores, utilizar `2`.

```bash
docker exec -it master bash

cd /app/us-stock-analysis
spark-submit --master 'spark://master:7077' \
  --class "es.arjon.StreamingETL" \
  --total-executor-cores 1 \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  kafka:9092 stocks
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

## En otra consola, acceder al dataset de Streaming
```bash
docker exec -it master bash
spark-shell --total-executor-cores 1
```

```scala
import spark.implicits._
val df = spark.read.parquet("/dataset/streaming.parquet")
df.show
```

## Utilizar Spark SQL y el Sink in Memory

En el archivo `StreamingETL.scala` comentar las líneas 71 a la 85 para evitar que se escriba en el archivo de output Parquet y descomentar las líneas de código de 90 al 103.

Compilar la aplicación de nuevo con: 

```bash
sbt assembly
```

Probar y observar el output por consola.

Luego comentar las líneas 98 a 103 y descomentar 106 a 121, compilar y ejecutar probar. Qué diferencia observa?


## Streaming Spark SQL + Insert a Postgres

Comentar las líneas 106 a 121 y descomentar la línea

```scala
AverageStocksToPostgres.process(spark, stocks)
```

En otra tab ingresar al container de Postgres y luego al utilitario de línea de comando `psql`

```bash
docker exec -it postgres bash
psql --host localhost --d workshop --username workshop
```

Crear la tabla para recibir los inserts

```sql

CREATE TABLE test_streaming_inserts_avg_price (
    "window" varchar(128),
    symbol varchar(10),
    avg_price real
);
```


## Más información
* [Structured Streaming in PySpark](https://hackersandslackers.com/structured-streaming-in-pyspark/)
* [Real-time Streaming ETL with Structured Streaming in Apache Spark 2.1](https://databricks.com/blog/2017/01/19/real-time-streaming-etl-structured-streaming-apache-spark-2-1.html)
* [Processing Data in Apache Kafka with Structured Streaming in Apache Spark 2.2](https://databricks.com/blog/2017/04/26/processing-data-in-apache-kafka-with-structured-streaming-in-apache-spark-2-2.html)
* [Real-Time End-to-End Integration with Apache Kafka in Apache Spark’s Structured Streaming](https://databricks.com/blog/2017/04/04/real-time-end-to-end-integration-with-apache-kafka-in-apache-sparks-structured-streaming.html)

# Siga leyendo
* [MLlib](README-ml.md)

____
Gustavo Arjones &copy; 2017-2020
