# portal-proxy
[![Build Status](https://server.stijnhooft.be/jenkins/buildStatus/icon?job=portal-proxy/master)](https://server.stijnhooft.be/jenkins/job/portal-proxy/job/master/)

A REST proxy for the portal, to make it possible to have 1 base url for every service. 
* Makes it easy to do load balancing
* Avoids cross-site-domain issues in the front-end 

Uses Spring Cloud Zuul.
