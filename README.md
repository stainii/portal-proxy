# portal-proxy
[![Build Status](https://server.stijnhooft.be/jenkins/buildStatus/icon?job=portal-proxy/master)](https://server.stijnhooft.be/jenkins/job/portal-proxy/job/master/)

A REST proxy for the portal, to make it possible to have 1 base url for every service. 
* Makes it easy to do load balancing
* Avoids cross-site-domain issues in the front-end 

Uses Spring Cloud Gateway.

### Release
#### Maven release
To release a module, this project makes use of the JGitflow plugin.
**Do use the Maven profile `-Pproduction`**.

More information can be found [here](https://gist.github.com/lemiorhan/97b4f827c08aed58a9d8).

At the moment, releases are made on a local machine. No Jenkins job has been made (yet).

#### Docker release
A Docker release is made, by running `mvn clean deploy -Pproduction` on the Maven release branch.
