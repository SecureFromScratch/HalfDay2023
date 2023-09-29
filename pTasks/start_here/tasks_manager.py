import logging
from task import Task

class TasksManager:
    FILENAME = "tasks.txt"

    def __init__(self, logger):
        self.logger = logger
        self.filepath = TasksManager.FILENAME
        self.logger.debug(f"Tasks file is at {self.filepath}")

    def add(self, creator, task_description):
        try:
            with open(self.filepath, "a") as file:
                file.write(task_description + "\n")
            return True
        except Exception as e:
            self.logger.error(f"Failed to write task to file. File: {self.filepath}, Message: {task_description}")
            return False

    def get_active_tasks(self):
        try:
            with open(self.filepath, "r") as file:
                lines = file.readlines()

            tasks = [
                Task("unknown", line.startswith("!"), line.lstrip("!").rstrip())
                for line in lines
            ]
            return tasks
        except Exception as ex:
            self.logger.warning(f"No active tasks found -or- an error happened: {ex}")
            return []
