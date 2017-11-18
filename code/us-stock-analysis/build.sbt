name := "us-stock-analysis"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.1.2",
  "org.postgresql" % "postgresql" % "42.1.1"
)
