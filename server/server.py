#!/usr/local/bin/python2
import database as db
import time
import imp
from functools import wraps  # TODO Maybe pip install
from flask import Flask, request, make_response, abort, jsonify
import random
import string
import time

TOKEN_LEN = 128


app = Flask(__name__)

config = imp.load_source('', 'config.py')


@app.route('/user/register', methods=['POST'])
def addUser():
    username = str(request.json.get('username', ""))
    password = str(request.json.get('password', ""))
    passwordConfirmation = str(request.json.get('passwordConfirmation', ""))

    if(username == "" or password == "" or passwordConfirmation == ""):
        resp = jsonify(
            success=False, message="username or password or passwordConfirmation is empty")
        return make_response(resp, 400)

    if(password != passwordConfirmation):
        resp = jsonify(success=False, message="Passwords do not match")
        return make_response(resp, 400)

    if db.User.query.filter_by(username=username).count() > 0:
        resp = jsonify(success=False, message="username already exists")
        return make_response(resp, 400)

    u = db.User(username, password)
    db.db_session.add(u)
    db.db_session.commit()
    resp = jsonify(success=True)
    return make_response(resp, 201)


@app.route('/user/register/drive', methods=['POST'])
def addDropbox():
    token = str(request.json.get('token', ""))
    dropbox = str(request.json.get('dropbox', ""))
    print "fodasse fodasse" 
    print token,dropbox

    if(token == "" or dropbox == ""):
        resp = jsonify(success=False, message="token or dropbox token is empty")
        return make_response(resp, 400)

    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)
    s = db.Session.query.filter_by(token=token).first()
    u = db.User.query.filter_by(username=s.username).first()
    u.dropbox = dropbox
    db.db_session.commit()
    resp = jsonify(success=True)
    return make_response(resp, 201)


@app.route('/album/create', methods=['POST'])
def createAlbum():
    token = str(request.json.get('token', ""))
    albumName = str(request.json.get('albumName', ""))
    link = str(request.json.get('link', ""))
    print token,albumName,link
    if(token == "" or albumName == ""):
        resp = jsonify(success=False, message="token or albumName is empty")
        return make_response(resp, 400)
    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)
    s = db.Session.query.filter_by(token=token).first()
    u = db.User.query.filter_by(username=s.username).first()
    if db.Album.query.filter_by(album=albumName).count() >= 1:
        resp = jsonify(success=False, message="album already exists")
        return make_response(resp, 201) 
        
    a = db.Album(albumName, u.username, link)
    db.db_session.add(a)
    db.db_session.commit()
    resp = jsonify(success=True)
    return make_response(resp, 201)



@app.route('/album/check', methods=['POST'])
def checkAlbum():
    token = str(request.json.get('token', ""))
    albumName = str(request.json.get('albumName', ""))
    if(token == "" or albumName == ""):
        resp = jsonify(success=False, message="token or albumName is empty")
        return make_response(resp, 400)
    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)
    s = db.Session.query.filter_by(token=token).first()
    u = db.User.query.filter_by(username=s.username).first()

    if db.Album.query.filter_by(album=albumName).count() == 1:
        if db.Album.query.filter_by(album=albumName,username=u.username).count() >= 1:
            resp = jsonify(success=False, message="You are in this album already")
        else:
            resp = jsonify(success=False, message="album already exists")
        return make_response(resp, 409) 
        
    resp = jsonify(success=True)
    return make_response(resp, 201)

@app.route('/album/list', methods=['POST'])
def getAlbums():
    token = str(request.json.get('token', ""))

    if(token == ""):
        resp = jsonify(success=False, message="token is empty")
        return make_response(resp, 400)

    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)
    s = db.Session.query.filter_by(token=token).first()
    albums = db.Album.query.filter_by(username=s.username)
    resp = jsonify(success=True,albums = [album.album for album in albums])
    return make_response(resp, 201)


