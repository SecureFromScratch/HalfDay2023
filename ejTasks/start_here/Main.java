import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import Authorization.AuthMgr;
import Authorization.Authorization;
import Authorization.InvalidAuth;
import SimpleServer.SimpleServer;
import SimpleServer.SimpleServer.Connection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// to use logging configuration add this to VM flags: -Djava.util.logging.config.file=logging.properties
final class Main
{
	private final static Path SHUTDOWN_PWD_FILE = Paths.get("..", "..", "shutdown.txt");
	private final static Logger s_logger = Logger.getLogger("main");
	
    public static void main(String[] a_args) throws IOException
    {
		String shutdownPassword = loadShutdownPasword();
		SimpleServer server = SimpleServer.create(extractPort(a_args), s_logger);
		TasksManager tasksMgr = new TasksManager(s_logger); 
		while (true) {
			try (Connection c = server.waitForConnection()) {
				String username = c.getInput();
				if (username.equals(shutdownPassword)) {
					s_logger.log(Level.INFO, "Received shutdown");
					break;
				}
				try (UsernameLoggingScope scope = new UsernameLoggingScope(s_logger, username)) {
					s_logger.log(Level.INFO, "Logged in"); // username added automatically
					final Authorization autherization = AuthMgr.getAuthorization(username, s_logger);
					displayActiveTasks(autherization, tasksMgr, c);
					performAddTaskDialog(autherization, tasksMgr, c);
				}
			}
		}
		
    }

    private static void displayActiveTasks(Authorization a_authorization, TasksManager a_tasksMgr, Connection a_connection) {
		Task[] tasks = a_tasksMgr.GetActiveTasks(a_authorization);
		if (tasks.length == 0) {
			a_connection.writeln(String.format("Hello %s, there are currently no tasks that require attention.", a_authorization.getUsername()));				
		}
		else {
			a_connection.writeln(String.format("Hello %s, the following tasks require attention:", a_authorization.getUsername()));
	        a_connection.writeln("URGENT? TASK");
        	for (Task t : tasks) {
				if (t.isUrgent()) {
					a_connection.writeln(String.format("YES     %s", t.getDescription()));
				}
				else {
					a_connection.writeln(String.format("NO      %s", t.getDescription()));
				}
			}
		}
    }
    
    private static void performAddTaskDialog(Authorization a_authorization, TasksManager a_tasksMgr, Connection a_connection) {
		a_connection.writeln(String.format("%s, you can now add a new task or quit.", a_authorization.getUsername()));
		if (a_authorization.allows(AuthMgr.URGENT_TASK)) {
			a_connection.writeln(String.format("If you want a task to be marked as urgent, use '!' as the first character. Examples:"));
			a_connection.writeln(String.format("This is a normal task"));
			a_connection.writeln(String.format("!This is an urgent task"));
			a_connection.writeln(String.format("Add a new task now or press enter on an empty line to quit."));
		}
		
		String newTaskDescription = a_connection.getInput();				
		if (!newTaskDescription.isEmpty()) {
			try {
				a_tasksMgr.Add(a_authorization, newTaskDescription);
				a_connection.writeln(String.format("Task added"));
			} catch (InvalidAuth e) {
				// On next line username added automatically by logger
				s_logger.log(Level.WARNING, String.format("Tried to perform unautherized operation %s",  e.getRight()));
				a_connection.writeln(e.getExplanation());
			}
		}
		a_connection.writeln(String.format("Goodbye %s.", a_authorization.getUsername()));
    }

    private static int extractPort(String[] a_args) {
        System.err.println("USAGE: java Main [port]");
    	if (a_args.length > 1) {
            System.exit(-1);
    	}
    	
    	if (a_args.length == 1) {
	    	try {
	    		int port = Integer.parseInt(a_args[0]);
	        	return port;
	    	}
	    	catch (NumberFormatException unused) {
	            System.exit(-2);
	    	}
    	}
    	
        System.err.print("Using default port 8000");
    	return 8000;
    }
    
    private static String loadShutdownPasword() {
    	try {
    		List<String> lines = Files.readAllLines(SHUTDOWN_PWD_FILE);
    		if ((lines.size() != 1) || lines.get(0).strip().isEmpty()) {
    			final String errmsg = "Shutdown password file should have 1, non-empty, line";
        		s_logger.log(Level.SEVERE, errmsg);
        		System.err.print(errmsg);
        		System.exit(-51);
    		}
    		return lines.get(0).strip();
    	}
    	catch (IOException ex) {
    		s_logger.log(Level.SEVERE, "Shutdown password file \"{filename}\" not found or inaccessible", SHUTDOWN_PWD_FILE);
    		System.err.print(String.format("Shutdown password file \"%s\" not found or inaccessible", SHUTDOWN_PWD_FILE));
    	}
		System.exit(-50);
    	return ""; // to quiet down compiler
    }
}
