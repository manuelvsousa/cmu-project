from sqlalchemy import Column, Integer, Unicode, UnicodeText, String, Boolean
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

class User(Base):
  query = db_session.query_property()
  __tablename__ = 'users'
  id = Column(Integer, primary_key=True)
  username = Column(Unicode(40), unique=True)
  password = Column(Unicode(40))

  def __init__(self, username, password):
    self.username = username
    self.password = password

Base.metadata.create_all()