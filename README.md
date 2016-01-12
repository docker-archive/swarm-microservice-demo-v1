# Swarm Example Cluster:  Web App

This is a sample Swarm cluster that illustrates how Swarm can be used as the foundation for a high-traffic microservice-architecture web application.  It is based on the Docker Cats-vs-Dogs voting example application, but re-architected to accomodate arbitrarily large scale through the use of parallel vote capture frontends and asynchronous background workers processing each vote.

# Use Case

Imagine that your company is planning to buy an ad during the Superbowl to drive people to a web survey about whether they prefer cats or dogs as pets.  (Perhaps your company sells pet food.)  You need to ensure that millions of people can vote nearly simultaneously without your website becoming unavailable.  You don't need exact real time results because you will announce them the next day, but you do need confidence that every vote will eventually get counted.

# Architecture

An Interlock load balancer (ha\_proxy plugin) sits in front of N web containers, each of which runs a simple Python (Flask) app that accepts votes and queues them into a redis container on the same node.  These N web (+ redis) nodes capture votes quickly and can scale up to any value of N since they operate independently.  Any level of expected voting traffic can thus be accomodated.  

Asynchronously, M background workers running on separate nodes scan through those N redis containers, dequeueing votes, de-duplicating them (to prevent double voting) and committing the results to a single postgres container that runs on its own node.

![Cluster Diagram](https://raw.githubusercontent.com/mgoelzer/swarm-demo-voting-app/master/cluster.png)

# Usage

This cluster can be deployed on either Vagrant and AWS.  A CloudFormation template (for AWS) and a Vangrantfile (for Vagrant) are included.

For AWS deployment:  start at `AWS/HOWTO.TXT`, which will guide you through other text files documenting the various steps to set up the cluster and the application on AWS.

For Vagrant deployment:  start at `Vagrant/HOWTO.TXT`, again following the pointers to other text files and scripts.

# TODO

Some details we need to finish up:
1.  Deploy with a docker-compose.yml (based on the various `docker run` and `docker build` lines in `Vagrant/HOWTO.TXT`)
2.  Remove Engine's key.json and make a new AMI.  Then we can simplify the Cloudformation template to remove the docker daemon restart lines in UserData
3.  Document of suggest as "extra credit":
  * There is a race condition with vote changes going into separate queues and workers not being able to know which came first.
  * Possible fixes:  don't allow them to change vote; timestamp each vote and enforce a 1 second delay between vote changes so that we can be sure we are only counting the user's "latest" vote.
  * For timestamp solution, all nodes need to sync clocks to a central time server (easiest) or someone needs to track their drift (harder).  For this simple example, we can just have host AMI sync'ing to `[0-3].us.pool.ntp.org`
