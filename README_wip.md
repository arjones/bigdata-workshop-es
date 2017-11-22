# Workshop de Big Data con Apache Spark [🇪🇸]
Este repositorio contiene el workshop de Big Data con Apache Spark.

## Setup & Configuración
* La plataforma funciona con [Docker](https://docs.docker.com/engine/installation/) instalarlo para seguir.

* Correr script de Docker Compose: `docker-compose up -d`
* Crear el usuario y contraseña de Superset: `docker exec -it superset superset-init`
* Check the portal with links to all services: http://localhost:8888/



`docker exec -it bigdataworkshop_master_1  spark-shell --master spark://master:7077`


```bash
kafka-topics \
  --zookeeper localhost:2181 \
  --partitions 4 \
  --replication-factor 1 \
  --create \
  --topic stocks


kafka-console-producer \
  --broker-list localhost:9092 \
  --topic stocks
 ```



## Acknowledge
This workshop is heavily inspired by these material.

* https://github.com/caroljmcdonald/spark-ml-randomforest-creditrisk
* https://github.com/hipic/biz_data_LA#section-22-clustering-food-related-business-in-yelp-using-spark-machine-learning-2017
* https://community.hortonworks.com/articles/53903/spark-machine-learning-pipeline-by-example.html
* https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-mllib/spark-mllib-pipelines-example-classification.html

## Contribuciones
Si te gustaría contribuir para ese material, hacelo a través de un pull request.
