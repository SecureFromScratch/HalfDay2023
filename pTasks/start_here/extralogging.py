import logging as baselogging

class _UsernameHolder:
	username = None

	def __str__(self):
		return self.username if self.username else "<no user>"

class UsernameScope:
	def __init__(self, username):
		_usernameHolder.username = username

	def __enter__(self):
		pass

	def __exit__(self, exc_type, exc_value, traceback):
		_usernameHolder.username = None		
		return True

_usernameHolder = _UsernameHolder()

baselogging.basicConfig(filename='tasks.log',
				filemode='a',
                format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(user)s %(message)s',
				datefmt='%H:%M:%S',
				level=baselogging.DEBUG)

def getLogger(name):
	baselogger = baselogging.getLogger(name)
	return baselogging.LoggerAdapter(baselogger, {'user': _usernameHolder})
