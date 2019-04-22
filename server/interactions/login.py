import requests
import sys

if(len(sys.argv) != 3):
  print "usage python2 createUser.py [username] [password]"
  sys.exit(0)

username = sys.argv[1]
password = sys.argv[2]

data = {"username":username,"password":password,"passwordConfirmation":password}

r = requests.post("http://127.0.0.1:5000/user/login",json = data)
print r.text

print r.status_code
