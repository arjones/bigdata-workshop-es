package es.arjon
import org.apache.spark.sql.SparkSession

trait DatasetUtil {

  // when using console add this
  // implicit val ss = spark
  def loadData(csv: String)(implicit spark: SparkSession) = {
    import org.apache.spark.sql.types._

    val schema = StructType(Seq(
      StructField("creditability", DoubleType, nullable = false),
      StructField("balance", DoubleType, nullable = false),
      StructField("duration", DoubleType, nullable = false),
      StructField("history", DoubleType, nullable = false),
      StructField("purpose", DoubleType, nullable = false),
      StructField("amount", DoubleType, nullable = false),
      StructField("savings", DoubleType, nullable = false),
      StructField("employment", DoubleType, nullable = false),
      StructField("instPercent", DoubleType, nullable = false),
      StructField("sexMarried", DoubleType, nullable = false),
      StructField("guarantors", DoubleType, nullable = false),
      StructField("residenceDuration", DoubleType, nullable = false),
      StructField("assets", DoubleType, nullable = false),
      StructField("age", DoubleType, nullable = false),
      StructField("concCredit", DoubleType, nullable = false),
      StructField("apartment", DoubleType, nullable = false),
      StructField("credits", DoubleType, nullable = false),
      StructField("occupation", DoubleType, nullable = false),
      StructField("dependents", DoubleType, nullable = false),
      StructField("hasPhone", DoubleType, nullable = false),
      StructField("foreign", DoubleType, nullable = false)
    ))

    spark.read.
      option("header", false).
      schema(schema).
      csv(csv)
  }
}
