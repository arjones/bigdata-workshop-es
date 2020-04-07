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
Acceda al [Jupyter Notebook aqui](http://localhost:8888/), los notebook disponibles en ese workshop [están en Github](https://github.com/arjones/bigdata-workshop-es/tree/master/jupyter/notebook)

## Material de lectura:

* [Apache Spark in Python: Beginner's Guide](https://www.datacamp.com/community/tutorials/apache-spark-python)
* [Introduction to PySpark](https://www.datacamp.com/courses/introduction-to-pyspark)
* [pySpark: Evaluating the machine learning model](https://www.datacamp.com/community/tutorials/apache-spark-tutorial-machine-learning)


## Visualización de Datos

* [Python Data Visualization with Matplotlib](https://stackabuse.com/python-data-visualization-with-matplotlib/)
* [Top 50 matplotlib Visualizations](https://www.machinelearningplus.com/plots/top-50-matplotlib-visualizations-the-master-plots-python/)
* [Seaborn Library for Data Visualization in Python: Part 1](https://stackabuse.com/seaborn-library-for-data-visualization-in-python-part-1/)


____
Gustavo Arjones &copy; 2017-2020
