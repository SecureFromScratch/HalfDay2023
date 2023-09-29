

import java.util.Set;

public class Autherization {
	private final String m_username;
	private final Set<String> m_allowed;
	
	public Autherization(String a_username, Set<String> a_allowed) {
		m_username = a_username;
		m_allowed = a_allowed;
	}
	
	public String getUsername() {
		return m_username;
	}
	
	public boolean allows(String a_right) {
		return m_allowed.contains(a_right);
	}
}
