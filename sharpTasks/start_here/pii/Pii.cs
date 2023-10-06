using System;

namespace PiiLib
{
    public class Pii<TValue>
    {
        private TValue m_value;
        
        public Pii(TValue a_value)
        {
            m_value = a_value;
        }

        public TValue ExposeUnsecured()
        {
            return m_value;
        }

        public void ReplaceValue(TValue a_newValue)
        {
            m_value = a_newValue;
        }

        public override string ToString()
        {
            return "******";
        }

        public void ApplyTransform(Func<TValue, TValue> a_transform)
        {
            m_value = a_transform(m_value);
        }

        public virtual Pii<TValue> CloneWithTransform(Func<TValue, TValue> a_transform)
        {
            return new Pii<TValue>(a_transform(m_value));
        }
    }
}
