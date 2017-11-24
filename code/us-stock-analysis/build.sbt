import sbt.Keys._

name := "us-stock-analysis"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions += "-target:jvm-1.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.1.2" % "provided",
  "org.postgresql" % "postgresql" % "42.1.1",

  "org.apache.spark" %% "spark-streaming" % "2.1.2" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.1.2",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.1.2"
)

assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.first
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}