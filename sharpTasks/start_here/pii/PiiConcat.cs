using System.Security.Cryptography;
using System.Text;

namespace PiiLib
{
    public class PiiConcat
    {
        struct PiiLocation
        {
            public PiiLocation(int a_offset, int a_length, string a_altId)
            {
                m_offset = a_offset;
                m_length = a_length;
                m_altId = a_altId;
            }

            /*public PiiLocation(int a_offset, int a_length)
                : this(a_offset, a_length, null)
            {
            }*/

            public int Offset
            {
                get { return m_offset; }
            }

            public int Length
            {
                get { return m_length; }
            }

            public string AltId
            {
                get { return m_altId; }
            }

            public void Push(int a_howMuch)
            {
                m_offset += a_howMuch;
            }

            private int m_offset;
            private int m_length;
            private string m_altId;
        }

        private StringBuilder m_stringBuilder = new StringBuilder();
        private List<PiiLocation> m_piiLocations = new List<PiiLocation>();
        private Func<string, string>? m_elementTransform;

        public PiiConcat() { }
        public PiiConcat(Func<string, string> a_elementTransform) {
            m_elementTransform = a_elementTransform;
        }

        public PiiConcat(string v1)
        {
            Append(v1);
        }

        public PiiConcat(string v1, Pii<string> v2)
        {
            Append(v1);
            Append(v2);
        }

        public PiiConcat(string v1, Pii<string> v2, string v3)
        {
            Append(v1);
            Append(v2);
            Append(v3);
        }

        public void Append(string a_nonclassifiedString)
        {
            if (m_elementTransform != null)
            {
                a_nonclassifiedString = m_elementTransform(a_nonclassifiedString);
            }
            m_stringBuilder.Append(a_nonclassifiedString);
        }

        public void PushPrefix(string a_prefix)
        {
            if (m_elementTransform != null)
            {
                a_prefix = m_elementTransform(a_prefix);
            }

            foreach (PiiLocation l in m_piiLocations)
            {
                l.Push(a_prefix.Length);
            }
            m_stringBuilder.Insert(0, a_prefix);
        }

        public void Append<TValue>(Pii<TValue> a_pii)
        {
            int offset = m_stringBuilder.Length;
            if (m_elementTransform == null)
            {
                m_stringBuilder.Append(a_pii.ExposeUnsecured());
            }
            else
            {
                object? raw = a_pii.ExposeUnsecured();
                string exposed = m_elementTransform((raw == null) ? "" : raw.ToString());
                m_stringBuilder.Append(exposed);
            }
            int length = m_stringBuilder.Length - offset;
            m_piiLocations.Add(new PiiLocation(offset, length, a_pii.ToString()));
        }

        public void Append<TValue>(ObscuredPii<TValue> a_pii)
        {
            int offset = m_stringBuilder.Length;
            if (m_elementTransform == null)
            {
                m_stringBuilder.Append(a_pii.ExposeUnsecured());
            }
            else
            {
                object? raw = a_pii.ExposeUnsecured();
                string exposed = m_elementTransform((raw == null) ? "" : raw.ToString());
                m_stringBuilder.Append(exposed);
            }
            int length = m_stringBuilder.Length - offset;
            m_piiLocations.Add(new PiiLocation(offset, length, a_pii.ToLoggable()));
        }

        public string ExposeUnsecured()
        {
            return m_stringBuilder.ToString();
        }

        public override string ToString() // NOTE: since debugger also calls this method (even while I debug it) it must be reentrant
        {
            StringBuilder loggableStr = new StringBuilder();

            int copyPos = 0;

            foreach (PiiLocation piiLoc in this.m_piiLocations) {
                int nonPiiLength = piiLoc.Offset - copyPos;
                char[] nonPiiPart = new char[nonPiiLength];
                m_stringBuilder.CopyTo(copyPos, nonPiiPart, 0, nonPiiLength);
                loggableStr.Append(nonPiiPart);
                loggableStr.Append(piiLoc.AltId);
                copyPos = piiLoc.Offset + piiLoc.Length;
            }

            // copy last part
            char[] rest = new char[m_stringBuilder.Length - copyPos];
            m_stringBuilder.CopyTo(copyPos, rest, 0, rest.Length);
            loggableStr.Append(rest);

            return loggableStr.ToString();
        }
    }
}
