#!/bin/sh

docker stop web01
docker rm web01
docker rmi web-vote-app
docker -H tcp://192.168.33.20:2375 build -t web-vote-app .

