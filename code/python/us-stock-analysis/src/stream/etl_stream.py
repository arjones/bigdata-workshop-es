import sys

from time import sleep

from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, year, month, dayofmonth, hour, minute
from pyspark.sql import functions as F  # col doesn't import correctly
from pyspark.sql.types import TimestampType, StringType, StructType, StructField, DoubleType


def validate_params(args):
    if len(args) != 3:
        print(f"""
         |Usage: {args[0]} <brokers> <topics>
         |  <brokers> is a list of one or more Kafka brokers
         |  <topic> is a a kafka topic to consume from
         |
         |  {args[0]} kafka:9092 stocks
        """)
        sys.exit(1)
    pass


def create_spark_session():
    return SparkSession \
        .builder \
        .appName("Stocks:Stream:ETL") \
        .getOrCreate()


def start_stream(args):
    validate_params(args)
    _, brokers, topic = args

    spark = create_spark_session()

    json = spark \
        .readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", brokers) \
        .option("subscribe", topic) \
        .load()

    json.printSchema()

    # Explicitly set schema
    schema = StructType([StructField("symbol", StringType(), False),
                         StructField("timestamp", TimestampType(), False),
                         StructField("price", DoubleType(), False)])

    json_options = {"timestampFormat": "yyyy-MM-dd'T'HH:mm'Z'"}
    stocks_json = json \
        .select(from_json(F.col("value").cast("string"), schema, json_options).alias("content"))

    stocks_json.printSchema

    stocks = stocks_json.select("content.*")

    ####################################
    # Stream to Parquet
    ####################################
    query = stocks \
        .withColumn('year', year(F.col('timestamp'))) \
        .withColumn('month', month(F.col('timestamp'))) \
        .withColumn('day', dayofmonth(F.col('timestamp'))) \
        .withColumn('hour', hour(F.col('timestamp'))) \
        .withColumn('minute', minute(F.col('timestamp'))) \
        .writeStream \
        .format('parquet') \
        .partitionBy('year', 'month', 'day', 'hour', 'minute') \
        .option('startingOffsets', 'earliest') \
        .option('checkpointLocation', '/dataset/checkpoint') \
        .option('path', '/dataset/streaming.parquet') \
        .trigger(processingTime='30 seconds') \
        .start()

    avg_pricing = stocks \
        .groupBy(F.col("symbol")) \
        .agg(F.avg(F.col("price")).alias("avg_price"))

    ####################################
    # Console Output
    ####################################
    query2 = avg_pricing.writeStream \
        .outputMode('complete') \
        .format("console") \
        .trigger(processingTime="10 seconds") \
        .start()

    ####################################
    # Table in Memory
    ####################################
    # query3 = avg_pricing \
    #     .writeStream \
    #     .queryName("avgPricing") \
    #     .outputMode("complete") \
    #     .format("memory") \
    #     .trigger(processingTime="10 seconds") \
    #     .start()
    #
    # while True:
    #     print('\n' + '_' * 30)
    #     # interactively query in-memory table
    #     spark.sql('SELECT * FROM avgPricing').show()
    #     print(query3.lastProgress)
    #     sleep(10)

    query2.awaitTermination()
    pass


if __name__ == '__main__':
    start_stream(sys.argv)
