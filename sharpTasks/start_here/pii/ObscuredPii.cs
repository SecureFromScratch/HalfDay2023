using System;
using System.Collections.Generic;
using System.Text;

namespace PiiLib
{
    public class ObscuredPii<TValue> : Pii<TValue>
    {
        public delegate string Obscurer(TValue a_value);

        private string m_altIdentifier;

        public ObscuredPii(TValue a_value, string a_altIdentifier)
            : base(a_value)
        {
            m_altIdentifier = a_altIdentifier;
        }

        public ObscuredPii(TValue a_value, Obscurer a_obscuringFunction)
            : base(a_value)
        {
            m_altIdentifier = a_obscuringFunction(a_value);
        }

        public override string ToString() 
        {
            return m_altIdentifier;
        }

        public string ToLoggable()
        {
            return m_altIdentifier;
        }

        public override Pii<TValue> CloneWithTransform(Func<TValue, TValue> a_transform)
        {
            return new ObscuredPii<TValue>(a_transform(ExposeUnsecured()), m_altIdentifier);
        }
    }
}
