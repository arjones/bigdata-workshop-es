# ETL: US stocks analysis (BATCH)

## How to run our app

```bash
spark-submit \
  --master 'spark://master:7077' \
  --jars /app/postgresql-42.1.4.jar \
  src/batch/etl_steps.py \
  /dataset/stocks-small \
  /dataset/yahoo-symbols-201709.csv \
  /dataset/output.parquet
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

# ETL: US stocks analysis (STREAM)

### Start fake gen
```bash
docker exec -it worker1 bash

cd /app/python/us-stock-analysis/

# generate stream data
python src/stream/fake_stock_price_generator.py kafka:9092 stocks 2017-11-11T10:00:00Z
```

### Process using Spark Structured Stream API
```bash
....
....

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
