package Pii;

import java.util.function.UnaryOperator;

public class ObscuredPii<TValue> extends Pii<TValue> {
	public interface AltIdGenerator<TValue> {
		String obscure(TValue a_value);
	}
	
	private String m_altIdentifier;

    public ObscuredPii(TValue a_value, String a_altIdentifier) {
    	super(a_value);
        m_altIdentifier = a_altIdentifier;
    }

    public ObscuredPii(TValue a_value, AltIdGenerator<TValue> a_altIdentifierGenerator) {
    	super(a_value);
        m_altIdentifier = a_altIdentifierGenerator.obscure(a_value);
    }

    @Override
    public String toString() 
    {
        return m_altIdentifier;
    }

    public String toLoggable()
    {
        return m_altIdentifier;
    }

    @Override
    public Pii<TValue> cloneWithTransform(UnaryOperator<TValue> a_transform)
    {
        return new ObscuredPii<TValue>(a_transform.apply(exposeUnsecured()), m_altIdentifier);
    }
}
