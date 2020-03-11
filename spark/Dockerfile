FROM openjdk:8-jre-slim

WORKDIR /app

ARG SPARK_VERSION=2.4.5

ENV SPARK_HOME /opt/spark

RUN apt-get update && \
  apt-get install -y wget ca-certificates procps && \
  wget http://apache.dattatec.com/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop2.7.tgz -O - | tar zx -C /opt && \
  ln -s /opt/spark-${SPARK_VERSION}-bin-hadoop2.7 /opt/spark && \
  sed 's/INFO/ERROR/g' /opt/spark/conf/log4j.properties.template > /opt/spark/conf/log4j.properties && \
  echo "\nexport PATH=\${PATH}:/opt/spark/bin" >> /etc/bash.bashrc && \
  echo "\nexport SPARK_NO_DAEMONIZE=1" >> /etc/bash.bashrc && \
  rm -rf /var/lib/apt/lists/*

CMD ["/opt/spark/bin/spark-shell"]

EXPOSE 8080 8081 4040 4041
