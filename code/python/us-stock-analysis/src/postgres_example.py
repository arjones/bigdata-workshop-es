import psycopg2

try:
    conn = psycopg2.connect(
        user='workshop',
        password='w0rkzh0p',
        host='postgres',
        database='workshop')

    # Open a cursor to perform database operations
    cursor = conn.cursor()

    cursor.execute('SELECT 1;')
    data = cursor.fetchone()
    print(data)

except psycopg2.Error as e:
    print(e)

finally:
    cursor.close()
    conn.close()
