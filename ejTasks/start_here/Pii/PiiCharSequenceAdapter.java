package Pii;

public class PiiCharSequenceAdapter<TValue> implements CharSequence {
	PiiConcat m_concat;
	
	public PiiCharSequenceAdapter(PiiConcat a_concat) {
		m_concat = a_concat;
	}

	@Override
	public int length() {
		return m_concat.exposeUnsecuredLength();
	}

	@Override
	public char charAt(int a_index) {
		return m_concat.exposeUnsecuredCharAt(a_index);
	}

	@Override
	public CharSequence subSequence(int a_start, int a_end) {
		return m_concat.expostUnsecureSubSequence(a_start, a_end);
	}	

    public String toLoggable() {
        return m_concat.toLoggable();
    }
}
