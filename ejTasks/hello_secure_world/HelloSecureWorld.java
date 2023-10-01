import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.lang.RuntimeException;

public final class HelloSecureWorld {
	private static void setupFileLogger(String a_name, String a_filepath) {
		final Logger logger = Logger.getLogger("main");

		try {  
			// This block configure the logger with handler and formatter  
			final FileHandler fh = new FileHandler(a_filepath);  
			logger.addHandler(fh);
			final SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  
		//} catch (SecurityException e) {  
		//    e.printStackTrace();  
		} catch (IOException e) {  
		    throw new RuntimeException(e.getMessage());  
		}
		logger.setUseParentHandlers(false);
	}

	public static void main(final String[] args) {
		setupFileLogger("main", "helloworld.log");
		final Logger logger = Logger.getLogger("main");

		logger.log(Level.INFO, "Hello Secure World!");
	}
}
