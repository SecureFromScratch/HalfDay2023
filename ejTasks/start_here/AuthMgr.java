import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthMgr {
	public static final String URGENT_TASK = "urgenttask";

	public static class InvalidAuth extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6073477479259673013L;
		private static final Map<String, String> INVALID_AUTH_EXPLANATIONS = new HashMap<String, String>();
		static {
			INVALID_AUTH_EXPLANATIONS.put(URGENT_TASK, "mark a task as urgent");
		}

		private String m_right;

		InvalidAuth(String a_right) {
			m_right = a_right;
		}

		public String getRight() {
			return m_right;
		}
		
	    public String getExplanation() {
	        return "You do not have autherization to " + INVALID_AUTH_EXPLANATIONS.get(m_right);
	    }
	}
	
	public static boolean isAllowed(Autherization a_autherization, String a_right) {
	    return a_autherization.allows(a_right);
	}

	private final static Path AUTH_FILENAME = Path.of("auth.txt");
	public static Autherization getAutherization(String a_username, Logger a_logger) {
	    Set<String> allowed = new HashSet<String>(); // Welcome/Allow list according to Easy to Use Safely
	    try {
            List<String> lines = Files.readAllLines(AUTH_FILENAME);
	        for (String line : lines) {
	            if (line.isBlank()) {
	                continue; // skip empty lines
	            }
	            
	            String[] parts = line.split(":");
	            if (parts.length != 2) {
	                a_logger.log(Level.SEVERE, String.format("Auth line invalid: %s", line));
	                allowed.clear(); // if there's a format error I mistrust EVERYTHING
	                break;
	            }
	            else if (parts[0].equals(a_username)) {
	                allowed.add(parts[1]);
	            }
	        }
	    }
	    catch (Exception ex) {
	        a_logger.log(Level.WARNING, String.format("No auth file %s found -or- empty, or an error happened: %s", AUTH_FILENAME, ex.getMessage()));
	    }
	    return new Autherization(a_username, allowed);
	}
}
