# ETL: US stocks analysis



### Package a jar containing your application
```bash
$ sbt package
```

### Use spark-submit to run your application

```bash
$ spark-submit \
  --class "es.arjon.FromCsvToParquet" \
  --master 'local[*]' \
  target/scala-2.11/us-stock-analysis_2.11-0.1.jar
```

```bash
$ spark-submit \
  --class "es.arjon.RunAll" \
  --master 'local[*]' \
  --driver-class-path ~/.ivy2/cache/org.postgresql/postgresql/jars/postgresql-42.1.4.jar \
  target/scala-2.11/us-stock-analysis_2.11-0.1.jar
```
