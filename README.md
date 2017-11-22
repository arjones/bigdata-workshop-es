# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshopde Big Data

## Elementos
* Apache Spark
* Apache Zeppelin
* Apache Kafka
* Postgres
* [Superset](http://superset.incubator.apache.org) (Dashboard)

## Codigo
* [Analisis de acciones de EEUU](code/us-stock-analysis) (US Stocks)

## Levantar ambiente
Depende de [docker >= 17.03](https://www.docker.com/community-edition)
```bash
./restart-env.sh

# Access Spark-Master and run spark-shell
docker exec -it wksp_master_1 bash
root@588acf96a879:/app# spark-shell
```
Probar:
```scala
val file = sc.textFile("/dataset/yahoo-symbols-201709.csv")
file.count
file.take(10).foreach(println)
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

## Submit de un job
```bash
docker exec -it wksp_master_1 bash

cd /app/us-stock-analysis
spark-submit --master 'spark://master:7077' \
  --class "es.arjon.RunAll" \
  --driver-class-path /app/postgresql-42.1.4.jar \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar \
  /dataset/stocks-small /dataset/yahoo-symbols-201709.csv /tmp/output.parquet
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

## Sobre
Gustavo Arjones &copy; 2017  
[arjon.es](http://arjon.es) | [LinkedIn](http://linkedin.com/in/arjones/) | [Twitter](https://twitter.com/arjones)
