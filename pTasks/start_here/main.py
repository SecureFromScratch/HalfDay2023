import logging
import sys
from simple_server import SimpleServer
from tasks_manager import TasksManager

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
                print("shutting down")
                return
            logger.info(f"user {username} logged in")
            display_active_tasks(tasks_mgr, username, c)
            perform_add_task_dialog(tasks_mgr, username, c)

def display_active_tasks(tasks_mgr, username, connection):
    tasks = tasks_mgr.get_active_tasks()
    if not tasks:
        connection.writeln(f"Hello {username}, there are currently no tasks that require attention.")
    else:
        connection.writeln(f"Hello {username}, the following tasks require attention:")
        for task in tasks:
            if task.is_urgent():
                connection.writeln(f"- URGENT: {task.get_description()}")
            else:
                connection.writeln(f"- {task.get_description()}")

def perform_add_task_dialog(tasks_mgr, username, connection):
    connection.writeln(f"{username}, you can now add a new task or quit.")
    if username == "theboss":
        connection.writeln(f"If you want a task to be marked as urgent, use '!' as the first character. Examples:")
        connection.writeln(f"This is a normal task")
        connection.writeln(f"!This is an urgent task")
        connection.writeln(f"Add a new task now or press enter on an empty line to quit.")

    new_task_description = connection.get_input().strip()
    if new_task_description:
        if new_task_description.startswith("!") and username != "theboss":
            connection.writeln(f"You are not autherized to enter an urgent task")
        else:
            tasks_mgr.add(username, new_task_description)
            connection.writeln(f"Task added")
    connection.writeln(f"Goodbye {username}.")

def extract_port(args):
    print(f"USAGE: {args[0]} [port]")

    if len(args) > 2:
        sys.exit(-1)

    if len(args) == 2:
        try:
            port = int(args[1])
            return port
        except ValueError:
            sys.exit(-2)

    print("Using default port 8000")
    return 8000

def read_shutdown_pwd():
    SHUTDOWN_PWD_FILE = "../../shutdown.txt"
    try:
        with open(SHUTDOWN_PWD_FILE, "r") as file:
            password = file.readline().strip()
            if not password:
                print(f"Error: The file {SHUTDOWN_PWD_FILE} is empty. Please provide a non-empty password.")
                exit(1)
            return password
    except FileNotFoundError:
        print(f"Error: File '{SHUTDOWN_PWD_FILE}' not found.")
        exit(1)
    except Exception as e:
        print(f"Error: {e}")
        exit(1)

if __name__ == "__main__":
    shutdown_pwd = read_shutdown_pwd()
    print("starting server")
    run(shutdown_pwd)
