URGENT_TASK = "urgenttask"
VIEW_ACTIVE = "viewactive"

_INVALID_AUTH_EXPLANATIONS = {
    URGENT_TASK: "mark a task as urgent",
    VIEW_ACTIVE: "view active tasks"
}

class InvalidAuth(Exception):
    def __init__(self, right):
        self.right = right

    def getExplanation(self):
        return getNoAuthDescription(self.right)
    
class Authorization:
    def __init__(self, username, allowed):
        self.username = username
        self.allowed = allowed

    def getUsername(self):
        return self.username
    
    def allows(self, right):
        return right in self.allowed

    def throwIfNotAllowed(self, right):
        if not right in self.allowed:
            raise InvalidAuth(right)

def isAllowed(authorization, right):
    return authorization.allows(right)

_AUTH_FILENAME="auth.txt"
def getAuthorization(username, logger):
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
            elif len(parts[0]) == 0 or parts[0] == username:
                allowed[parts[1]] = True
    except Exception as ex:
        logger.warning(f"No auth file {_AUTH_FILENAME} found -or- empty, or an error happened: {ex}")
        logger.warning(f"Each line in auth file is of the form [username]:<right>, i.e. theboss:urgenttask")
        logger.warning(f"If the username is left empty the right is given to all users, i.e. :viewactive")
    return Authorization(username, allowed)

def getNoAuthDescription(right):
    return "You do not have authorization to " + _INVALID_AUTH_EXPLANATIONS[right]
