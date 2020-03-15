from pyspark.sql import SparkSession

spark = SparkSession \
    .builder \
    .appName("first_example") \
    .getOrCreate()

df = spark.read.format("jdbc") \
    .options(driver="org.postgresql.Driver", \
        url="jdbc:postgresql://postgres/workshop", \
        dbtable="stocks", \
        user="workshop", \
        password="w0rkzh0p") \
    .load()

elems_count = df.count()

print(f'Count: {elems_count}')

df.show()
