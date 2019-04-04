#!/usr/local/bin/python2
from OpenSSL import SSL
import database as db
import time
import imp
from functools import wraps # TODO Maybe pip install
from flask import Flask, request, make_response, abort, jsonify
app = Flask(__name__)

config = imp.load_source('', 'config.py')

@app.errorhandler(404)
def not_found(error):
  return make_response(jsonify({'error': 'Not found'}), 404)

@app.route('/user/register', methods=['POST']) 
def addUser():
  username = str(request.json.get('username', ""))
  password = str(request.json.get('password', ""))
  print db.User.query.filter_by(username = username)
  if db.User.query.filter_by(username = username).count() > 0:
    resp = jsonify(success=False)
    return make_response(resp,400)

  u = db.User(username,password)
  db.db_session.add(u)
  db.db_session.commit()
  resp = jsonify(success=True)
  return make_response(resp,201)

# @app.route('/user/login', methods=['POST']) 
# def loginUser():
#   username = str(request.json.get('username', ""))
#   password = str(request.json.get('password', ""))
  
#   u = db.User(username,password,email)
#   db.db_session.add(u)
#   db.db_session.commit()
#   return "ACK"

@app.route('/user/list', methods=['GET']) 
def listUser():
  print repr(request.json)
  users = db.User.query.all()
  return jsonify([user.name for user in users])

if __name__ == '__main__':
    app.run(debug=True)