import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
    public static void main(String[] a_args) throws IOException
    {
		final Logger logger = Logger.getLogger("main");
		String shutdownPassword = loadShutdownPasword(logger);
		SimpleServer server = SimpleServer.create(extractPort(a_args), logger);
		TasksManager tasksMgr = new TasksManager(logger); 
		while (true) {
			try (Connection c = server.waitForConnection()) {
				String username = c.getInput();
				if (username.equals(shutdownPassword)) {
					logger.log(Level.INFO, "Received shutdown");
					break;
				}
				displayActiveTasks(username, tasksMgr, c);
				performAddTaskDialog(username, tasksMgr, c);
			}
		}
		
    }

    private static void displayActiveTasks(String a_username, TasksManager a_tasksMgr, Connection a_connection) {
		Task[] tasks = a_tasksMgr.GetActiveTasks();
		if (tasks.length == 0) {
			a_connection.writeln(String.format("Hello %s, there are currently no tasks that require attention.", a_username));				
		}
		else {
			a_connection.writeln(String.format("Hello %s, the following tasks require attention:", a_username));
			for (Task t : tasks) {
				if (t.isUrgent()) {
					a_connection.writeln(String.format("- URGENT: %s", t.getDescription()));
				}
				else {
					a_connection.writeln(String.format("- %s", t.getDescription()));
				}
			}
		}
    }
    
    private static void performAddTaskDialog(String a_username, TasksManager a_tasksMgr, Connection a_connection) {
		a_connection.writeln(String.format("%s, you can now add a new task or quit.", a_username));
		a_connection.writeln(String.format("If you want a task to be marked as urgent, use '!' as the first character. Examples:"));
		a_connection.writeln(String.format("This is a normal task"));
		a_connection.writeln(String.format("!This is an urgent task"));
		a_connection.writeln(String.format("Add a new task now or press enter on an empty line to quit."));

		String newTaskDescription = a_connection.getInput();				
		if (!newTaskDescription.isEmpty()) {
			a_tasksMgr.Add(a_username, newTaskDescription);
			a_connection.writeln(String.format("Task added"));
		}
		a_connection.writeln(String.format("Goodbye %s.", a_username));
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
    
    private static String loadShutdownPasword(Logger a_logger) {
    	try {
    		List<String> lines = Files.readAllLines(SHUTDOWN_PWD_FILE);
    		if ((lines.size() != 1) || lines.get(0).strip().isEmpty()) {
    			final String errmsg = "Shutdown password file should have 1, non-empty, line";
        		a_logger.log(Level.SEVERE, errmsg);
        		System.err.print(errmsg);
        		System.exit(-51);
    		}
    		return lines.get(0).strip();
    	}
    	catch (IOException ex) {
    		a_logger.log(Level.SEVERE, "Shutdown password file \"{filename}\" not found or inaccessible", SHUTDOWN_PWD_FILE);
    		System.err.print(String.format("Shutdown password file \"%s\" not found or inaccessible", SHUTDOWN_PWD_FILE));
    	}
		System.exit(-50);
    	return ""; // to quiet down compiler
    }
}
