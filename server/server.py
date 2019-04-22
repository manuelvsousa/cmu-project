#!/usr/local/bin/python2
import database as db
import time
import imp
from functools import wraps # TODO Maybe pip install
from flask import Flask, request, make_response, abort, jsonify
import random
import string
import time

TOKEN_LEN = 128


app = Flask(__name__)

config = imp.load_source('', 'config.py')

@app.route('/user/register', methods=['POST']) 
def addUser():
  print request.get_json()
  print "fodassse"
  username = str(request.json.get('username', ""))
  password = str(request.json.get('password', ""))
  passwordConfirmation = str(request.json.get('passwordConfirmation', ""))

  if(username == "" or password == "" or passwordConfirmation == ""):
    resp = jsonify(success=False,message = "username or password or passwordConfirmation is empty")
    return make_response(resp,400)

  if(password != passwordConfirmation):
    resp = jsonify(success=False,message = "Passwords do not match")
    return make_response(resp,400)

  if db.User.query.filter_by(username = username).count() > 0:
    resp = jsonify(success=False,message = "username already exists")
    return make_response(resp,400)

  u = db.User(username,password)
  db.db_session.add(u)
  db.db_session.commit()
  resp = jsonify(success=True)
  return make_response(resp,201)

@app.route('/user/register/drive', methods=['POST']) 
def addDrive():
  token = str(request.json.get('token', ""))
  drive = str(request.json.get('drive', ""))

  if(token == "" or drive == ""):
    resp = jsonify(success=False,message = "token or drive is empty")
    return make_response(resp,400)

  if db.User.query.filter_by(username = username).count() > 0:
    resp = jsonify(success=False,message = "username already exists")
    return make_response(resp,400)

  u = db.User(username,password)
  db.db_session.add(u)
  db.db_session.commit()
  resp = jsonify(success=True)
  return make_response(resp,201)

@app.route('/user/login', methods=['POST']) 
def loginUser():
  username = str(request.json.get('username', ""))
  password = str(request.json.get('password', ""))

  if(username == "" or password == ""):
    resp = jsonify(success=False,message = "username or password is empty")
    return make_response(resp,400)

  if db.User.query.filter_by(username = username, password = password).count() != 1:
    resp = jsonify(success=False,message = "username not found or password invalid")
    return make_response(resp,404)

  token = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(TOKEN_LEN))
  ts = time.time()
  s = db.Session(token,ts)
  db.db_session.add(s)
  db.db_session.commit()

  resp = jsonify(success=True, token = token)
  return make_response(resp,201)

@app.route('/user/list', methods=['GET']) 
def listUser():
  print repr(request.json)
  users = db.User.query.all()
  return jsonify([user.username for user in users])

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)