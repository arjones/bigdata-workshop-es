FROM openjdk:8-jre-slim

RUN mkdir /app && \
  apt-get update && \
  apt-get install -y wget ca-certificates procps && \
  wget http://apache.dattatec.com/spark/spark-2.1.2/spark-2.1.2-bin-hadoop2.7.tgz -O - | tar zx -C /opt && \
  ln -s /opt/spark-2.1.2-bin-hadoop2.7 /opt/spark && \
  sed 's/INFO/WARN/g' /opt/spark/conf/log4j.properties.template > /opt/spark/conf/log4j.properties && \
  echo "\nexport PATH=\${PATH}:/opt/spark/bin" >> /etc/bash.bashrc && \
  echo "\nexport SPARK_NO_DAEMONIZE=1" >> /etc/bash.bashrc && \
  rm -rf /var/lib/apt/lists/*

WORKDIR /app

CMD ["/opt/spark/bin/spark-shell"]

EXPOSE 8080 8081 4040 4041
