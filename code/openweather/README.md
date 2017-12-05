# Openweather



### Create a jar containing your application and its deps
```bash
$ sbt clean assembly
```
## Iniciar el simulador de acciones
Dentro del mismo package tenemos la clase del simulador [WeatherProducer](./code/openweather/src/main/scala/es/openweather/WeatherProducer.scala)

```bash
# Compilar el similador
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

```bash
docker exec -it wksp_master_1 bash

cd /app/openweater
spark-submit --master 'spark://master:7077' \
  --class "es.openweather.WeatherConsumer" \
  --total-executor-cores 2 \
  target/scala-2.11/openweather-assembly-0.1.jar \
  kafka:9092 stoOpenWeathercks
```
