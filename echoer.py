from flask import Flask
import sys

app = Flask(__name__)
host = "localhost"
port = int(sys.argv[1])

@app.route("/")
def hello():
    return f"hello from http://{host}:{port}"

@app.route("/health")
def health():
    return "I am alive"

if __name__ == '__main__':
    app.run(host=host, port=port, debug=True)