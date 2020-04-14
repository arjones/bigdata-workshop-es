"""Random number dag."""
from datetime import datetime
from pathlib import Path

from airflow.models import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.dummy_operator import DummyOperator
from airflow.operators.python_operator import PythonOperator

STORE_DIR = Path(__file__).resolve().parent / 'tmp-files' / 'random-num'
Path.mkdir(STORE_DIR, exist_ok=True, parents=True)
bash_cmd = f"echo $(( ( RANDOM % 10 )  + 1 )) > {str(STORE_DIR / 'random_number.txt')}"


def _read_number_and_square(store_dir):
    fn = str(store_dir / 'random_number.txt')
    with open(fn, 'r') as f:
        n = f.readline()
    return int(n) ** 2


default_args = {'owner': 'pedro', 'retries': 0, 'start_date': datetime(2020, 4, 10)}
with DAG(
    'random_number', default_args=default_args, schedule_interval='0 4 * * *'
) as dag:
    dummy_start_task = DummyOperator(task_id=f'dummy_start')
    generate_random_number = BashOperator(
        task_id='generate_random_number', bash_command=bash_cmd
    )
    read_num_and_square = PythonOperator(
        task_id='read_number_and_square_it',
        python_callable=_read_number_and_square,
        op_args=[STORE_DIR],
    )
    dummy_start_task >> generate_random_number >> read_num_and_square
