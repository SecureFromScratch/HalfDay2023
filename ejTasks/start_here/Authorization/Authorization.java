package Authorization;

import java.util.Set;

import Pii.Pii;

public class Authorization {
	private final Pii<String> m_username;
	private final Set<String> m_allowed;
	
	/*package*/ Authorization(Pii<String> a_username, Set<String> a_allowed) {
		m_username = a_username;
		m_allowed = a_allowed;
	}
	
	public Pii<String> getUsername() {
		return m_username;
	}
	
	public boolean allows(String a_right) {
		return m_allowed.contains(a_right);
	}
	
	public void throwIfNotAllowed(String a_right) throws InvalidAuth {
		if (!allows(a_right)) {
			throw new InvalidAuth(a_right);
		}
	}
}
