from random import randrange, random
from datetime import datetime, timedelta
from time import sleep

from kafka import KafkaProducer
import json
import sys


class QuoteGenerator:
    # Using as SEED for the generator last 90 days of stocks
    # price: Max Closing Price
    # volatility: StdDev of Closing Pricing
    #    df.groupBy($"symbol")
    #      .agg(stddev_pop($"close").as("volatility"), max($"close").as("price"))
    #      .orderBy($"symbol")
    quotes_list = [("AAPL", 175.61, 6.739169981533334),
                   ("BABA", 188.51, 5.637335242825282),
                   ("CSCO", 34.62, 0.9673997717593282),
                   ("DHR", 93.24, 2.949284608917899),
                   ("EBAY", 38.99, 0.8110024414266584),
                   ("FB", 182.66, 4.14292553638126),
                   ("GOOG", 1039.85, 37.960859608812854),
                   ("GOOGL", 1058.29, 39.11749241707603),
                   ("IBM", 160.47, 4.8367462989079755),
                   ("INTC", 46.826, 3.678237311321825),
                   ("JNJ", 143.62, 4.336597380435497),
                   ("MELI", 292.05, 19.703519789367583),
                   ("MSFT", 84.56, 3.7745700470384693),
                   ("ORCL", 52.593, 1.4026418724678085),
                   ("QCOM", 65.49, 3.962328548164577),
                   ("TSLA", 385.0, 21.667055079857995),
                   ("TXN", 98.54, 5.545761038090265),
                   ("WDC", 89.9, 1.7196676293981952),
                   ("XRX", 33.86, 1.4466726098188216)]

    def __init__(self, trading_start_at):
        self.trading_start_datetime = trading_start_at

    # a very naive impl of marketing hours
    # not consider weekends nor holidays
    def __nextMarketTime(self):
        # Sometimes it substracts 1 and generates late arriving tickers
        tick = randrange(5) - 1
        next_time = self.trading_start_datetime + timedelta(minutes=tick)
        # Market should be closed, bump to next day
        if next_time.hour > 15:
            next_time = (next_time + timedelta(days=1)).replace(hour=10, minute=0)

        self.trading_start_datetime = next_time
        return next_time

    def __signal(self):
        if randrange(2) == 0:
            return 1
        else:
            return -1

    def next_symbol(self):
        quote_idx = randrange(len(self.quotes_list) - 1)
        quote = self.quotes_list[quote_idx]

        # price = quote.price + (signal * rnd.nextDouble * quote.volatility * 3)
        price = quote[1] + (self.__signal() * random() * quote[2] * 3)

        return {
            'symbol': quote[0],
            'timestamp': self.__nextMarketTime().isoformat(),
            'price': float(f'{price:2.3f}')
        }


if __name__ == '__main__':
    # Initialization
    args = sys.argv

    if len(args) != 4:
        print(f"""
        |Usage: {args[0]} <brokers> <topics> <start_date>
        |  <brokers> is a list of one or more Kafka brokers
        |  <topic> one kafka topic to produce to
        |  <start_date> [OPTIONAL] iso timestamp from when to start producing data
        |
        |  {args[0]} kafka:9092 stocks 2017-11-11T10:00:00Z
        """)
        sys.exit(1)

    _, brokers, topic, start_date = args
    trading_start_datetime = datetime.strptime(start_date, '%Y-%m-%dT%H:%M:%S%z')

    quote_gen = QuoteGenerator(trading_start_datetime)

    producer = KafkaProducer(
        bootstrap_servers=brokers,
        value_serializer=lambda v: json.dumps(v).encode('utf-8'))

    while True:
        stock_data = quote_gen.next_symbol()
        producer.send(topic, stock_data)
        print(stock_data)
        sleep(.5)
