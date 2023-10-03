package Pii;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class PiiConcat {
    private class PiiLocation {
        private int m_offset;
        private int m_length;
        private String m_altId;

        public PiiLocation(int a_offset, int a_length, String a_altId)
        {
            m_offset = a_offset;
            m_length = a_length;
            m_altId = a_altId;
        }

        public int getOffset()
        {
            return m_offset;
        }

        public int length()
        {
            return m_length;
        }

        public String getAltId()
        {
            return m_altId;
        }

        public void push(int a_howMuch)
        {
            m_offset += a_howMuch;
        }
    }

    private StringBuilder m_stringBuilder = new StringBuilder();
    private List<PiiLocation> m_piiLocations = new ArrayList<PiiLocation>();
    private UnaryOperator<String> m_elementTransform;

	public PiiConcat() {
	}

	public PiiConcat(UnaryOperator<String> a_elementTransform) {
        m_elementTransform = a_elementTransform;
    }

	public PiiConcat(String... a_strings) {
		for (String s : a_strings) {
			this.append(s);
		}
	}
	
	public PiiConcat(Object... a_stringsAndPiis) {
		for (Object o : a_stringsAndPiis) {
			if (o instanceof String) {
				this.append((String)o);
			}
			else if (o instanceof Pii) {
				Pii pii = (Pii)o;
				if (pii.exposeUnsecured() instanceof String) {
					this.append((Pii<String>)pii);
				}
				else {
					throw new ClassCastException("Pii was not a Pii<String>");
				}
			}
			//else if (o instanceof PiiConcat) {
				
			//}
			else {
				throw new ClassCastException("Unsupported type " + o.getClass().getName());
			}
		}
	}

	public void append(String a_nonclassifiedString)
    {
        if (m_elementTransform != null)
        {
            a_nonclassifiedString = m_elementTransform.apply(a_nonclassifiedString);
        }
        m_stringBuilder.append(a_nonclassifiedString);
    }

    public void pushPrefix(String a_prefix)
    {
        if (m_elementTransform != null)
        {
            a_prefix = m_elementTransform.apply(a_prefix);
        }

        for (PiiLocation l : m_piiLocations)
        {
            l.push(a_prefix.length());
        }
        m_stringBuilder.insert(0, a_prefix);
    }

    public <TValue> void append(Pii<TValue> a_pii)
    {
        int offset = m_stringBuilder.length();
        if (m_elementTransform == null)
        {
            m_stringBuilder.append(a_pii.exposeUnsecured());
        }
        else
        {
            String exposed = m_elementTransform.apply(a_pii.exposeUnsecured().toString());
            m_stringBuilder.append(exposed);
        }
        int length = m_stringBuilder.length() - offset;
        m_piiLocations.add(new PiiLocation(offset, length, a_pii.toString()));
    }

    public <TValue> void append(ObscuredPii<TValue> a_pii)
    {
        int offset = m_stringBuilder.length();
        if (m_elementTransform == null)
        {
            m_stringBuilder.append(a_pii.exposeUnsecured());
        }
        else
        {
            String exposed = m_elementTransform.apply(a_pii.exposeUnsecured().toString());
            m_stringBuilder.append(exposed);
        }
        int length = m_stringBuilder.length() - offset;
        m_piiLocations.add(new PiiLocation(offset, length, a_pii.toLoggable()));
    }

    public String exposeUnsecured() {
        return m_stringBuilder.toString();
    }

    public int exposeUnsecuredLength() {
    	return m_stringBuilder.length();
    }
    
    public char exposeUnsecuredCharAt(int a_index) {
    	return m_stringBuilder.charAt(a_index);
    }
    
    public CharSequence expostUnsecureSubSequence(int a_start, int a_end) {
    	return m_stringBuilder.subSequence(a_start, a_end);
    }
    
    @Override
    public String toString() { // NOTE: since debugger also calls this method (even while I debug it) it must be reentrant
    	return toLoggable();
    }
    
    public String toLoggable() {
        StringBuilder loggableStr = new StringBuilder();

        int copyPos = 0;

        for (PiiLocation piiLoc : this.m_piiLocations) {
            int nonPiiLength = piiLoc.getOffset() - copyPos;
            CharSequence nonPiiPart = m_stringBuilder.subSequence(copyPos, nonPiiLength);
            loggableStr.append(nonPiiPart);
            loggableStr.append(piiLoc.getAltId());
            copyPos = piiLoc.getOffset() + piiLoc.length();
        }

        // copy last part
        CharSequence  rest = m_stringBuilder.subSequence(copyPos, m_stringBuilder.length());
        loggableStr.append(rest);

        return loggableStr.toString();
    }
}
