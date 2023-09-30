package Authorization;

public class InvalidAuth extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6073477479259673013L;

	private String m_right;

	public InvalidAuth(String a_right) {
		m_right = a_right;
	}

	public String getRight() {
		return m_right;
	}
	
    public String getExplanation() {
        return "You do not have autherization to " + AuthMgr.INVALID_AUTH_EXPLANATIONS.get(m_right);
    }
}
