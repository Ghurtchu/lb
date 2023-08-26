# In progress

Current implementation uses round robin logic for distributing requests.

For testing locally:

start the load balancer:
- `./lb`

run a few backends on different ports:
- `./be 8081`
- `./be 8082`
- `./be 8083`

observe output:

![My Image](example.png)