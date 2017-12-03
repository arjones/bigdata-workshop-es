package es.openweather

import dispatch._
import Defaults._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ read }
import org.json4s.NoTypeHints
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties

//http://krishnabhargav.github.io/scala,/how/to/2014/06/15/Scala-Json-REST-Example.html
object WeatherProducer {

  private val API_KEY = "b31f1615a005989872e7c12084ade812"

  def main(args: Array[String]): Unit = {
    //Step 1 : Prepare the request object

    //even though its a https . doing a .secure is not required
    val request = url("http://api.openweathermap.org/data/2.5/forecast")
    val requestAsGet = request.GET //not required but lets be explicit
    val BROKERS = "kafka:9092"
 
     
    val props = new Properties()
    props.put("bootstrap.servers", BROKERS)
    props.put("client.id", "OpenWeatherGenerator")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    
    val producer = new KafkaProducer[String, String](props)
    val topic = "OpenWeather"
    
    while (true) {
      
      //Step 2 : Set the required parameters
    val builtRequest = requestAsGet.addQueryParameter("id", "524901")
      .addQueryParameter("APPID", API_KEY)

    //Step 3: Make the request (method is already set above)
    val content = Http(builtRequest)

    //Step 4: Once the response is available
    //response completed successfully
    content onSuccess {

      //Step 5 : Request was successful & response was OK
      case x if x.getStatusCode() == 200 =>
        //Step 6 : Response was OK, read the contents
        val data = new ProducerRecord[String, String](topic, null, x.getResponseBody)
        producer.send(data)
        println("Sent:" + x.getStatusCode)
        //handleJsonOutput(x.getResponseBody)
        
      case y => //Step 7 : Response is not OK, read the error
        println("Failed with status code" + y.getStatusCode())
    }

    //Step 7 : Request did not complete successfully, read the error
    content onFailure {
      case x =>
        println("Failed but"); println(x.getMessage)
    }

      Thread.sleep(300)

    }

    
  }

  private def handleJsonOutput(body: String): Unit = {
    //required to set implicit here for JSON serialization to work properly
    implicit val formats = Serialization.formats(NoTypeHints)

    //read the output as a RootJsonObject instance
    //the read call does the trick of mapping JSon to the case classes
    val output = read[ResponseRoot](body)
    println(s"Total Results: ${output.cnt}")
    output.list.foreach { l =>
      println(s"${l.main.temp}")
    }
  }
}

case class WindObject(speed: Float, deg: Float)
case class WeatherObject(id: Int, main: String, description: String)
case class TempObject(temp: Float, temp_min: Float, temp_max: Float, pressure: Float, sea_level: Float, humidity: Float)
case class WeatherContainerObject(main: TempObject, weather: List[WeatherObject], wind: WindObject)
case class ResponseRoot(cod: String, cnt: Int, list: List[WeatherContainerObject])
