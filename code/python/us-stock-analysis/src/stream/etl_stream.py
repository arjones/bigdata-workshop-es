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

    query.awaitTermination()


    # avg_pricing = stocks \
    #     .groupBy(F.col("symbol")) \
    #     .agg(F.avg(F.col("price")).alias("avg_price"))

    ####################################
    # Console Output
    ####################################
    # query2 = avg_pricing.writeStream \
    #     .outputMode('complete') \
    #     .format("console") \
    #     .trigger(processingTime="10 seconds") \
    #     .start()

    # query2.awaitTermination()

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

    # query3.awaitTermination()

    ####################################
    # Writing to Postgres
    ####################################

    # Simple insert
    # query = stream_to_postgres(stocks)
    # query.awaitTermination()

    # Average Price Aggregation
    # query = stream_aggregation_to_postgres(stocks)
    # query.awaitTermination()

    # Final Average Price Aggregation with Timestamp columns
    # query = stream_aggregation_to_postgres_final(stocks)
    # query.awaitTermination()

    pass


def define_write_to_postgres(table_name):
  
    def write_to_postgres(df, epochId):
        return (
            df.write
                .format("jdbc")
                .option("url", "jdbc:postgresql://postgres/workshop")
                .option("dbtable", f"workshop.{table_name}")
                .option("user", "workshop")
                .option("password", "w0rkzh0p")
                .option("driver", "org.postgresql.Driver")
                .mode('append')
                .save()
        )
    return write_to_postgres

    
def stream_to_postgres(stocks, output_table="streaming_inserts"):
    wstocks =  (
        stocks
            .withWatermark("timestamp", "60 seconds")
            .select("timestamp", "symbol", "price")
    )

    write_to_postgres_fn = define_write_to_postgres("streaming_inserts")
    
    query = (
        wstocks.writeStream
        .foreachBatch(write_to_postgres_fn)
        .outputMode("append")
        .trigger(processingTime="10 seconds")
        .start()
    )

    return query


def summarize_stocks(stocks):
    avg_pricing = (
        stocks
        .withWatermark("timestamp", "60 seconds")
        .groupBy(
            F.window("timestamp", "30 seconds"),
            stocks.symbol)
        .agg(F.avg("price").alias('avg_price'))
    )
    avg_pricing.printSchema()
    return avg_pricing


def stream_aggregation_to_postgres(stocks, output_table="streaming_inserts_avg_price"):

    avg_pricing = summarize_stocks(stocks)

    window_to_string = F.udf(lambda w: str(w.start) + ' - ' + str(w.end), StringType())
    
    write_to_postgres_fn = define_write_to_postgres(output_table)

    query = (
        avg_pricing\
        .withColumn("window", window_to_string("window"))
        .writeStream
        .foreachBatch(write_to_postgres_fn)
        .outputMode("append")
        .trigger(processingTime="10 seconds")
        .start()
    )

    return query


def stream_aggregation_to_postgres_final(stocks, output_table="streaming_inserts_avg_price_final"):

    avg_pricing = summarize_stocks(stocks)

    window_start_ts_fn = F.udf(lambda w: w.start, TimestampType())

    window_end_ts_fn = F.udf(lambda w: w.end, TimestampType())
    
    write_to_postgres_fn = define_write_to_postgres(output_table)

    query = (
        avg_pricing\
        .withColumn("window_start", window_start_ts_fn("window"))
        .withColumn("window_end", window_end_ts_fn("window"))
        .drop("window")
        .writeStream
        .foreachBatch(write_to_postgres_fn)
        .outputMode("append")
        .trigger(processingTime="10 seconds")
        .start()
    )

    return query


if __name__ == '__main__':
    start_stream(sys.argv)
