"""Word count dag."""
from datetime import datetime
from pathlib import Path

import pandas as pd
from airflow.hooks.postgres_hook import PostgresHook
from airflow.models import DAG

# from airflow.operators.docker_operator import DockerOperator
from airflow.operators.postgres_operator import PostgresOperator
from airflow.operators.python_operator import PythonOperator

STORE_DIR = Path(__file__).resolve().parent

CONNECTION_ID = 'postgres_local'
SQL_DB = "word_count"
SQL_TABLE = 'word_count'
SQL_CREATE = f"""
CREATE TABLE IF NOT EXISTS {SQL_TABLE} (
date TEXT,
word TEXT,
count REAL,
UNIQUE(date,word)
)
"""


def _insert_file_to_sql(**context):
    df_result = pd.read_csv(f"{STORE_DIR}/output.csv", names=["word", "count"])
    df_result["date"] = context["ds"]
    if not df_result.empty:
        for c in df_result.columns:
            if c == 'count':
                df_result[c] = df_result[c].astype(float)
    df_result = df_result.squeeze()  # squeezing single row dataframe

    df_tuple = [(df_result["date"], df_result["word"], df_result["count"])]

    hook = PostgresHook(postgres_conn_id=CONNECTION_ID)
    hook.insert_rows(SQL_TABLE, df_tuple)


default_args = {'owner': 'pedro', 'retries': 0, 'start_date': datetime(2020, 12, 14)}
with DAG('word_count', default_args=default_args, schedule_interval='0 0 * * *') as dag:
    create_table_if_not_exists = PostgresOperator(
        task_id='create_table_if_not_exists',
        sql=SQL_CREATE,
        postgres_conn_id=CONNECTION_ID,
    )
    # spark_job = DockerOperator(
    # task_id='spark_job',
    # image='bde2020/spark-master:latest',
    # api_version='auto',
    # auto_remove=True,
    # environment={'PYSPARK_PYTHON': "python3", 'SPARK_HOME': "/spark"},
    # volumes=[f'{STORE_DIR}:/spark-job'],
    # command='/spark/bin/spark-submit --master local[*] /spark-job/spark_job.py',
    # docker_url='unix://var/run/docker.sock',
    # network_mode='bridge',
    # )
    insert_file_to_sql = PythonOperator(
        task_id='file_to_sql',
        python_callable=_insert_file_to_sql,
        provide_context=True,
    )
