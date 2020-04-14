"""Stocks dag extended."""
import json
from datetime import datetime
from time import sleep

import numpy as np
import pandas as pd
import requests
import sqlalchemy.exc
from airflow.models import DAG
from airflow.operators.email_operator import EmailOperator
from airflow.operators.python_operator import PythonOperator
from airflow.operators.sqlite_operator import SqliteOperator
from sqlite_cli import SqLiteClient

BASE_URL = 'https://www.alphavantage.co/query'
API_KEY = 'TFHNYCWBD71JBSON'
STOCK_FN = 'TIME_SERIES_DAILY'

SQL_DB = '/tmp/sqlite_default.db'  # This is defined in Admin/Connections
SQL_TABLE = 'stocks_daily_extended'
SQL_CREATE = f"""
CREATE TABLE IF NOT EXISTS {SQL_TABLE} (
date TEXT,
symbol TEXT,
avg_num_trades REAL,
avg_price REAL,
UNIQUE(date,symbol)
)
"""
SQL_REPORT = f"""
SELECT symbol, avg_num_trades
FROM {SQL_TABLE}
WHERE date = '{{date}}'
ORDER BY avg_num_trades DESC
LIMIT 1
"""

STOCKS = {'apple': 'aapl', 'tesla': 'tsla', 'facebook': 'fb'}


def _get_stock_data(stock_symbol, **context):
    date = f"{context['execution_date']:%Y-%m-%d}"  # read execution date from context
    end_point = (
        f"{BASE_URL}?function={STOCK_FN}&symbol={stock_symbol}"
        f"&apikey={API_KEY}&datatype=json"
    )
    print(f"Getting data from {end_point}...")
    r = requests.get(end_point)
    sleep(15)  # To avoid api limits
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
            [[date, np.nan, np.nan]], columns=['date', 'avg_num_trades', 'avg_price']
        )
    df['symbol'] = stock_symbol
    df = df[['date', 'symbol', 'avg_num_trades', 'avg_price']]
    return df


def _insert_daily_data(**context):
    task_instance = context['ti']
    # Get xcom for each upstream task
    dfs = []
    for company in STOCKS:
        dfs.append(task_instance.xcom_pull(task_ids=f'get_daily_data_{company}'))
    df = pd.concat(dfs, axis=0)
    sql_cli = SqLiteClient(SQL_DB)
    try:
        sql_cli.insert_from_frame(df, SQL_TABLE)
        print(f"Inserted {len(df)} records")
    except sqlalchemy.exc.IntegrityError:
        # You can avoid doing this by setting a trigger rule in the reports operator
        print("Data already exists! Nothing to do...")
    return


def _perform_daily_report(**context):
    date = f"{context['execution_date']:%Y-%m-%d}"
    sql_cli = SqLiteClient(SQL_DB)
    sql = SQL_REPORT.format(date=date)
    df = sql_cli.to_frame(sql).squeeze()
    msg = (
        f"Most traded action in {date} was {df['symbol']} with "
        f"an avg of {df['avg_num_trades']} trades per minute."
    )
    return msg


default_args = {
    'owner': 'pedro',
    'retries': 0,
    'start_date': datetime(2020, 4, 8),
    'email_on_failure': True,
    'email_on_retry': False,
    'email': ['pedro@muttdata.ai'],
}
with DAG(
    'stocks_extended', default_args=default_args, schedule_interval='0 4 * * *'
) as dag:

    create_table_if_not_exists = SqliteOperator(
        task_id='create_table_if_not_exists',
        sql=SQL_CREATE,
        sqlite_conn_id='sqlite_default',
    )

    # Create several task in loop
    get_data_task = {}
    for company, symbol in STOCKS.items():
        get_data_task[company] = PythonOperator(
            task_id=f'get_daily_data_{company}',
            python_callable=_get_stock_data,
            op_args=[symbol],
            provide_context=True,
        )

    insert_daily_data = PythonOperator(
        task_id='insert_daily_data',
        python_callable=_insert_daily_data,
        provide_context=True,
    )

    do_daily_report = PythonOperator(
        task_id='do_most_traded_report',
        python_callable=_perform_daily_report,
        provide_context=True,
    )

    send_report_email = EmailOperator(
        task_id='send_report_email',
        to='pedro@muttdata.ai',
        subject='Airflow Stocks Report {{ ds }}',
        html_content="{{ ti.xcom_pull(task_ids='do_most_traded_report') }}",
    )

    for company in STOCKS:
        upstream_task = create_table_if_not_exists
        task = get_data_task[company]
        upstream_task.set_downstream(task)
        task.set_downstream(insert_daily_data)
    insert_daily_data.set_downstream(do_daily_report)
    do_daily_report.set_downstream(send_report_email)
