# Usando `pySpark`:

## Consola

```bash
docker exec -it master bash
root@588acf96a879:/app# pyspark
```
```python
file = spark.read.text("/dataset/yahoo-symbols-201709.csv")
file.count()
for line in file.take(10):
  print(line)
```

## Usando Jupyter Notebook
Acceda al [Jupyter Notebook aqui](http://localhost:8888/)

## Material de lectura:

* [Apache Spark in Python: Beginner's Guide](https://www.datacamp.com/community/tutorials/apache-spark-python)
* [Introduction to PySpark](https://www.datacamp.com/courses/introduction-to-pyspark)
* [pySpark: Evaluating the machine learning model](https://www.datacamp.com/community/tutorials/apache-spark-tutorial-machine-learning)


____
Gustavo Arjones &copy; 2017-2018
