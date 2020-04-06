#!/bin/bash

# SSHD
sudo apt-get update

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

sudo systemctl enable docker

# Docker Compose
sudo curl -L https://github.com/docker/compose/releases/download/1.24.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Cleanup
sudo apt-get -y autoremove

#############################################
#
echo '===================================='
echo
echo 'You need to logout and restart again'
echo
echo '===================================='
