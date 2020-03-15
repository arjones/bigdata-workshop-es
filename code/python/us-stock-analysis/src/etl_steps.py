from pyspark.sql import SparkSession

spark = SparkSession \
    .builder \
    .appName("Stocks:ETL") \
    .getOrCreate()

