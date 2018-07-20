#!/usr/bin/env python3
from flask import Flask

app = Flask("myValue")

@app.route("/myValue")
def hello():
    return "bar"

app.run(host='0.0.0.0', debug=True)


