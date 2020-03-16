from pyspark.sql import SparkSession

spark = SparkSession \
    .builder \
    .appName("first_example") \
    .getOrCreate()

df = spark.read \
    .format("jdbc") \
    .option("url", "jdbc:postgresql://postgres/workshop") \
    .option("dbtable", "workshop.stocks") \
    .option("user", "workshop") \
    .option("password", "w0rkzh0p") \
    .option("driver", "org.postgresql.Driver") \
    .load()

df.printSchema()

elems_count = df.count()

print(f'Count: {elems_count}\n\n')

df.show()
