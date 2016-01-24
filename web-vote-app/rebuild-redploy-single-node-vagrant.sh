#!/bin/sh

docker stop web01 ; docker rm web01 ; docker rmi web-vote-app ; docker -H tcp://192.168.33.20:2375 build -t web-vote-app . ; docker run --restart=unless-stopped --env="constraint:node==web01" -d -p 5000:80 -e WEB_VOTE_NUMBER='01' --name web01 --net mynet --hostname votingapp.local web-vote-app

