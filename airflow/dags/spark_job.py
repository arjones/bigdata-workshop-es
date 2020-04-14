import os

from pyspark import SparkConf, SparkContext

BLACK_LIST = ["the", "be", "to", "of", "=", "=="]


sc = SparkContext("local", "PySpark Word Count Exmaple")
input_file = os.environ.get('INPUT_FILE', f"{os.environ['SPARK_HOME']}/README.md")
output_file = os.environ.get('OUTPUT_FILE', '/spark-job/output.csv')
log_file = f"{input_file}"  # Should be some file on your system

words = sc.textFile(log_file).flatMap(lambda line: line.split(" "))
word_counts = (
    words.filter(lambda word: word != '' and len(word) > 1 and word not in BLACK_LIST)
    .map(lambda word: (word, 1))
    .reduceByKey(lambda a, b: a + b)
    .max(lambda x: x[1])
)

with open(output_file, 'w') as output:
    output.write(f"{word_counts[0]},{word_counts[1]}")
