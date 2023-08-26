# In progress

Current implementation uses round robin logic for distributing requests.

For testing,

run the load balancer:
- `./lb`

run the servers:
- `python3 echoer.py 8081`
- `python3 echoer.py 8082`
- `python3 echoer.py 8083`