# Flags Analysis
## Spark Machine Learning (Random Forest)


```bash
sbt clean assembly

spark-submit \
  --class es.gilardenghi.flagsTrain \
  --master 'spark://master:7077' \
  target/scala-2.11/flags-analysis-assembly-0.1.jar \
  /dataset/flags/flags.csv \
  /dataset/flags.model


spark-submit \
  --class es.gilardenghi.flagsAnalysis \
  --master 'spark://master:7077' \
  target/scala-2.11/flags-analysis-assembly-0.1.jar \
  /dataset/flags/flags-user-input.csv \
  /dataset/flags.model
```


# Acerca del Dataset
Flags Data Set
https://archive.ics.uci.edu/ml/datasets/Flags/
Abstract: From Collins Gem Guide to Flags, 1986

Creators:
Collected primarily from the "Collins Gem Guide to Flags": Collins Publishers (1986).

Donor:
Richard S. Forsyth
8 Grosvenor Avenue
Mapperley Park
Nottingham NG3 5DX
0602-621676
