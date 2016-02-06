#!/bin/sh

vagrant up

vagrant ssh -c "sudo apt-get install -y linux-image-generic-lts-utopic && sudo reboot" master
vagrant ssh -c "sudo apt-get install -y linux-image-generic-lts-utopic && sudo reboot" interlock
vagrant ssh -c "sudo apt-get install -y linux-image-generic-lts-utopic && sudo reboot" frontend01
vagrant ssh -c "sudo apt-get install -y linux-image-generic-lts-utopic && sudo reboot" worker01
vagrant ssh -c "sudo apt-get install -y linux-image-generic-lts-utopic && sudo reboot" store

