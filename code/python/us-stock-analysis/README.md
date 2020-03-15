# ETL: US stocks analysis


# Create a Project using `venv`

```bash
python3 -m venv us-stock-analysis
source us-stock-analysis/bin/activate

cd us-stock-analysis
pip install --upgrade pip
pip install -r requirements.txt
```











### Create a jar containing your application and its deps
```bash

Zpip3 install -r requirements.txt

# Generating Wheel
pip3 wheel -r requirements.txt -w dist


cd ./src && zip ../dist/mysparklib.zip -r ./mysparklib


```

### Use spark-submit to run your application

```bash
spark-submit \
  --master 'spark://master:7077' \
  src/first_example.py

spark-submit \
  --master 'spark://master:7077' \
  --jars /app/postgresql-42.1.4.jar \
  src/postgres_example.py


```

```bash
$ spark-submit \
  --class "es.arjon.RunAll" \
  --master 'spark://master:7077' \
  --driver-class-path /dataset/postgresql-42.1.4.jar \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar
```
