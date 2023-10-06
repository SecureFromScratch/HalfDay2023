#ifndef __TASKS_MANAGER_H__
#define __TASKS_MANAGER_H__

#pragma once

#include <string>
#include <vector>
#include "task.h"

namespace tasksserver
{

class TasksManager
{
public:
	TasksManager(const std::string &a_filepath = FILENAME);

	bool Add(const std::string &a_creator, const std::string &a_taskDescription);
	std::vector<Task> GetActiveTasks(const std::string &a_username);

private:
	static constexpr const char *FILENAME = "tasks.txt";

    static bool IsUrgent(const std::string &a_taskDesc);

	std::string m_filepath;
};

} // tasksserver

#endif
