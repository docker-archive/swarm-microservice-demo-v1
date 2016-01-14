#!/bin/sh

##
## Commands to create the Swarm layer containers
##

docker -H=tcp://192.168.33.11:2375 run --restart=unless-stopped -d -p 8500:8500 -h consul progrium/consul -server -bootstrap

docker -H=tcp://192.168.33.20:2375 run --restart=unless-stopped -d swarm join --advertise=192.168.33.20:2375 consul://192.168.33.11:8500/
docker -H=tcp://192.168.33.21:2375 run --restart=unless-stopped -d swarm join --advertise=192.168.33.21:2375 consul://192.168.33.11:8500/
[et cetera]

docker -H=tcp://192.168.33.200:2375 run --restart=unless-stopped -d swarm join --advertise=192.168.33.200:2375 consul://192.168.33.11:8500/
docker -H=tcp://192.168.33.201:2375 run --restart=unless-stopped -d swarm join --advertise=192.168.33.201:2375 consul://192.168.33.11:8500/
[et cetera]

docker -H=tcp://192.168.33.250:2375 run --restart=unless-stopped -d swarm join --advertise=192.168.33.250:2375 consul://192.168.33.11:8500/

docker -H=tcp://192.168.33.11:2375 run --restart=unless-stopped -d -p 3375:2375 swarm manage consul://192.168.33.11:8500/

export DOCKER_HOST="tcp://192.168.33.11:3375"
docker network create --driver overlay mynet

##
## OPTIONAL:  Commands to test the cluster
##
docker info
docker run -d --name web --net mynet nginx
docker run -itd --name shell1 --net mynet alpine /bin/sh
docker attach shell1
$ ping web
docker stop web shell1 ; docker rm web shell1
