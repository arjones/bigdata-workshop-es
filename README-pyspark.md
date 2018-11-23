# Usando `pySpark`:

```bash
docker exec -it master bash
root@588acf96a879:/app# pyspark
```
```python
file = spark.read.text("/dataset/yahoo-symbols-201709.csv")
file.count()
```
#### [Introduction to PySpark](https://www.datacamp.com/courses/introduction-to-pyspark)