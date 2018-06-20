#!/usr/bin/env python3
from flask import Flask

app = Flask("hello")

@app.route("/hello")
def hello():
    return "ok"

app.run(host='0.0.0.0')


