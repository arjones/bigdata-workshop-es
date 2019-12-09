# Instrucciones de Instalación

**IMPORTANTE:** Cualquier opción de instalación requeriere por lo menos 8Gb de RAM para un correcto funcionamiento.

## Utilizando Windows

Si su computadora es Windows debe utilizar la Virtual Machine, algunas computadoras más antiguas no soporta virtualización, por lo cual [VirtualBox](https://www.virtualbox.org/) no funciona. Tampoco funciona adecuadamente **Docker on Windows**.

Se puede generar una Virtual Machine de cero siguiendo las instrucciones acá: [Virtual Box - INSTALL](./vm). La virtual machine completa pesa 15Gb y 5.2Gb cuando comprimida con `gzip -9`.

## Utilizando MacOSX

Para compilar y correr el codigo adecuadamente en MacOSX es necesario instalar varias dependencias, acá pueden encontrar las instrucciones para instalar todas las dependencias necesarias: [Setting up Macbook Pro for Development](https://arjon.es/2019/setting-up-macbook-pro-for-development/)

Al finalizar la instalación clonar el repositorio:

```shell
git clone https://github.com/arjones/bigdata-workshop-es.git

cd bigdata-workshop-es

./control-env.sh start
```

## Sobre
Gustavo Arjones &copy; 2017-2019  
[arjon.es](https://arjon.es) | [LinkedIn](http://linkedin.com/in/arjones/) | [Twitter](https://twitter.com/arjones)
