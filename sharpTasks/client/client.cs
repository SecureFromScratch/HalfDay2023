using System;
using System.IO;
using System.Net.Sockets;
using System.Text;

public class TasksClient
{
    private struct Params {
        public string username;
        public string serverHost;
        public int serverPort;
    }

    public static void Main(string[] args)
    {
        Console.WriteLine("Usage: Client [host] [port] [-user=<username>]");
        Params p = ExtractArgs(args);

        try {
            using (Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp))
            {
                socket.Connect(p.serverHost, p.serverPort);

                Console.WriteLine("Connected to the server. Sending username.");
                using (NetworkStream networkStream = new NetworkStream(socket))
                using (StreamWriter writer = new StreamWriter(networkStream, Encoding.UTF8, 1024, true))
                using (StreamReader consoleReader = new StreamReader(Console.OpenStandardInput()))
                {
                    writer.WriteLine(p.username);
                    writer.Flush();

                    var serverReaderThread = new System.Threading.Thread(() =>
                    {
                        using (StreamReader reader = new StreamReader(networkStream, Encoding.UTF8))
                        {
                            char[] buffer = new char[1024];
                            int bytesRead;
                            while ((bytesRead = reader.Read(buffer, 0, buffer.Length)) > 0)
                            {
                                Console.Write(new string(buffer, 0, bytesRead));
                            }
                        }
                        Environment.Exit(0);
                    });
                    serverReaderThread.Start();

                    while (true)
                    {
                        string? userInput = consoleReader.ReadLine();
                        if (userInput == null)
                        {
                            break; // Exit if the user closes the console input
                        }
                        writer.WriteLine(userInput);
                        writer.Flush();
                    }
                }
            }
        }
        catch (IOException e)
        {
            Console.WriteLine(e.StackTrace);
        }
    }

    private static Params ExtractArgs(string[] args) {
        string username = UsernameProvider.GetUsername();
        bool isCustomUsername = false;
        string serverHost = "localhost";
        int serverPort = 8000;

        if (args.Length > 3)
        {
            Environment.Exit(-1);
        }

        if (args.Length > 0) {
            if (args[0].StartsWith("-user=")) {
                username = args[0].Substring("-user=".Length);
                isCustomUsername = true;
            }
            else {
                serverHost = args[0];                
            }
            if (args.Length > 1) {
                if (args[1].StartsWith("-user=")) {
                    if (isCustomUsername) {
                        Console.WriteLine("Can't use -user= more than once");
                        Environment.Exit(-1);
                    }
                    username = args[1].Substring("-user=".Length);
                    isCustomUsername = true;
                }
                else if (isCustomUsername) {
                    serverHost =  args[1];
                }
                else {
                    serverPort = int.Parse(args[1]);
                }

                if (args.Length > 2) {
                    if (args[2].StartsWith("-user=")) {
                        if (isCustomUsername) {
                            Console.WriteLine("Can't use -user= more than once");
                            Environment.Exit(-1);
                        }
                        username = args[2].Substring("-user=".Length);
                        isCustomUsername = true;
                    }
                    else if (isCustomUsername) {
                        serverPort = int.Parse(args[1]);
                    }
                    else if (!isCustomUsername) {
                        Console.WriteLine("Too many arguments");
                        Environment.Exit(-1);
                    }
                }
            }
        }

        Params p;
        p.username = username;
        p.serverHost = serverHost;
        p.serverPort = serverPort;
        return p;
    }
}

public static class UsernameProvider
{
    public static string GetUsername() {
        string? simulated =  Environment.GetEnvironmentVariable("SIMULATED_USERNAME");
        if (simulated != null)
        {
            return simulated;
        }
        string username = Environment.UserName;
        username = char.ToUpper(username[0]) + ((username.Length > 1) ? username[1..] : "");
        return username;
    }
}
