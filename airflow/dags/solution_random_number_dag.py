"""Random number dag extended."""
import logging
from datetime import datetime
from pathlib import Path

from airflow.models import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.dummy_operator import DummyOperator
from airflow.operators.python_operator import BranchPythonOperator, PythonOperator

STORE_DIR = Path(__file__).resolve().parent / 'tmp-files' / 'random-num'
Path.mkdir(STORE_DIR, exist_ok=True, parents=True)
# Add execution date to filename that stores random number
bash_cmd = (
    f'echo $(( ( RANDOM % 10 )  + 1 )) > {str(STORE_DIR)}/{{{{ ds_nodash }}}}.txt'
)


def _read_number_and_square(store_dir, **context):
    date = context['execution_date']  # read execution date from context
    fn = str(store_dir / f'{date:%Y%m%d}.txt')
    print(f"Reading {fn}...")  # add logging with print
    with open(fn, 'r') as f:
        n = f.readline()
    logging.info(f"Number read from file is: {n}")  # also adds logging
    n_sqr = int(n) ** 2
    return 'print_high' if n_sqr > 30 else 'print_low'  # return next task instance


def _print_high():
    return 'HIGH'


default_args = {'owner': 'pedro', 'retries': 0, 'start_date': datetime(2020, 4, 10)}
with DAG(
    'random_number_extended', default_args=default_args, schedule_interval='0 4 * * *'
) as dag:
    dummy_start_task = DummyOperator(task_id=f'dummy_start')
    generate_random_number = BashOperator(
        task_id='generate_random_number', bash_command=bash_cmd
    )
    # New branch operator
    read_num_and_square = BranchPythonOperator(
        task_id='read_number_and_square_it',
        python_callable=_read_number_and_square,
        op_args=[STORE_DIR],
        provide_context=True,  # pass task instance params to python callable
    )
    print_high = PythonOperator(task_id='print_high', python_callable=_print_high)
    print_low = BashOperator(task_id='print_low', bash_command='echo LOW')
    # Define tasks (normal path and then each branch)
    dummy_start_task >> generate_random_number >> read_num_and_square >> print_high
    read_num_and_square.set_downstream(print_low)