@app.route('/album/user/list', methods=['POST'])
def getAlbumsUsers():
    token = str(request.json.get('token', ""))
    albumName = str(request.json.get('albumName', ""))
    if(token == "" or albumName == ""):
        resp = jsonify(success=False, message="token or albumName is empty")
        return make_response(resp, 400)
    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)

    s = db.Session.query.filter_by(token=token).first()
    u = db.User.query.filter_by(username=s.username).first()

    if db.Album.query.filter_by(username=s.username,album=albumName).count() != 1:
        resp = jsonify(success=False, message=s.username + " does not have permition to check users in album: " + albumName)
        return make_response(resp, 403)

    albums = db.Album.query.filter_by(album=albumName)
    print [album.username for album in albums]
    return jsonify(users = [album.username for album in albums], success=True)



@app.route('/user/album/list', methods=['POST'])
def getUserAlbums():
    token = str(request.json.get('token', ""))
    user = str(request.json.get('user', ""))
    print token,user
    if(token == "" or user == ""):
        resp = jsonify(success=False, message="token or user is empty")
        return make_response(resp, 400)

    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)

    s = db.Session.query.filter_by(token=token).first()
    u = db.User.query.filter_by(username=s.username).first()

    albums = db.Album.query.filter_by(username=user)
    return jsonify(albums = [album.album for album in albums], success=True)

@app.route('/album/user/add', methods=['POST'])
def addUserAlbum():
    token = str(request.json.get('token', ""))
    user = str(request.json.get('user', ""))
    albumName = str(request.json.get('albumName', ""))
    print token,user,albumName
    if(token == "" or user == ""):
        resp = jsonify(success=False, message="token or albumName is empty")
        return make_response(resp, 400)
    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)

    s = db.Session.query.filter_by(token=token).first()
    u = db.User.query.filter_by(username=s.username).first()

    if db.Album.query.filter_by(username=s.username,album=albumName).count() != 1:
        resp = jsonify(success=False, message=user + " does not have permition to add users in album: " + albumName)
        return make_response(resp, 403)

    if(s.username == user):
        resp = jsonify(success=False, message="You are already in this album")
        return make_response(resp, 400)     

    if db.User.query.filter_by(username=user).count() != 1:
        print "caralhooooooooo"
        resp = jsonify(success=False, message=user + " does not exist")
        return make_response(resp, 404)

    a = db.Album(albumName, user, "")
    db.db_session.add(a)
    db.db_session.commit()
    return jsonify(success=True)


@app.route('/user/login', methods=['POST'])
def loginUser():
    username = str(request.json.get('username', ""))
    password = str(request.json.get('password', ""))

    if(username == "" or password == ""):
        resp = jsonify(success=False, message="username or password is empty")
        return make_response(resp, 400)

    if db.User.query.filter_by(username=username, password=password).count() != 1:
        resp = jsonify(
            success=False, message="username not found or password invalid")
        return make_response(resp, 404)

    token = ''.join(random.choice(string.ascii_uppercase + string.digits)
                    for _ in range(TOKEN_LEN))
    print "generated token: ", token
    timestamp = time.time()
    s = db.Session(token, username, timestamp)
    db.db_session.add(s)
    db.db_session.commit()
    u = db.User.query.filter_by(username=s.username).first()
    resp = jsonify(success=True, token=token, dropbox = u.dropbox)
    return make_response(resp, 201)


@app.route('/user/logout', methods=['POST'])
def logoutUser():
    token = str(request.json.get('token', ""))

    if(token == ""):
        resp = jsonify(success=False, message="token is empty")
        return make_response(resp, 400)

    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)
    s = db.Session.query.filter_by(token=token).first()
    s.expired = True
    db.db_session.commit()
    resp = jsonify(success=True, token=token)
    return make_response(resp, 201)


@app.route('/user/list', methods=['POST'])
def listUser():
    token = str(request.json.get('token', ""))
    print token
    if db.Session.query.filter_by(token=token).count() != 1:
        resp = jsonify(success=False, message="token does not exist")
        return make_response(resp, 404)
    users = db.User.query.all()
    print [user.username for user in users]
    return jsonify(users = [user.username for user in users], success=True)

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
