URGENT_TASK = "urgenttask"

_INVALID_AUTH_EXPLANATIONS = {
    URGENT_TASK: "mark a task as urgent"
}

class InvalidAuth(Exception):
    def __init__(self, right):
        self.right = right

    def getExplanation(self):
        return getNoAuthDescription(self.right)
    
class Autherization:
    def __init__(self, username, allowed):
        self.username = username
        self.allowed = allowed

    def allows(self, right):
        return right in self.allowed
            
def isAllowed(autherization, right):
    return autherization.allows(right)

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
            if len(parts) != 2:
                logger.error(f"Auth line invalid: {line}")
                allowed = { } # if there's a format error I mistrust EVERYTHING
                break
            elif parts[0] == username:
                allowed[parts[1]] = True
    except Exception as ex:
        logger.warning(f"No auth file {_AUTH_FILENAME} found -or- empty, or an error happened: {ex}")
    return Autherization(username, allowed)

def getNoAuthDescription(right):
    return "You do not have autherization to " + _INVALID_AUTH_EXPLANATIONS[right]
