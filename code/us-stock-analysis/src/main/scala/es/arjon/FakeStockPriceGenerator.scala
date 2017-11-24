package es.arjon

import java.time.ZonedDateTime
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object FakeStockPriceGenerator extends App {
  val rnd = new scala.util.Random(42)
  // This is when Dataset ends
  var tradingBeginOfTime = ZonedDateTime.parse("2017-11-11T10:00:00Z")

  if (args.length < 2) {
    System.err.println(
      s"""
         |Usage: FakeStockPriceGenerator <brokers> <topics>
         |  <brokers> is a list of one or more Kafka brokers
         |  <topic> one kafka topic to produce to
         |
         |  FakeStockPriceGenerator kafka:9092 stocks
        """.stripMargin)
    System.exit(1)
  }

  val Array(brokers, topic) = args

  println(
    s"""
       |Generating faking stocks prices at $brokers/$topic
       |Each tick (300ms) represents 3min in clock time
    """.stripMargin)

  val props = new Properties()
  props.put("bootstrap.servers", brokers)
  props.put("client.id", "FakeStockPriceGenerator")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)
  var counter = 0

  while (true) {
    val stock = nextSymbol
    val data = new ProducerRecord[String, String](topic, null, stock)

    producer.send(data)
    Thread.sleep(300)

    counter += 1
    println(s"# $counter: $stock")
  }

  producer.close()


  def nextSymbol: String = {
    // a very naive impl of marketing hours
    // not consider weekends nor holidays
    def nextMarketTime = {
      val tick = 3
      val proposedNextTime = tradingBeginOfTime.plusMinutes(tick)
      val nextTime = if (proposedNextTime.getHour > 15)
        proposedNextTime.plusDays(1).withHour(10).withMinute(0)
      else
        proposedNextTime

      tradingBeginOfTime = nextTime
      nextTime
    }


    case class StockConf(symbol: String, price: Double, volatility: Double)

    // Using as SEED for the generator last 90 days of stocks
    // price: Max Closing Price
    // volatility: StdDev of Closing Pricing
    //    df.groupBy($"symbol")
    //      .agg(stddev_pop($"close").as("volatility"), max($"close").as("price"))
    //      .orderBy($"symbol")
    //
    val quotes = List(
      StockConf("AAPL", 175.61, 6.739169981533334),
      StockConf("BABA", 188.51, 5.637335242825282),
      StockConf("CSCO", 34.62, 0.9673997717593282),
      StockConf("DHR", 93.24, 2.949284608917899),
      StockConf("EBAY", 38.99, 0.8110024414266584),
      StockConf("FB", 182.66, 4.14292553638126),
      StockConf("GOOG", 1039.85, 37.960859608812854),
      StockConf("GOOGL", 1058.29, 39.11749241707603),
      StockConf("IBM", 160.47, 4.8367462989079755),
      StockConf("INTC", 46.826, 3.678237311321825),
      StockConf("JNJ", 143.62, 4.336597380435497),
      StockConf("MELI", 292.05, 19.703519789367583),
      StockConf("MSFT", 84.56, 3.7745700470384693),
      StockConf("ORCL", 52.593, 1.4026418724678085),
      StockConf("QCOM", 65.49, 3.962328548164577),
      StockConf("TSLA", 385.0, 21.667055079857995),
      StockConf("TXN", 98.54, 5.545761038090265),
      StockConf("WDC", 89.9, 1.7196676293981952),
      StockConf("XRX", 33.86, 1.4466726098188216)
    )

    def signal = if (rnd.nextInt(2) == 0) 1 else -1

    val quote = quotes(rnd.nextInt(quotes.size))

    val price = quote.price + (signal * rnd.nextDouble * quote.volatility)

    //
    f"""{"symbol":"${quote.symbol}","timestamp":"${nextMarketTime}","price":$price%2.3f}"""
  }

}
