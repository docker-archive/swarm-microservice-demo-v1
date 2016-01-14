#!/bin/sh

docker stop worker01
docker rm worker01
docker rmi vote-worker
docker -H tcp://192.168.33.200:2375 build -t vote-worker .

