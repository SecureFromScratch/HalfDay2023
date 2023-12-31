import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TasksManager
{
	private static final Path FILENAME = Paths.get("tasks.txt");
	private static final Task[] NO_TASKS = new Task[0];

	private final Logger m_logger;
    private final Path m_filepath;

    public TasksManager(Logger a_logger)
    {
    	this(a_logger, FILENAME);
    }

    public TasksManager(Logger a_logger, Path a_filepath)
    {
    	m_logger = a_logger;
        m_filepath = a_filepath;
        a_logger.log(Level.FINE, String.format("Tasks file is at %s", m_filepath));
    }

    public boolean Add(String a_creator, String a_taskDescription) 
    {
    	// NOTE: a_creator is ignored for now
        try {
			Files.write(m_filepath, Arrays.asList(a_taskDescription), new StandardOpenOption[]{ StandardOpenOption.CREATE, StandardOpenOption.APPEND });
		} 
        catch (IOException e) {
        	m_logger.log(Level.SEVERE, String.format("Failed to write task to file. File: %s, Message: %s", m_filepath, a_taskDescription));
			return false;
		}
    	
        return true;                
    }

    public Task[] GetActiveTasks()
    {
        try
        {
            List<String> lines = Files.readAllLines(m_filepath);
        	List<Task> tasks = new ArrayList<Task>(lines.size());
	        for (String line : lines)
            {
	        	final boolean isUrgent = (line.charAt(0) == '!');
	        	final String msg = isUrgent ? line.substring(1) : line;
	        	Task t = new Task("unknown", isUrgent, msg);
	        	tasks.add(t);
            }
        	return tasks.toArray(NO_TASKS);
        }
        catch (IOException ex)
        {
        	m_logger.warning(String.format("No active tasks found -or- an error happened: %s", ex.getMessage()));
        	return NO_TASKS;
        }
    }	
}
