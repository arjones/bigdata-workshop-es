# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshop de Big Data

## Batch Processing

## Codigo
* [Analisis de acciones de EEUU](code/us-stock-analysis) (US Stocks)

## Compilar el codigo
Compilar y empaquetar el codigo para deploy en el cluster

```bash
cd code/us-stock-analysis
sbt clean assembly
```

## Submit de un job
Conectarse al Spark-Master y hacer submit del programa

```bash
docker exec -it wksp_master_1 bash

cd /app/us-stock-analysis
spark-submit --master 'spark://master:7077' \
  --class "es.arjon.RunAll" \
  --driver-class-path /app/postgresql-42.1.4.jar \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  /dataset/stocks-small /dataset/yahoo-symbols-201709.csv /dataset/output.parquet
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI


## Usando Spark-SQL
Usando SparkSQL para acceder a los datos en Parquet y hacer analysis interactiva.

```bash
docker exec -it wksp_master_1 bash
spark-shell
```

```scala
import spark.implicits._
val df = spark.read.parquet("/dataset/output.parquet")
df.show

df.createOrReplaceTempView("stocks")

// Usando particiones
val highestClosingPrice = spark.sql("SELECT symbol, MAX(close) AS price FROM stocks WHERE year=2017 AND month=9 GROUP BY symbol")
highestClosingPrice.show
highestClosingPrice.explain

// No usando particiones
val highestClosingPrice = spark.sql("SELECT symbol, MAX(close) AS price FROM stocks WHERE full_date > '2017-09-01' GROUP BY symbol")
highestClosingPrice.explain
highestClosingPrice.show
```

## Creando un Dashboard con Superset

* Acceder a http://localhost:8088/, user: `admin`, pass: `superset`.
* Agregar el database (Sources > Databases):
  - Database: `Workshop`
  - SQLAlchemy URI: `postgresql://workshop:w0rkzh0p@postgres/workshop`
  - OK
* Agregar tabla (Sources > Tables) :
  - Database: `workshop`
  - Table Name: `stocks`
* Create Slices & Dashboard [official docs](https://superset.incubator.apache.org/tutorial.html#creating-a-slice-and-dashboard)

![Superset Dashboard Example](superset.png)

## Siga leyendo
* [Structured Streaming Processing](README-streaming.md)


____
Gustavo Arjones &copy; 2017
