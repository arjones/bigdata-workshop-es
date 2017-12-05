package es.openweather

import dispatch._
import Defaults._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.NoTypeHints
import scala.io.Source
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties

//http://krishnabhargav.github.io/scala,/how/to/2014/06/15/Scala-Json-REST-Example.html
object WeatherProducer {

  private val API_KEY = "b31f1615a005989872e7c12084ade812"
  private val CITIES_FILE = "/dataset/openweather/cityList.csv"

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: WeatherProducer <brokers> <topics>
           |  <brokers> is a list of one or more Kafka brokers
           |  <topic> one kafka topic to produce to
           |
         |  WeatherProducer kafka:9092 OpenWeather
        """.stripMargin)
      System.exit(1)
    }
    val Array(brokers, topic) = args
    //val brokers = "kafka:9092"
    //val topic = "OpenWeather"
    println(
    s"""
         |Generating weather data at $brokers/$topic
         |Each tick (500ms)
    """.stripMargin)

    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", "OpenWeatherGenerator")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    val cities = Source.fromFile(CITIES_FILE)
    val request = url("http://api.openweathermap.org/data/2.5/weather").GET
    val cityList = Source.fromFile(CITIES_FILE).getLines().toList
    val eternalCityList:Stream[String] = for(x <- Stream.continually(1); y<-cityList) yield y
    eternalCityList.foreach(cityLine => {
      val cityId = cityLine.split(",")(0)
      if (cityId != "ID") {
        println(s"Requesting city id ${cityId}")
        val builtRequest = request
          .addQueryParameter("id", cityId)
          .addQueryParameter("units", "metric")
          .addQueryParameter("APPID", API_KEY)
        val content = Http(builtRequest)
        content onSuccess {
          case x if x.getStatusCode() == 200 => {
            val data = new ProducerRecord[String, String](topic, null, x.getResponseBody)
            producer.send(data)
          }
          case y => println(s"Failed with status code ${y.getStatusCode()}")
        }
        content onFailure {
          case x => println(s"Failed but ${x.getMessage}")
        }
        Thread.sleep(500)
      }
    })
    cities.close()
  }

//  private def handleJsonOutput(body: String): Unit = {
//    implicit val formats = Serialization.formats(NoTypeHints)
//    val output = read[ResponseRoot](body)
//    println(s"Total Results: ${output.cnt}")
//    output.list.foreach { l =>
//      println(s"${l.main.temp}")
//    }
//  }
}

// Para forecast
//case class WindObject(speed: Float, deg: Float)
//case class WeatherContainerObject(main: TempData, weather: List[WeatherObject], wind: WindObject)
//case class ResponseRoot(cod: String, cnt: Int, list: List[WeatherContainerObject])

// Para clima actual
case class TempData(temp: Float, temp_min: Float, temp_max: Float, pressure: Float, sea_level: Float, humidity: Float)
case class WeatherObject(id: Int, main: String, description: String)
case class Coordinates(lon: Float, lat: Float)
case class SysData(sunrise: Long, sunset: Long)
case class CurrentWeather(coord: Coordinates, weather: List[WeatherObject], main: TempData, visibility: Int, dt: Long, sys: SysData)