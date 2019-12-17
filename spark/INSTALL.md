# Install notes

## Trying new libs
A quick & dirty way to try new libs in pySpark would be:

```
cd jupyter/notebook
docker run -it -p8888:8888 -w /notebook -v $PWD:/notebook arjones/pyspark:2.4.4 bash

apt-get update && \
  apt-get --no-install-recommends --no-install-suggests install -y \
  python3-pip && \
  pip3 install gensim

```

After detecting all dependencies, you can include it on `Dockerfile` definitions and rebuild the image.