# Apache Spark Image

How to build and push the Spark Image:

```bash
SPARK_VERSION=2.1.3

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
