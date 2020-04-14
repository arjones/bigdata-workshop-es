"""Stocks dag."""
import json
from datetime import datetime

import numpy as np
import pandas as pd
import requests
from airflow.models import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.operators.sqlite_operator import SqliteOperator
from sqlite_cli import SqLiteClient

BASE_URL = 'https://www.alphavantage.co/query'
API_KEY = 'TFHNYCWBD71JBSON'
STOCK_FN = 'TIME_SERIES_DAILY'

SQL_DB = '/tmp/sqlite_default.db'  # This is defined in Admin/Connections
SQL_TABLE = 'stocks_daily'
SQL_CREATE = f"""
CREATE TABLE IF NOT EXISTS {SQL_TABLE} (
date TEXT,
symbol TEXT,
avg_num_trades REAL,
avg_price REAL,
UNIQUE(date,symbol)
)
"""


def _get_stock_data(stock_symbol, **context):
    date = f"{context['execution_date']:%Y-%m-%d}"  # read execution date from context
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


def _insert_daily_data(**context):
    task_instance = context['ti']
    df = task_instance.xcom_pull(task_ids='get_daily_data')
    sql_cli = SqLiteClient(SQL_DB)
    sql_cli.insert_from_frame(df, SQL_TABLE)
    return


default_args = {'owner': 'pedro', 'retries': 0, 'start_date': datetime(2020, 4, 8)}
with DAG('stocks', default_args=default_args, schedule_interval='0 4 * * *') as dag:
    create_table_if_not_exists = SqliteOperator(
        task_id='create_table_if_not_exists',
        sql=SQL_CREATE,
        sqlite_conn_id='sqlite_default',
    )
    get_daily_data = PythonOperator(
        task_id='get_daily_data',
        python_callable=_get_stock_data,
        op_args=['aapl'],
        provide_context=True,
    )
    # Add insert stock data
    insert_daily_data = PythonOperator(
        task_id='insert_daily_data',
        python_callable=_insert_daily_data,
        provide_context=True,
    )
    create_table_if_not_exists >> get_daily_data >> insert_daily_data
