import logging
import sys
from simple_server import SimpleServer
from tasks_manager import TasksManager
import authmgr

logging.basicConfig(filename='tasks.log',
				filemode='a',
				format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
				datefmt='%H:%M:%S',
				level=logging.DEBUG)

def run(shutdown_pwd):
    logger = logging.getLogger("main")
    tasks_mgr = TasksManager(logger)
    server = SimpleServer.create(extract_port(sys.argv))
    while True:
        logger.debug("waiting for connection")
        with server.wait_for_connection() as c:
            username = c.get_input()
            if username == shutdown_pwd:
                sys.stderr.write("shutting down\n")
                return
            logger.info(f"user {username} logged in")
            autherization = authmgr.getAutherization(username, logger)
            try:
                display_active_tasks(tasks_mgr, autherization, c)
                perform_add_task_dialog(tasks_mgr, autherization, c)
            except authmgr.InvalidAuth as e:
                logger.warning(f"user {username} tried to perform {e.type}")
                c.writeln(f"{autherization.username}, {e.getExplanation()}")
                
def display_active_tasks(tasks_mgr, autherization, connection):
    tasks = tasks_mgr.get_active_tasks(autherization)
    if not tasks:
        connection.writeln(f"Hello {autherization.username}, there are currently no tasks that require attention.")
    else:
        connection.writeln(f"Hello {autherization.username}, the following tasks require attention:")
        for task in tasks:
            if task.is_urgent():
                connection.writeln(f"- URGENT: {task.get_description()}")
            else:
                connection.writeln(f"- {task.get_description()}")

def perform_add_task_dialog(tasks_mgr, autherization, connection):
    connection.writeln(f"{autherization.username}, you can now add a new task or quit.")
    if authmgr.isAllowed(autherization, authmgr.URGENT_TASK):
        connection.writeln(f"If you want a task to be marked as urgent, use '!' as the first character. Examples:")
        connection.writeln(f"This is a normal task")
        connection.writeln(f"!This is an urgent task")
        connection.writeln(f"Add a new task now or press enter on an empty line to quit.")

    new_task_description = connection.get_input().strip()
    if new_task_description:
        tasks_mgr.add(autherization, new_task_description)
        connection.writeln(f"Task added")
    connection.writeln(f"Goodbye {autherization.username}.")

def extract_port(args):
    sys.stderr.write(f"USAGE: {args[0]} [port]\n")

    if len(args) > 2:
        sys.exit(-1)

    if len(args) == 2:
        try:
            port = int(args[1])
            return port
        except ValueError:
            sys.exit(-2)

    sys.stderr.write("Using default port 8000\n")
    return 8000

def read_shutdown_pwd():
    SHUTDOWN_PWD_FILE = "../../shutdown.txt"
    try:
        with open(SHUTDOWN_PWD_FILE, "r") as file:
            password = file.readline().strip()
            if not password:
                sys.stderr.write(f"Error: The file {SHUTDOWN_PWD_FILE} is empty. Please provide a non-empty password.\n")
                exit(1)
            return password
    except FileNotFoundError:
        sys.stderr.write(f"Error: File '{SHUTDOWN_PWD_FILE}' not found.\n")
        exit(1)
    except Exception as e:
        sys.stderr.write(f"Error: {e}\n")
        exit(1)

if __name__ == "__main__":
    shutdown_pwd = read_shutdown_pwd()
    sys.stderr.write("starting server\n")
    run(shutdown_pwd)
