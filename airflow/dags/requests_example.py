"""Get data from API."""
import json
from datetime import datetime

import numpy as np
import pandas as pd
import requests

BASE_URL = 'https://www.alphavantage.co/query'
API_KEY = 'TFHNYCWBD71JBSON'
STOCK_FN = 'TIME_SERIES_DAILY'


def _get_stock_data(stock_symbol, date):
    date = f"{date:%Y-%m-%d}"  # read execution date from context
    end_point = (
        f"{BASE_URL}?function={STOCK_FN}&symbol={stock_symbol}"
        f"&apikey={API_KEY}&datatype=json"
    )
    print(f"Getting data from {end_point}...")
    r = requests.get(end_point)
    data = json.loads(r.content)
    df = (
        pd.DataFrame(data['Time Series (Daily)'])
        .T.reset_index()
        .rename(columns={'index': 'date'})
    )
    df = df[df['date'] == date]
    if not df.empty:
        for c in df.columns:
            if c != 'date':
                df[c] = df[c].astype(float)
        df['avg_price'] = (df['2. high'] + df['3. low']) / 2
        df['avg_num_trades'] = df['5. volume'] / 1440
    else:
        df = pd.DataFrame(
            [[date, np.nan, np.nan]], columns=['date', 'avg_num_trades', 'avg_price'],
        )
    df['symbol'] = stock_symbol
    df = df[['date', 'symbol', 'avg_num_trades', 'avg_price']]
    return df


if __name__ == '__main__':
    yesterday = datetime(2020, 4, 13)
    df1 = _get_stock_data('aapl', yesterday)
