# ETL: US stocks analysis



### Create a jar containing your application and its deps
```bash
$ sbt clean assembly
```

### Use spark-submit to run your application

```bash
$ spark-submit \
  --class "es.arjon.FromCsvToParquet" \
  --master 'local[*]' \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar
```

```bash
$ spark-submit \
  --class "es.arjon.RunAll" \
  --master 'spark://master:7077' \
  --driver-class-path /dataset/postgresql-42.1.4.jar \
  target/scala-2.11/us-stock-analysis-assembly-0.1.jar
```
