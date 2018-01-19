# Creando VM de cero

* Download una version de Ubuntu Desktop

`wget -c http://releases.ubuntu.com/16.04/ubuntu-16.04.3-desktop-amd64.iso`

## Configurar VirtualBox

* Instalar [VirtualBox](https://www.virtualbox.org)
* Configurar disco de **>= 20Gb** y **8Gb RAM**
* Configurar red: Settings > Network > **Port Forwarding**

![virtualbox-port-forwarding](virtualbox-port-forwarding.png)

* Instalar la VM
* Abrir la terminal y ejecutar los comandos de [install-script.sh](install-script.sh)

## Acceso por SSH

* Despues de habilitar SSHD (corriendo script arriba) se puede acceder a la VM por SSH: `ssh analyst@localhost -p 2222`
