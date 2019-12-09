# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshop de Big Data

## Creando un Dashboard con Superset

![Superset Dashboard Example](./images/superset.png)

* Acceder a http://localhost:8088/, user: `admin`, pass: `superset`.
* Agregar el database (Sources > Databases):
  - Database: `Workshop`
  - SQLAlchemy URI: `postgresql://workshop:w0rkzh0p@postgres/workshop`
  - OK
* Agregar tabla (Sources > Tables) :
  - Database: `workshop`
  - Table Name: `stocks`
* Create Slices & Dashboard [official docs](https://superset.incubator.apache.org/tutorial.html#creating-a-slice-and-dashboard)

![](images/ss-01.png)

![](images/ss-02.png)

![](images/ss-03.png)

![](images/ss-04.png)

![](images/ss-05.png)

![](images/ss-06.png)

![](images/ss-07.png)


## Sobre
Gustavo Arjones &copy; 2017-2019  
[arjon.es](https://arjon.es) | [LinkedIn](http://linkedin.com/in/arjones/) | [Twitter](https://twitter.com/arjones)
