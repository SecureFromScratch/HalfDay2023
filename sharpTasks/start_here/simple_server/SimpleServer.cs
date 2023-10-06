using System.Net;
using System.Net.Sockets;
using System.Text;
using Microsoft.Extensions.Logging;
using PiiLib;
using Utils;

namespace SimpleServer
{
    public class SimpleServerFactory {
        private readonly ILogger<SimpleServer> m_logger;

        public SimpleServerFactory(ILogger<SimpleServer> a_logger) {
            m_logger = a_logger;
        }

        public SimpleServer Create(int a_port)
        {
            return new SimpleServer(a_port, m_logger);
        }
    }

    public class SimpleServer : IDisposable
    {
        private readonly TcpListener m_serverSocket;
        private readonly ILogger<SimpleServer> m_logger;
        private Optional<Connection> m_activeRequest = Optional.Empty<Connection>();

        public class Connection : IDisposable
        {
            private readonly SimpleServer m_server;
            private readonly TcpClient m_curSocket;
            private readonly StreamReader m_reader;
            private readonly StreamWriter m_writer;

            public Connection(SimpleServer a_server, TcpClient a_client)
            {
                m_server = a_server;
                m_curSocket = a_client;
                NetworkStream stream = m_curSocket.GetStream();
                m_reader = new StreamReader(stream, Encoding.UTF8);
                m_writer = new StreamWriter(stream, Encoding.ASCII) { AutoFlush = true };
            }

            public string GetInput()
            {
                try
                {
                    string? line = m_reader.ReadLine();
                    if (line == null)
                    {
                        m_server.m_logger.LogWarning("Receive terminated");
                        Close();
                        return "";
                    }

                    return line;
                }
                catch (IOException ex)
                {
                    m_server.m_logger.LogWarning("Socket error {ex}", ex.Message);
                    Close();
                    return "";
                }
            }

            public void Write(PiiConcat text)
            {
                m_server.m_logger.LogDebug("Server is writing {text}", text);
                m_writer.Write(text.ExposeUnsecured());
                m_writer.Flush();
            }

            public void WriteLine(PiiConcat text)
            {
                m_server.m_logger.LogDebug("Server is writing {text}", text);
                m_writer.WriteLine(text.ExposeUnsecured());
                m_writer.Flush();
            }

            public void Close()
            {
                m_reader.Close();
                m_writer.Close();
                m_curSocket.Close();

                if (m_server.m_activeRequest.IsPresent() && m_server.m_activeRequest.Get() == this)
                {
                    m_server.m_activeRequest = Optional.Empty<Connection>();
                }
            }

            public void Dispose()
            {
                Close();
            }
        }

        public SimpleServer(int port, ILogger<SimpleServer> logger)
        {
            m_logger = logger;
            m_serverSocket = new TcpListener(IPAddress.Any, port);
            m_serverSocket.Start();
        }

        public Connection WaitForConnection()
        {
            if (m_activeRequest.IsPresent())
            {
                throw new PreviousConnectionNotClosed();
            }

            TcpClient client = m_serverSocket.AcceptTcpClient();
            m_logger.LogInformation("New client connected");

            try
            {
                return new Connection(this, client);
            }
            catch (IOException ex)
            {
                m_logger.LogCritical("Socket error during connection init: {ex}", ex.Message);
                throw;
            }
        }

        public void Dispose()
        {
            m_serverSocket.Stop();
        }
    }

    public class PreviousConnectionNotClosed : Exception
    {
        public PreviousConnectionNotClosed()
        {
        }
    }

}
