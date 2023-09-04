from flask import Flask
import sys

app = Flask(__name__)
host = "localhost"
port = int(sys.argv[1])

@app.route("/")
def hello():
    return f"hello from {url(host, port)}"

@app.route("/health")
def health():
    return f"{url(host, port)} is alive and healthy ^_^"

def url(host, port):
    return f"http://{host}:{port}"

if __name__ == '__main__':
    app.run(host=host, port=port, debug=True)