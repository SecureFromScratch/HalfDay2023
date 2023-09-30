import logging
from task import Task
import authmgr

class TasksManager:
    FILENAME = "tasks.txt"

    def __init__(self, logger):
        self.logger = logger
        self.filepath = TasksManager.FILENAME
        self.logger.debug(f"Tasks file is at {self.filepath}")

    def add(self, authorization, task_description):
        if _is_urgent(task_description) and not authorization.allows(authmgr.URGENT_TASK):
            raise authmgr.InvalidAuth(authmgr.URGENT_TASK)
        try:
            with open(self.filepath, "a") as file:
                file.write(task_description + "\n")
            return True
        except Exception as e:
            self.logger.error(f"Failed to write task to file. File: {self.filepath}, Message: {task_description}")
            return False

    def get_active_tasks(self, authorization):
        authorization.throwIfNotAllowed(authmgr.VIEW_ACTIVE)
        try:
            with open(self.filepath, "r") as file:
                lines = file.readlines()

            tasks = [
                Task("unknown", _is_urgent(line), line.lstrip("!").rstrip())
                for line in lines
            ]
            return tasks
        except Exception as ex:
            self.logger.warning(f"No active tasks found -or- an error happened: {ex}")
            return []

def _is_urgent(task_description):
    return task_description.startswith("!")