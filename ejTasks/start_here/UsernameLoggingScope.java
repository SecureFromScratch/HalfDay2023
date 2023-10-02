import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class UsernameLoggingScope implements Closeable {
	public final Optional<ExtrasFileHandler> m_extrasHandler;
	
	public UsernameLoggingScope(Logger a_logger, String a_username) {
		do {
			for (Handler h : a_logger.getHandlers()) {
				if (h instanceof ExtrasFileHandler) {
					m_extrasHandler = Optional.of((ExtrasFileHandler)h);
					m_extrasHandler.get().setUsername(a_username);
					return;
				}
			}
			a_logger = a_logger.getParent();
		} while (a_logger != null);
		m_extrasHandler = Optional.empty();
	}

	@Override
	public void close() throws IOException {
		if (m_extrasHandler.isPresent()) {
			m_extrasHandler.get().unsetUsername();
		}
	}

}
