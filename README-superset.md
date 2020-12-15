# Workshop de Big Data con Apache Spark [🇪🇸]
Material del Workshop de Big Data

## Creando un Dashboard con Superset

![Superset Dashboard Example](images/superset.png)

* Antes de acceder por primera vez a Superset inicializar la base de datos y crear las credenciales del usuario admin corriendo el siguiente comando: 
`./control-env.sh superset-init`
* Acceder a http://localhost:8088/ (utilizar las credenciales creadas en el primer paso).
* Agregar el database (Sources > Databases):
  - Database: `Workshop`
  - SQLAlchemy URI: `postgresql://workshop:w0rkzh0p@postgres/workshop`
  - OK
* Agregar tabla (Sources > Tables) :
  - Database: `workshop`
  - Table Name: `stocks`
* Create Slices & Dashboard [official docs](https://superset.incubator.apache.org/tutorial.html#creating-a-slice-and-dashboard)

![](images/superset-01.png)

![](images/superset-02.png)

![](images/superset-03.png)

![](images/superset-04.png)

![](images/superset-05.png)

![](images/superset-06.png)

![](images/superset-07.png)

![](images/superset-08.png)

![](images/superset-09.png)

![](images/superset-10.png)

![](images/superset-11.png)


## Sobre
Gustavo Arjones &copy; 2017-2020  
[arjon.es](https://arjon.es) | [LinkedIn](http://linkedin.com/in/arjones/) | [Twitter](https://twitter.com/arjones)
