package Pii;

import java.util.function.UnaryOperator;

public class Pii<TValue> {
	private TValue m_sensativeValue;
	
	public Pii(TValue a_sensativeValue) {
		m_sensativeValue = a_sensativeValue;
	}

    public final TValue exposeUnsecured() {
        return m_sensativeValue;
    }

    public final void replaceValue(TValue a_newValue) {
    	m_sensativeValue = a_newValue;
    }

    @Override
    public String toString() {
    	return toLoggable();
    }

    public String toLoggable() {
        return "******";
    }
    
    public final void applyTransform(UnaryOperator<TValue> a_transform) {
    	m_sensativeValue = a_transform.apply(m_sensativeValue);
    }

    public Pii<TValue> cloneWithTransform(UnaryOperator<TValue> a_transform) {
        return new Pii<TValue>(a_transform.apply(m_sensativeValue));
    }
}
