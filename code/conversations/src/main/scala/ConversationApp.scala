import org.apache.spark.sql.SparkSession

object ConversationApp {
  def main(args: Array[String]): Unit = {
    val logFile = "dataset/yahoo-symbols-201709.csv"
    val spark = SparkSession.builder.appName("us-stock-analysis").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()

    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()

    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()

  }
}
