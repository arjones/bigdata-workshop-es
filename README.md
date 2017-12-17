# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshopde Big Data

## Contenidos
* [Levantar el ambiente](#levantar-ambiente)


## Infrastructura

El workshop simula una instalación de producción utilizando container de Docker.
[docker-compose.yml](docker-compose.yml) contiene las definiciones y configuraciones para esos servicios y sus respectivas UIs:

* Apache Spark: [Spark Master UI](http://localhost:8080) | [Job Progress](http://localhost:4040)
* Apache Kafka:
* Postgres:
* [Superset](http://superset.incubator.apache.org) [Dashboard](http://localhost:8088/)

Los puertos de acceso a cada servicio quedaron los defaults. Ej: spark-master:7077, postgres: 5432

## Levantar ambiente
Instalar [Docker >= 17.03](https://www.docker.com/community-edition).
Correr el script que levanta el ambiente.
**IMPORTANTE** el script `restart-env.sh` borra cualquier dato que haya sido procesado anteriormente.

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

# Openweather



### Create a jar containing your application and its deps
```bash
$ sbt clean assembly
```
## Iniciar el Productor de Clima en tiempo real para Argentina
Dentro del mismo package tenemos la clase del simulador [WeatherProducer](./code/openweather/src/main/scala/es/openweather/WeatherProducer.scala)

```bash
# Compilar el simulador
cd code/openweather
sbt clean assembly

# Ejecutarlos dentro de un Worker
docker exec -it wksp_worker1_1 bash
cd /app/openweather
java -cp target/scala-2.11/openweather-assembly-0.1.jar \ 
"es.openweather.WeatherProducer" kafka:9092 OpenWeather
```

## Chequear el contenido de Kafka

```bash
docker exec -it wksp_kafka_1 bash

/opt/kafka_2.11-0.10.1.0/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic OpenWeather --from-beginning

# apretar CTRL+C para salir
```

## Iniciar consumidor de mensajes

```bash
docker exec -it wksp_master_1 bash

cd /app/openweater
spark-submit --master 'spark://master:7077' \
  --class "es.openweather.WeatherConsumer" \
  --total-executor-cores 2 \
  target/scala-2.11/openweather-assembly-0.1.jar \
  kafka:9092 OpenWeather
  
```

## Detener el consumidor de mensajes despues de unos minutos corriendo Iniciar el ETL
```bash
docker exec -it wksp_master_1 bash

cd /app/openweater
spark-submit --master 'spark://master:7077'  \
--class "es.openweather.WeatherETL" \
--total-executor-cores 2 \
--driver-class-path ../postgresql-42.1.4.jar \ 
target/scala-2.11/openweather-assembly-0.1.jar 
  
```
## Creando un Dashboard con Superset
* Chequear la ip del container de Superset en docker
  - Ir a la consola y ejecutar: ```bash 
  docker network inspect wksp_default```
  - Buscar la entrada de Superset en el output: Por Ej
    ```bash
    "Name": "wksp_superset_1",
                "EndpointID": "c62165f1cdb7b83aa585fc6405befea4591444750294282a7eda2ea529f0fe05",
                "MacAddress": "02:42:ac:12:00:06",
                "IPv4Address": "172.18.0.6/16",
                "IPv6Address": ""```
  - Utilizar el IP mencionado en espacio anterior para acceder a Superset
* Acceder a http://172.18.0.6:8088/, user: `admin`, pass: `superset`.
* Agregar el database (Sources > Databases):
  - Database: `workshop`
  - SQLAlchemy URI: `postgresql://workshop:w0rkzh0p@postgres/workshop`
  - OK
* Agregar tabla (Sources > Tables) :
  - Database: `workshop`
  - Table Name: `weather`
* Create Slices & Dashboard [official docs](https://superset.incubator.apache.org/tutorial.html#creating-a-slice-and-dashboard)

![Superset Dashboard Example](superset.png)


## Sobre
Ignacio Cicero 
Jorge Goldman
Juan Francisco Dalceggio
