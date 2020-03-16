from pyspark.sql import SparkSession

# UDF
from pyspark.sql.types import StringType
#
from pyspark.sql.functions import *

spark = SparkSession \
    .builder \
    .appName("Stocks:ETL") \
    .getOrCreate()


# Create a function and define it as a UDF
# UDF
def extract_symbol_from(filename):
    return filename.split('/')[-1].split('.')[0].upper()


extract_symbol = udf(lambda filename: extract_symbol_from(filename), StringType())

data_folder = '/dataset/stocks-small/'

df = spark.read \
    .option("header", True) \
    .option("inferSchema", True) \
    .csv(data_folder) \
    .withColumn("name", extract_symbol(input_file_name())) \
    .withColumnRenamed("Date", "dateTime") \
    .withColumnRenamed("Open", "open") \
    .withColumnRenamed("High", "high") \
    .withColumnRenamed("Low", "low") \
    .withColumnRenamed("Close", "close") \
    .drop("Volume", "OpenInt")


def load_lookup_data(filename):
    df = spark.read. \
        option("header", True). \
        option("inferSchema", True). \
        csv(filename). \
        select("Ticker", "Category Name"). \
        withColumnRenamed("Ticker", "symbol"). \
        withColumnRenamed("Category Name", "category")

    # return df.filter("Country = \"USA\""). \
    # return df.filter("Country" === "USA").
    return df
