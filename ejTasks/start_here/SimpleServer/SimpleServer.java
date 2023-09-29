package SimpleServer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleServer implements Closeable {
	private int m_port;
	private Logger m_logger;
	private ServerSocket m_serverSocket;
	private Optional<Connection> m_activeRequest = Optional.empty(); 

	public static SimpleServer create(final int port, final Logger logger) throws IOException {
		return new SimpleServer(port, logger);
	}

	public class Connection implements Closeable {
		private final Socket m_curSocket;
		private final BufferedReader m_reader;
		private final PrintWriter m_writer;

		public Connection(final Socket s) throws IOException {
			m_curSocket = s;

			InputStream input = m_curSocket.getInputStream();
            m_reader = new BufferedReader(new InputStreamReader(input));

			OutputStream output = m_curSocket.getOutputStream();
			m_writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
		}

		public String getInput() {
			try {
	            String line = m_reader.readLine();
	            if (line == null) {
	    			m_logger.log(Level.WARNING, "Receive terminated");
	    			close();
	    			return "";
	            }
	            
	            return line;
			}
			catch (IOException ex) {
				m_logger.warning(String.format("Socket error: ", ex.toString()));
				close();
				return "";
			}
		}

		public void write(String a_text) {
			m_logger.log(Level.FINEST, "Server is writing " + a_text);
			m_writer.print(a_text);
			m_writer.flush();
		}

		public void writeln(String a_text) {
			m_logger.log(Level.FINEST, "Server is writing " + a_text);
			m_writer.println(a_text);
			m_writer.flush();
		}

		@Override
		public void close() {
			try {
				m_reader.close();
				m_writer.close();
				m_curSocket.close();
			}
			catch (IOException ex) {
				m_logger.severe(String.format("Error closing socket: ", ex));
			}

			if (m_activeRequest.isPresent() && (m_activeRequest.get() == this)) {
				m_activeRequest = Optional.empty();
			}
		}
	}

	public SimpleServer(int port, Logger logger) throws IOException {
		m_port = port;
		m_logger = logger;
		m_serverSocket = new ServerSocket(port);
 	}
	
	public Connection waitForConnection() throws IOException {
		if (m_activeRequest.isPresent()) {
			throw new PreviousConnectionNotClosed();
		}
		
		Optional<Socket> socket = Optional.empty();
		while (true) {
			try {
				m_logger.log(Level.INFO, "Server is listening on port " + m_port);
	
				socket = Optional.of(m_serverSocket.accept());
				m_logger.log(Level.INFO, "New client connected");
				
				if (!socket.isEmpty() && socket.get() != null) {
					try {
						return new Connection(socket.get());
					}
					catch (IOException ex) {
						m_logger.severe(String.format("Socket error during connection init: ", ex.toString()));
					}
				}
			}
			catch (IOException ex) {
				if (socket.isPresent()) {
					socket.get().close();
				}
				m_logger.log(Level.WARNING, "Socket read error " + ex.getMessage());
				throw ex;
			}
		}
	}

	@Override
	public void close() throws IOException {
		m_serverSocket.close();
	}
}
