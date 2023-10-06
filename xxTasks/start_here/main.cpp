#include <iostream>
#include <fstream>
#include <string>
#include <array>
#include "spdlog/spdlog.h"
#include "spdlog/fmt/ranges.h"
#include "spdlog/sinks/basic_file_sink.h"
#include "SimpleNet/SimpleNetMT.h"
#include "tasks_manager.h"

using Connection = simplenet::SimpleNetMT::Connection;
using namespace tasksserver;

static void SetupLogToFile(const std::string& a_logfilePath)
{
	auto fileLogger = spdlog::basic_logger_mt("main", a_logfilePath.c_str(), true);
    spdlog::set_default_logger(fileLogger);
    spdlog::flush_every(std::chrono::seconds(1));
}

void DisplayActiveTasks(const std::string &a_username, TasksManager &a_tasksMgr, Connection &a_connection);
void PerformAddTaskDialog(const std::string &a_username, TasksManager &a_tasksMgr, Connection &a_connection);

int main()
{
	SetupLogToFile("tasks.log");

	simplenet::SimpleNetMT net{8000};
	TasksManager tasksMgr{};

	while (true)
	{
		simplenet::SimpleNetMT::Connection connection = net.WaitForConnection();

		try 
		{
			std::string username = connection.ReadLine();
			spdlog::info("User {} logged in", username);

			DisplayActiveTasks(username, tasksMgr, connection);
			PerformAddTaskDialog(username, tasksMgr, connection);
		}
		catch (const simplenet::SimpleNetMT::ConnectionClosed&)
		{
			std::cout << "CLOSED\n";
		}
	}

	return 0;
}

void DisplayActiveTasks(const std::string &a_username, TasksManager &a_tasksMgr, Connection &a_connection)
{
    std::vector<Task> tasks = a_tasksMgr.GetActiveTasks(a_username);
    if (tasks.empty())
    {
        a_connection.Write("Hello " + a_username + ", there are currently no tasks that require attention.\n");
    }
    else
    {
        a_connection.Write("Hello " + a_username + ", the following tasks require attention:\n");
        for (Task t : tasks)
        {
            if (t.IsUrgent())
            {
                a_connection.Write("- URGENT: " + t.GetDescription() + "\n");
            }
            else
            {
                a_connection.Write("- " + t.GetDescription() + "\n");
            }
        }
    }
}

void PerformAddTaskDialog(const std::string &a_username, TasksManager &a_tasksMgr, Connection &a_connection)
{
    a_connection.Write(a_username + ", you can now add a new task or quit.\n");
    a_connection.Write("If you want a task to be marked as urgent, use '!' as the first character. Examples:\n");
    a_connection.Write("This is a normal task\n");
    a_connection.Write("!This is an urgent task\n");
    a_connection.Write("Add a new task now or press enter on an empty line to quit.\n");

    std::string newTaskDescription = a_connection.ReadLine();
    if (!newTaskDescription.empty())
    {
        a_tasksMgr.Add(a_username, newTaskDescription);
        a_connection.Write("Task added\n");
    }
    a_connection.Write("Goodbye " + a_username + ".");
}
