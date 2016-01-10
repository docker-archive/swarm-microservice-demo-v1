# swarm-demo-voting-app

This is a sample Swarm cluster to run a large-scale deployment of the Docker Cats-vs-Dogs voting application.

An Interlock load balancer (ha\_proxy) sits in front of N web containers, each of which runs a simple Python (Flask) app that accepts votes and queues them into a redis container running on the same node.  Asynchronously, M background workers scan through those N redis containers, dequeueing votes, deduplicating them (to prevent double voting) and committing the results to a single postgres container that runs on its own node.

This is tested on both Vagrant and AWS.  A CloudFormation template (AWS) and a Vangrantfile (Vagrant) are included.

For AWS deployment:  follow `AWS/HOWTO.TXT`

For Vagrant deployment:  follow `Vagrant/HOWTO.TXT`
