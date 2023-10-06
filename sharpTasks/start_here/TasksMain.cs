using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using SimpleServer;
using AuthorizationNS;

namespace TasksServer
{
    class TasksMain
    {
        private static readonly string SHUTDOWN_PWD_FILE = Path.Combine("..", "..", "shutdown.txt");

        public static void Main(string[] a_args)
        {
            // NOTE: The code in this method does not required changing
            // This code sets up dependency injection
            using ServiceProvider services = ServiceProviderFactory.Create(
                typeof(TasksMain), 
                typeof(TasksManager),
                typeof(SimpleServerFactory));

            TasksMain mainFlow = services.GetRequiredService<TasksMain>();
            mainFlow.Execute(a_args);
        }

        private readonly ILogger<TasksMain> m_logger;
        private readonly TasksManager m_tasksManager;
        private readonly SimpleServerFactory m_serverFactory;

        public TasksMain(ILogger<TasksMain> a_logger, TasksManager a_tasksManager, SimpleServerFactory a_serverFactory)
        {
            m_logger = a_logger;
            m_tasksManager = a_tasksManager;
            m_serverFactory = a_serverFactory;
        }

        void Execute(string[] a_args)
        {
            string shutdownPassword = LoadShutdownPassword(m_logger);

            SimpleServer.SimpleServer server = m_serverFactory.Create(ExtractPort(a_args));

            while (true)
            {
                using (SimpleServer.SimpleServer.Connection connection = server.WaitForConnection())
                {
                    string username = connection.GetInput();
                    if (username.Equals(shutdownPassword))
                    {
                        m_logger.LogInformation("Received shutdown");
                        break;
                    }

                    ///
                    /// TODO: call AuthMgr.getAuthorization to create authorization object
                    ///
				    Authorization authorization = null;
                    DisplayActiveTasks(username, m_tasksManager, connection);
                    PerformAddTaskDialog(username, m_tasksManager, connection);
                }
            }
        }

        private void DisplayActiveTasks(string a_username, TasksManager a_tasksMgr, SimpleServer.SimpleServer.Connection a_connection)
        {
            Task[] tasks = a_tasksMgr.GetActiveTasks(a_username);
            if (tasks.Length == 0)
            {
                a_connection.WriteLine($"Hello {a_username}, there are currently no tasks that require attention.");
            }
            else
            {
                a_connection.WriteLine($"Hello {a_username}, the following tasks require attention:");
                foreach (Task t in tasks)
                {
                    if (t.IsUrgent)
                    {
                        a_connection.WriteLine($"- URGENT: {t.Description}");
                    }
                    else
                    {
                        a_connection.WriteLine($"- {t.Description}");
                    }
                }
            }
        }

        private void PerformAddTaskDialog(Authorization a_authorization, TasksManager a_tasksMgr, SimpleServer.SimpleServer.Connection a_connection)
        {
            a_connection.WriteLine($"{a_authorization.Username}, you can now add a new task or quit.");
            if (a_authorization.Allows(AuthMgr.URGENT_TASK)) {
                a_connection.WriteLine("If you want a task to be marked as urgent, use '!' as the first character. Examples:");
                a_connection.WriteLine("This is a normal task");
                a_connection.WriteLine("!This is an urgent task");
                a_connection.WriteLine("Add a new task now or press enter on an empty line to quit.");
            }

            string newTaskDescription = a_connection.GetInput();
            if (!string.IsNullOrEmpty(newTaskDescription))
            {
                try {
                    a_tasksMgr.Add(a_authorization, newTaskDescription);
                    a_connection.WriteLine("Task added");
                } 
                catch (InvalidAuthException e) {
                    a_connection.WriteLine(e.Explanation);
                }
            }
            a_connection.WriteLine($"Goodbye {a_authorization.Username}.");
        }

        private static int ExtractPort(string[] args)
        {
            Console.Error.WriteLine("USAGE: dotnet run [port]");
            if (args.Length > 1)
            {
                Environment.Exit(-1);
            }

            if (args.Length == 1)
            {
                if (int.TryParse(args[0], out int port))
                {
                    return port;
                }
                else
                {
                    Environment.Exit(-2);
                }
            }

            Console.Error.WriteLine("Using default port 8000");
            return 8000;
        }

        private static string LoadShutdownPassword(ILogger<TasksMain> a_logger)
        {
            try
            {
                string[] lines = File.ReadAllLines(SHUTDOWN_PWD_FILE);
                if (lines.Length != 1 || string.IsNullOrWhiteSpace(lines[0]))
                {
                    string errorMessage = "Shutdown password file should have 1 non-empty line";
                    a_logger.LogError(errorMessage);
                    Console.Error.Write(errorMessage);
                    Environment.Exit(-51);
                }
                return lines[0].Trim();
            }
            catch (IOException)
            {
                a_logger.LogError("Shutdown password file \"{ShutdownPasswordFile}\" not found or inaccessible", SHUTDOWN_PWD_FILE);
                Console.Error.Write($"Shutdown password file \"{SHUTDOWN_PWD_FILE}\" not found or inaccessible");
            }
            Environment.Exit(-50);
            return ""; // to quiet down the compiler
        }
    }
}
