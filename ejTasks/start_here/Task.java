public class Task {
	private final String m_creator;
	private final String m_desc;
	private final boolean m_isUrgent;
	
	public Task(String a_creator, boolean a_isUrgent, String a_desc) {
		m_creator = a_creator;
		m_isUrgent = a_isUrgent;
		m_desc = a_desc;
	}
	
	public String getCreator() {
		return m_creator;
	}
	
	public boolean isUrgent() {
		return m_isUrgent;
	}
	
	public String getDescription() {
		return m_desc;
	}
}
