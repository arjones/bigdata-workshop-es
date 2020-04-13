# ETL: US stocks analysis (BATCH)

## How to run our app

```bash
docker exec -it master bash
cd /app/python/us-stock-analysis

spark-submit \
  --master 'spark://master:7077' \
  --jars /app/postgresql-42.1.4.jar \
  src/batch/etl_steps.py \
  /dataset/stocks-small \
  /dataset/yahoo-symbols-201709.csv \
  /dataset/output.parquet


pyspark \
  --master 'spark://master:7077' \
  --jars /app/postgresql-42.1.4.jar
```

## More examples

```bash
spark-submit \
  --master 'spark://master:7077' \
  src/examples/first_example.py

spark-submit \
  --master 'spark://master:7077' \
  --jars /app/postgresql-42.1.4.jar \
  src/examples/postgres_example.py
```
# Create a Project using `venv`

```bash
mkdir project1
cd project1

# Create virtualenv
python3 -m venv venv
source venv/bin/activate

# Upgrade pip & Install deps
pip install --upgrade pip
pip install -r requirements.txt

charm .
```

# ETL: US stocks analysis (STREAMING)

### Comenzar fake generator
```bash
docker exec -it worker1 bash

cd /app/python/us-stock-analysis/

# generate stream data
python src/stream/fake_stock_price_generator.py kafka:9092 stocks 2017-11-11T10:00:00Z
```

### Process using Spark Structured Stream API
[Structured Streaming + Kafka Integration Guide](https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html#deploying)

```bash
spark-submit \
  --master 'spark://master:7077' \
  --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.5 \
  --jars /app/postgresql-42.1.4.jar \
  src/stream/etl_stream.py \
  kafka:9092 stocks
```

(Para stopear el comando presiones `Ctrl + c` )

### Escribiendo a Postgres

En una nueva tab de la terminal ingresar a la línea de comando de Postgres con:

```bash
./control-env.sh psql
```

Crear las tablas que vamos a utilizar para el ejercicio con los siguientes comandos (copiar el comando entero, pegar y presionar enter por cada uno)

```sql
CREATE TABLE streaming_inserts (
    "timestamp" timestamptz NOT NULL,
    symbol varchar(10),
    price real
);
```

```sql
CREATE TABLE streaming_inserts_avg_price (
    "window" varchar(128),
    symbol varchar(10),
    avg_price real
);
```

```sql
CREATE TABLE streaming_inserts_avg_price_final (
    window_start timestamp,
    window_end timestamp,
    symbol varchar(10),
    avg_price real
);
```

Asegurarse que todas las líneas de la 59 a la 114 del archivo `etl_stream.py` se encuentran comentadas.

Descomentar el primer job de inserción a Postgres en las siguientes líneas
```python
  # Simple insert
  query = stream_to_postgres(stocks)
  query.awaitTermination()
```

Asegurarse que el generador de datos está corriendo y ejecutar el job de streaming con el mismo comando que con anterioridad:

Comente las líneas del primer job (`stream_to_postgres`) y descomente las del job `stream_aggregation_to_postgres`.
Revise el código de la nueva función y observe las diferencias con el anterior. Qué diferencias observa?
Luego de correrlo revise los datos insertados en la tabla con `psql`. Qué ve de particular en la fecha de comienzo?

Finalmente comente el job `stream_aggregation_to_postgres` y descomente `stream_aggregation_to_postgres_final`.
Agregue una visualización en Superset para poder visualizar las filas insertándose en esta nueva tabla.

Una vez completados los pasos anteriores pruebe algunos de las siguientes modificaciones:

1. Agregue al job final lógica para que además de calcular el avg_price calcule el max de cada ventana.
2. Agregue nuevas visualizaciones al dashboard de Superset y haga que se refresque cada 10 segundos.
3. Agregue al ETL batch el código necesario para que también guarde la información del volumen de cada acción.
4. Agregue al `fake_stock_price_generator.py` lógica para generar un volumen para cada acción de manera artificial además del precio. Modifique los jobs de streaming para procesar este dato. 






