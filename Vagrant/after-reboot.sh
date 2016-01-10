#!/bin/sh

docker -H=tcp://192.168.33.11:2375 run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap

docker -H=tcp://192.168.33.20:2375 run -d swarm join --advertise=192.168.33.20:2375 consul://192.168.33.11:8500/
docker -H=tcp://192.168.33.200:2375 run -d swarm join --advertise=192.168.33.200:2375 consul://192.168.33.11:8500/
docker -H=tcp://192.168.33.251:2375 run -d swarm join --advertise=192.168.33.251:2375 consul://192.168.33.11:8500/

docker -H=tcp://192.168.33.11:2375 run -d -p 3375:2375 swarm manage consul://192.168.33.11:8500/

##
## Commands to test the cluster
##
export DOCKER_HOST="tcp://192.168.33.11:3375"
docker info
docker network create -d overlay mynet
docker run -d --name web --net mynet nginx
docker run -itd --name shell1 --net mynet alpine /bin/sh
docker attach shell1
$ ping web
docker stop web shell1 ; docker rm web shell1
