package es.arjon

import kafka.serializer.StringDecoder
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingETL extends App {
  if (args.length < 2) {
    System.err.println(
      s"""
         |Usage: StreamingETL <brokers> <topics>
         |  <brokers> is a list of one or more Kafka brokers
         |  <topics> is a list of one or more kafka topics to consume from
         |
        """.stripMargin)
    System.exit(1)
  }

  val Array(brokers, topics) = args
  val spark = SparkSession.
    builder.
    appName("Stocks:StreamingETL").
    getOrCreate()

  val conf = spark.conf
  val ssc = new StreamingContext(spark.sparkContext, Seconds(1))
  ssc.checkpoint("checkpoint")


  // Create direct kafka stream with brokers and topics
  val topicsSet = topics.split(",").toSet
  val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
  val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
    ssc, kafkaParams, topicsSet)

  messages.print()
  ssc.start()
  ssc.awaitTermination()
}
