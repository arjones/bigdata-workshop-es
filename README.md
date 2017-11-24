# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshopde Big Data

## Contenidos
* [Levantar el ambiente](#levantar-ambiente)
* [Batch Processing](README-batch.md)
* [Structured Streaming Processing](README-streaming.md)

## Infrastructura

El workshop simula una instalación de producción utilizando container de Docker.
[docker-compose.yml](docker-compose.yml) contiene las definiciones y configuraciones para esos servicios y sus respectivas UIs:

* Apache Spark: [Spark Master UI](http://localhost:8080) | [Job Progress](http://localhost:4040)
* ~~Apache Zeppelin: [UI](http://localhost:3000)~~
* Apache Kafka:
* Postgres:
* [Superset](http://superset.incubator.apache.org) [Dashboard](http://localhost:8088/)

Los puertos de acceso a cada servicio quedaron los defaults. Ej: spark-master:7077, postgres: 5432

## Levantar ambiente
Instalar [Docker >= 17.03](https://www.docker.com/community-edition).
Correr el script que levanta el ambiente.
**IMPORTANTE** el script `restart-env.sh` borra cualquier dado que haya sido procesado anteriormente.

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

# Siga leyendo
* [Batch Processing](README-batch.md)

## Sobre
Gustavo Arjones &copy; 2017  
[arjon.es](http://arjon.es) | [LinkedIn](http://linkedin.com/in/arjones/) | [Twitter](https://twitter.com/arjones)
