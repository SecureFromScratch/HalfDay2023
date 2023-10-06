namespace Utils
{
    public static class Optional
    {
        public static Optional<T> Empty<T>()
        {
            return new Optional<T>();
        }
    }

    public class Optional<T>
    {
        private readonly T? m_value;
        private readonly bool m_present;

        public Optional()
        {
            m_present = false;
        }

        public Optional(T value)
        {
            m_value = value;
            m_present = true;
        }

        public bool IsPresent()
        {
            return m_present;
        }

        public T Get()
        {
            if (!m_present || m_value == null) // null check added to remove warning of possible null value
            {
                throw new InvalidOperationException("No value present");
            }
            return m_value;
        }
    }
}
