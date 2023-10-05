import logging as baselogging

class _UsernameHolder:
	username_pii = None

	def __str__(self):
		return self.username_pii.to_loggable() if self.username_pii else "<no user>"

class UsernameScope:
	def __init__(self, username_pii):
		_usernameHolder.username_pii = username_pii

	def __enter__(self):
		pass

	def __exit__(self, exc_type, exc_value, traceback):
		_usernameHolder.username_pii = None		
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
