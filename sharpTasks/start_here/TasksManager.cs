using System;
using System.Collections.Generic;
using System.IO;
using Microsoft.Extensions.Logging;

namespace TasksServer
{
    public class TasksManager
    {
        private static readonly string FILENAME = "tasks.txt";

        private readonly ILogger<TasksManager> m_logger;
        private readonly string m_filepath;

        public TasksManager(ILogger<TasksManager> am_logger)
        {
            m_logger = am_logger;
            m_filepath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, FILENAME);
            m_logger.LogDebug("Tasks file is at {m_filepath}", m_filepath);
        }

        public bool Add(string a_creator, string a_taskDescription)
        {
            // NOTE: creator is ignored for now
            try
            {
                File.AppendAllLines(m_filepath, new[] { a_taskDescription });
                return true;
            }
            catch (IOException)
            {
                m_logger.LogError("Failed to write task to file. File: {filepath}, Message: {taskDescription}", m_filepath, a_taskDescription);
                return false;
            }
        }

        public Task[] GetActiveTasks(string a_username)
        {
            try
            {
                string[] lines = File.ReadAllLines(m_filepath);
                var tasks = new List<Task>(lines.Length);
                foreach (var line in lines)
                {
                    bool isUrgent = IsUrgent(line);
                    string msg = isUrgent ? line.Substring(1) : line;
                    var task = new Task("unknown", isUrgent, msg);
                    tasks.Add(task);
                }

                return tasks.ToArray();
            }
            catch (IOException ex)
            {
                m_logger.LogWarning("No active tasks found -or- an error happened: {ex}", ex.Message);
                return Array.Empty<Task>();
            }
        }

        private static bool IsUrgent(string a_taskDesc) {
            return a_taskDesc.StartsWith('!');
        }
    }
}
