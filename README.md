# Swarm Example Cluster:  Web App

This is a sample Swarm cluster that illustrates how Swarm can be used as the foundation for a high-traffic microservice-architecture web application.  It is based on the Docker Cats-vs-Dogs voting example application, but re-architected to accomodate arbitrarily large scale through the use of parallel vote capture frontends and asynchronous background workers processing each vote.

# Architecture

An Interlock load balancer (ha\_proxy plugin) sits in front of N web containers, each of which runs a simple Python (Flask) app that accepts votes and queues them into a redis container on the same node.  These N web+redis nodes capture votes quickly.  By scaling up N, any level of expected traffic can be accomodated.  

Asynchronously, M background workers running on separate nodes scan through those N redis containers, dequeueing votes, de-duplicating them (to prevent double voting) and committing the results to a single postgres container that runs on its own node.

![Cluster Diagram](https://raw.githubusercontent.com/mgoelzer/swarm-demo-voting-app/master/cluster.png)

(Detailed cluster diagram with port numbers in `./cluster-detailed.png`)

# Usage

This cluster can be deployed on either Vagrant and AWS.  A CloudFormation template (for AWS) and a Vangrantfile (for Vagrant) are included.

For AWS deployment:  start at `AWS/HOWTO.TXT`, which will guide you through other text files documenting the various steps to set up the cluster and the application on AWS.

For Vagrant deployment:  start at `Vagrant/HOWTO.TXT`, again following the pointers to other text files and scripts.
