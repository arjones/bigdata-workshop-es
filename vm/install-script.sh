# Enables sudo without passwd
echo "%sudo ALL=(ALL) NOPASSWD: ALL" | sudo tee -a /etc/sudoers

# SSHD
sudo apt-get update
sudo apt-get install -y openssh-server

# Basics
sudo apt-get install -y \
  git \
  openjdk-8-jdk \
  maven \
  scala

# SBT
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install -y sbt

# Docker
sudo apt-get install -y \
  apt-transport-https \
  ca-certificates \
  curl \
  software-properties-common

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

sudo apt-key fingerprint 0EBFCD88

sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"

sudo apt-get update
sudo apt-get install -y docker-ce

sudo groupadd docker
sudo usermod -aG docker "${USER}"

# Docker Compose
sudo curl -L https://github.com/docker/compose/releases/download/1.24.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

#############################################
#
# Course Material
cd ~
git clone https://github.com/arjones/bigdata-workshop-es.git

cd bigdata-workshop-es
docker-compose pull
