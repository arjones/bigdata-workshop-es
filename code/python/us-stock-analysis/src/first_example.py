from pyspark.sql import SparkSession

spark = SparkSession \
    .builder \
    .appName("first_example") \
    .getOrCreate()

df = spark.read.csv("/dataset/yahoo-symbols-201709.csv")

df.show()

spark.stop
