# ETL sobre [Open Weather](http://openweathermap.org)
Componentes
- Productor de mensajes que obtiene los datos de la API y los envía a una cola de Kafka. [WeatherProducer](./code/openweather/src/main/scala/es/openweather/WeatherProducer.scala)
- Consumidor en streaming de mensajes de la cola de Kafka y los almacena en Parquet. [WeatherConsumer](./code/openweather/src/main/scala/es/openweather/WeatherConsumer.scala)
- ETL que toma los datos de parquet y los almacena postgres.[WeatherETL](./code/openweather/src/main/scala/es/openweather/WeatherETL.scala)

## Levantar ambiente
- Instalar [Docker >= 17.03](https://www.docker.com/community-edition).
- Correr el script que levanta el ambiente.
- **IMPORTANTE** el script `restart-env.sh` borra cualquier dato que haya sido procesado anteriormente.

```bash
./restart-env.sh
```

Probar:
```scala
val file = sc.textFile("/dataset/openweather/cityList.csv")
file.count
file.take(10).foreach(println)
```
Acceder a http://localhost:8080 y http://localhost:4040 para ver la SPARK-UI

### Crear jar
```bash
cd code/openweather
sbt clean assembly
```

## Iniciar el Productor de Clima en tiempo real para Argentina
```bash
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


# Detener el consumidor de mensajes despues de unos minutos corriendo (apretar CTRL+C para salir) 
```

##  Iniciar el ETL
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
* Acceder a http://localhost:8088/, user: `admin`, pass: `superset`.
* Agregar el database (Sources > Databases):
  - Database: `workshop`
  - SQLAlchemy URI: `postgresql://workshop:w0rkzh0p@postgres/workshop`
  - OK
* Agregar tabla (Sources > Tables) :
  - Database: `workshop`
  - Table Name: `weather`
  
* Create Slices & Dashboard [official docs](https://superset.incubator.apache.org/tutorial.html#creating-a-slice-and-dashboard)

## Sobre
* Ignacio Cicero 
* Jorge Goldman
* Juan Francisco Dalceggio