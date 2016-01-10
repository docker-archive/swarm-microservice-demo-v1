# swarm-demo-voting-app

This is a sample Swarm cluster that illustrates how Swarm can be used as the foundation for a high-traffic microservice-architecture web application.  It is based on the Docker Cats-vs-Dogs voting example application, but re-architected to accomodate arbitrarily large scale through the use of parallel vote capture frontends and asynchronous background workers processing the votes.

# Architecture

An Interlock load balancer (ha\_proxy) sits in front of N web containers, each of which runs a simple Python (Flask) app that accepts votes and queues them into a redis container on the same node.  These N web+redis containers capture votes quickly and by scaling up N, any level of expected traffic can be accomodated.  

Asynchronously, M background workers running on separate nodes scan through those N redis containers, dequeueing votes, de-duplicating them (to prevent double voting) and committing the results to a single postgres container that runs on its own node.

*TODO:  DIAGRAM HERE!*

# Usage

This cluster can be deployed on either Vagrant and AWS.  A CloudFormation template (AWS) and a Vangrantfile (Vagrant) are included.

For AWS deployment:  follow `AWS/HOWTO.TXT` and use the `AWS/cloudformation.json` file.

For Vagrant deployment:  follow `Vagrant/HOWTO.TXT`.
