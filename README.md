# In progress

Current implementation uses round robin logic for distributing requests.

Also, it periodically checks the health of servers and if one of them is dead it stops sending requests to it.

As soon as dead server's back up and running it will start sending requests to it.

Comprehensive testing instructions:

Configure load balancer in `src/main/resources/application.conf`:
![My Image](screenshots/config.png)


Run a few backends on different ports:
- `./be 8081`
![My Image](screenshots/8081.png)
- `./be 8082`
![My Image](screenshots/8082.png)
- `./be 8083`
![My Image](screenshots/8083.png)


Run the load balancer and observe health check logs:
- `./lb`
![My Image](screenshots/lb-healthcheck-logs.png)

Ping the load balancer and observe responses from different servers:
- `curl localhost:8080`
![My Image](screenshots/lb-curl.png)

Stop one of the backends (8081):
![My Image](screenshots/stop-8081.png)

Observe the load balancer adjust distributing requests to only two servers:
![My Image](screenshots/lb-adjusted-1.png)

Run the server with port 8081 again:
- `./be 8081`
![My Image](screenshots/8081.png)

Observe the load balancer adjust distributing requests to all three servers again:
- `curl localhost:8080`
![My Image](screenshots/final.png)