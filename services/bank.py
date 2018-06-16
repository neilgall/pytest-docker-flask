#!/usr/bin/env python3
from flask import Flask, request
import json

app = Flask("bank")

_accounts = {
    "10011002": {
        "name": "Alice",
        "balance": 100
    },
    "10011003": {
        "name": "Bob",
        "balance": 200
    }
}

@app.route("/account-details", methods=["POST"])
def account_details():
    account = _accounts.get(request.form['account'])
    return json.dumps(account)

@app.route("/account-balance", methods=["POST"])
def account_balance():
    print(request.form)
    account = _accounts.get(request.form['account'])
    return str(account["balance"])

if __name__ == "__main__":
    app.run("0.0.0.0", port=5000, debug=True)
