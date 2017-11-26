import sbt.Keys._

name := "credit-risk-analysis"

version := "0.1"

scalaVersion := "2.11.12"

scalacOptions += "-target:jvm-1.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.1.2" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.1.2" % "provided",

  "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()
)

assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.first
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
