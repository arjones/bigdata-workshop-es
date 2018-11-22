FROM openjdk:8-jre-slim

WORKDIR /app

ARG SPARK_VERSION=2.1.3

RUN apt-get update && \
  apt-get install -y wget ca-certificates procps && \
  wget http://apache.dattatec.com/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop2.7.tgz -O - | tar zx -C /opt && \
  ln -s /opt/spark-${SPARK_VERSION}-bin-hadoop2.7 /opt/spark && \
  sed 's/INFO/WARN/g' /opt/spark/conf/log4j.properties.template > /opt/spark/conf/log4j.properties && \
  echo "\nexport PATH=\${PATH}:/opt/spark/bin" >> /etc/bash.bashrc && \
  echo "\nexport SPARK_NO_DAEMONIZE=1" >> /etc/bash.bashrc

# pySpark support
RUN apt-get install -y python3 && \
  update-alternatives --install /usr/bin/python python /usr/bin/python3.5 10 && \
  rm -rf /var/lib/apt/lists/*

CMD ["/opt/spark/bin/spark-shell"]

EXPOSE 8080 8081 4040 4041
