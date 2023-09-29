URGENT_TASK = "urgenttask"

_INVALID_AUTH_EXPLANATIONS = {
    URGENT_TASK: "mark a task as urgent"
}

class InvalidAuth(Exception):
    def __init__(self, type):
        self.type = type

    def getExplanation(self):
        return getNoAuthDescription(self.type)
    
class Autherization:
    def __init__(self, username, allowed):
        self.username = username
        self.allowed = allowed

    def allows(self, type):
        return type in self.allowed
            
def isAllowed(autherization, type):
    return autherization.allows(type)

_AUTH_FILENAME="auth.txt"
def getAutherization(username, logger):
    allowed = { } # Welcome/Allow list according to Easy to Use Safely
    try:
        with open(_AUTH_FILENAME, "r") as file:
            lines = file.readlines()

        for line in lines:
            if len(line.rstrip()) == 0:
                continue # skip empty lines
            parts = line.rstrip().split(":")
            print(parts)
            if len(parts) != 2:
                logger.error(f"Auth line invalid: {line}")
                allowed = { } # if there's a format error I mistrust EVERYTHING
                break
            elif parts[0] == username:
                print(parts[1])
                allowed[parts[1]] = True
    except Exception as ex:
        logger.warning(f"No auth file {_AUTH_FILENAME} found -or- empty, or an error happened: {ex}")
    print(allowed)
    return Autherization(username, allowed)

def getNoAuthDescription(type):
    return "You do not have autherization to " + _INVALID_AUTH_EXPLANATIONS[type]
