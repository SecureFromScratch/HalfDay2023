import java.io.IOException;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

import Pii.Pii;

public class ExtrasFileHandler extends FileHandler {
	private Optional<Pii<String>> m_username = Optional.empty();
	
    public ExtrasFileHandler() throws IOException, SecurityException {
		super();
    }
    
	public ExtrasFileHandler(String a_filepath) throws IOException, SecurityException {
		super(a_filepath);
	}

    public ExtrasFileHandler(String a_filepath, boolean a_append) throws IOException, SecurityException {
    	super(a_filepath, a_append);
    }
    
	public void setUsername(Pii<String> a_username) {
		m_username = Optional.of(a_username);
	}
	
	public void unsetUsername() {
		m_username = Optional.empty();
	}
	
	@Override
    public void publish(LogRecord a_record) {
		if (m_username.isPresent()) {
			String msg = a_record.getMessage();
			a_record.setMessage("[" + m_username.get().toLoggable() + "] " + msg);
		}
		super.publish(a_record);
	}
}

