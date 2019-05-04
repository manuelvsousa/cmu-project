from sqlalchemy import Column, Integer, Unicode, UnicodeText, String, Boolean, ForeignKey
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, scoped_session
from sqlalchemy.ext.declarative import declarative_base

from random import choice
from string import letters

import imp

config = imp.load_source('', 'config.py')

engine = create_engine('sqlite:///' + config.DATABASE_LOCATION, echo=True)
Base = declarative_base(bind=engine)
db_session = scoped_session(sessionmaker(autocommit=False,
                                         autoflush=False,
                                         bind=engine))

# TODO create relations instead of mongo like structure
class User(Base):
  query = db_session.query_property()
  __tablename__ = 'users'
  id = Column(Integer, primary_key=True)
  username = Column(Unicode(40), unique=True)
  password = Column(Unicode(40))
  dropbox = Column(Unicode(40))

  def __init__(self, username, password):
    self.username = username
    self.password = password
    self.dropbox = ""

class Album(Base):
  query = db_session.query_property()
  __tablename__ = 'album'
  id = Column(Integer, primary_key=True)
  album = Column(Unicode(40))
  username = Column(Unicode(40))
  url = Column(Unicode(100))

  def __init__(self, album, username,url):
    self.album = album
    self.username = username
    self.url = url

class Session(Base):
  query = db_session.query_property()
  __tablename__ = 'session'
  token = Column(Unicode(40), primary_key=True)
  username = Column(Unicode(40))
  createdts = Column(Unicode(40))
  valid = False

  def __init__(self, token, username, createdts):
    self.username = username
    self.token = token
    self.createdts = str(createdts)
    self.expired = False

Base.metadata.create_all()