# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshop de Big Data

## Batch Processing

## Codigo
* [Analisis de acciones de EEUU](code/scala/us-stock-analysis) (Scala)
* [Analisis de acciones de EEUU](code/python/us-stock-analysis) (Python)
* [us-stock-analysis Jupyter Notebook](jupyter/notebook/batch_etl_steps.ipynb) (Python)

## Compilar el codigo
Compilar y empaquetar el codigo para deploy en el cluster

```bash
cd code/us-stock-analysis
sbt clean assembly
```

## Submit de un job
Conectarse al Spark-Master y hacer submit del programa

```bash
docker exec -it master bash

cd /app/us-stock-analysis
spark-submit --master 'spark://master:7077' \
  --class "es.arjon.RunAll" \
  --driver-class-path /app/postgresql-42.1.4.jar \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  /dataset/stocks-small /dataset/yahoo-symbols-201709.csv /dataset/output.parquet
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

Verificar el resultado del job en la carpeta `/dataset/output.parquet`:

```bash
# Desde la maquina host
$ tree ~/bigdata-workshop-es/dataset/output.parquet/
```

## Usando Spark-SQL
Usando SparkSQL para acceder a los datos en Parquet y hacer analysis interactiva.

```bash
docker exec -it master bash
spark-shell
```

```scala
// reduce log noise
sc.setLogLevel("ERROR")

import spark.implicits._
val df = spark.read.parquet("/dataset/output.parquet")
df.show
df.printSchema

df.createOrReplaceTempView("stocks")

// No usando particiones
val badHighestClosingPrice = spark.sql("SELECT symbol, MAX(close) AS price FROM stocks WHERE full_date >= '2017-09-01' AND full_date < '2017-10-01' GROUP BY symbol")
badHighestClosingPrice.explain
badHighestClosingPrice.show

// Optimizando con particiones
val highestClosingPrice = spark.sql("SELECT symbol, MAX(close) AS price FROM stocks WHERE year=2017 AND month=9 GROUP BY symbol")
highestClosingPrice.explain
highestClosingPrice.show
```

## Ver los datos en Postgres
El batch job también escribe una tabla `stocks` en Postgres que se puede acceder:

```
# abrir otra consola

docker exec -it postgres bash

psql -U workshop workshop
workshop=# \d
...
...

workshop=# SELECT * FROM stocks LIMIT 10;
```

## Creando un Dashboard con Superset

* [Como configurar Superset](./README-superset.md)
* [Sitio Oficial Superset](https://superset.apache.org/)


## Siga leyendo
* [Structured Streaming Processing](README-streaming.md)


____
Gustavo Arjones &copy; 2017-2020
