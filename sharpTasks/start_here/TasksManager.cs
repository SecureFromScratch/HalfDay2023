using AuthorizationNS;
using Microsoft.Extensions.Logging;
using Authorization = AuthorizationNS.Authorization;

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

        public bool Add(Authorization a_authorization, string a_taskDescription)
        {
            boolean isAutherized = true; /// TODO: Check if user is authorized to perform action
            if (!isAutherized) {
                throw new InvalidAuth(AuthMgr.URGENT_TASK);
            }
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

        public Task[] GetActiveTasks(Authorization a_authorization)
        {
    	    a_authorization.ThrowIfNotAllowed(AuthMgr.VIEW_ACTIVE);

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
