package es.arjon

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.io.Source

object KafkaProducerCmd extends App {
  if (args.length < 3) {
    System.err.println(
      s"""
         |Usage: KafkaProducerCmd <brokers> <topics> <filename>
         |  <brokers> is a list of one or more Kafka brokers
         |  <topic> one kafka topic to produce to
         |  <files> is a list of files that must be sent
         |
         |  KafkaProducer localhost:9092 stocks dataset/example.json
        """.stripMargin)
    System.exit(1)
  }

  val Array(brokers, topic, files) = args

  val props = new Properties()
  props.put("bootstrap.servers", brokers)
  props.put("client.id", "KafkaProducer")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)
  var counter = 0

  for {
    file: String <- files.split(",").toSet
    line <- Source.fromFile(file).getLines
  } {
    val producedAt = System.currentTimeMillis()
    val data = new ProducerRecord[String, String](topic, producedAt.toString, line)

    producer.send(data)
    Thread.sleep(1000)

    counter += 1
    println(s"Sending line $counter and sleeping 1sec")
  }

  producer.close()
}
