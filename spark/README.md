# Apache Spark Image

How to build and push the Spark Image:

```bash
export SPARK_VERSION=2.4.5

docker build \
  --build-arg SPARK_VERSION=${SPARK_VERSION} \
  -t arjones/spark:${SPARK_VERSION} .

docker build \
  -f Dockerfile.pyspark \
  --build-arg SPARK_VERSION=${SPARK_VERSION} \
  -t arjones/pyspark:${SPARK_VERSION} .

docker push arjones/spark:${SPARK_VERSION}
docker push arjones/pyspark:${SPARK_VERSION}
```

## Sobre
Gustavo Arjones &copy; 2017-2020  
[arjon.es](https://arjon.es) | [LinkedIn](http://linkedin.com/in/arjones/) | [Twitter](https://twitter.com/arjones)
