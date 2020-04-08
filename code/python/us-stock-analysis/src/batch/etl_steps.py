import sys

from pyspark.sql import SparkSession

# UDF
from pyspark.sql.types import StringType

# https://spark.apache.org/docs/latest/api/python/pyspark.sql.html#module-pyspark.sql.functions
from pyspark.sql import functions as F
from pyspark.sql.window import Window

# Initialization
args = sys.argv

if len(args) != 4:
    print(f"""
   |Usage: {args[0]} <dataset folder> <lookup file> <output folder>
   |  <dataset folder> folder where stocks data are located
   |  <lookup file> file containing lookup information
   |  <output folder> folder to write parquet files
   |
   |  {args[0]} /dataset/stocks-small /dataset/yahoo-symbols-201709.csv /dataset/output.parquet
    """)
    sys.exit(1)

_, stocks_dir, lookup_file, output_dir = args

spark = SparkSession \
    .builder \
    .appName("Stocks:ETL") \
    .getOrCreate()


#
def csv_stocks_df(stocks_folder):
    # Create a function and define it as a UDF
    # UDF
    def extract_symbol_from(filename):
        return filename.split('/')[-1].split('.')[0].upper()

    extract_symbol = F.udf(lambda filename: extract_symbol_from(filename), StringType())

    df = spark.read \
        .option("header", True) \
        .option("inferSchema", True) \
        .csv(stocks_folder) \
        .withColumn("name", extract_symbol(F.input_file_name())) \
        .withColumnRenamed("Date", "dateTime") \
        .withColumnRenamed("Open", "open") \
        .withColumnRenamed("High", "high") \
        .withColumnRenamed("Low", "low") \
        .withColumnRenamed("Close", "close") \
        .drop("Volume", "OpenInt")

    return df


# Load lookup CSV and convert into DataFrame
def load_lookup_data(filename):
    # df.filter("Country = \"USA\""). \
    # df.filter("Country" === "USA").
    df = spark.read. \
        option("header", True). \
        option("inferSchema", True). \
        csv(filename). \
        select("Ticker", "Category Name"). \
        withColumnRenamed("Ticker", "symbol"). \
        withColumnRenamed("Category Name", "category")

    return df


df_stocks = csv_stocks_df(stocks_dir)
print("Sample of df_stocks data:")
df_stocks.show(3)

symbols_lookup = load_lookup_data(lookup_file)
print("Sample of symbols_lookup data:")
symbols_lookup.show(3)

joined_df = df_stocks \
    .withColumnRenamed('dateTime', "full_date") \
    .filter("full_date >= \"2017-09-01\"") \
    .withColumn("year", F.year("full_date")) \
    .withColumn("month", F.month("full_date")) \
    .withColumn("day", F.dayofmonth("full_date")) \
    .withColumnRenamed("name", "symbol") \
    .join(symbols_lookup, ["symbol"])

print("Sample of joined_df data:")
joined_df.show()

# Calculate Moving Average
# https://stackoverflow.com/questions/45806194/pyspark-rolling-average-using-timeseries-data

window20 = (Window.partitionBy(F.col('symbol')).orderBy(F.col("full_date")).rowsBetween(-20, 0))
window50 = (Window.partitionBy(F.col('symbol')).orderBy(F.col("full_date")).rowsBetween(-50, 0))
window100 = (Window.partitionBy(F.col('symbol')).orderBy(F.col("full_date")).rowsBetween(-100, 0))

# // Calculate the moving average
stocks_moving_avg_df = joined_df \
    .withColumn("ma20", F.avg("close").over(window20)) \
    .withColumn("ma50", F.avg("close").over(window50)) \
    .withColumn("ma100", F.avg("close").over(window100))

print("Sample of stocks_moving_avg_df data:")
stocks_moving_avg_df.show()

# Write to Parquet
stocks_moving_avg_df \
    .write \
    .mode('overwrite') \
    .partitionBy("year", "month", "day") \
    .parquet(output_dir)

# Write to Postgres
stocks_moving_avg_df \
    .drop("year", "month", "day") \
    .write \
    .format("jdbc") \
    .option("url", "jdbc:postgresql://postgres/workshop") \
    .option("dbtable", "workshop.stocks") \
    .option("user", "workshop") \
    .option("password", "w0rkzh0p") \
    .option("driver", "org.postgresql.Driver") \
    .mode('append') \
    .save()

print("All done")
