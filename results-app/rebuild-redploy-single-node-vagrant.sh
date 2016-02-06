#!/bin/sh

docker stop results-app ; docker rm results-app ; docker rmi results-app ; docker -H tcp://192.168.33.251:2375 build -t results-app . ; docker run --restart=unless-stopped --env="constraint:node==store" -p 80:80 -d --name results-app --net mynet results-app
