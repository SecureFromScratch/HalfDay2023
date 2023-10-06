#include "tasks_manager.h"
#include "task.h"
#include "spdlog/spdlog.h"
#include <iostream>
#include <fstream>

namespace tasksserver
{

TasksManager::TasksManager(const std::string &a_filepath)
	: m_filepath(a_filepath)
{
	spdlog::debug("Tasks file is at {}", m_filepath);
}


bool TasksManager::Add(const std::string &a_creator, const std::string &a_taskDescription)
{
	// NOTE: creator is ignored for now

	if (a_taskDescription.empty())
	{
		return false;
	}

	std::ofstream out(m_filepath, std::ios_base::app);
	out << a_taskDescription << "\n";
	return out.good();
}

std::vector<Task> TasksManager::GetActiveTasks(const std::string &a_username)
{
	std::ifstream file(m_filepath);
	if (!file.good()) {
		return std::vector<Task>{};
	}

	std::vector<Task> tasks;
	while (file.good())
	{
		std::string line;
		std::getline(file, line);
		if (!line.empty())
		{
			bool isUrgent = line.size() > 1 && line[0] == '!';
			tasks.push_back(Task("UNKNOWN", isUrgent, isUrgent ? line.substr(1) : line));
		}
	}

	return tasks;
}

} // tasksserver
