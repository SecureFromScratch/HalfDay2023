namespace TasksServer {
    public class Task {
        private readonly string m_creator;
        private readonly string m_desc;
        private readonly bool m_isUrgent;
        
        public Task(string a_creator, bool a_isUrgent, String a_desc) {
            m_creator = a_creator;
            m_isUrgent = a_isUrgent;
            m_desc = a_desc;
        }
        
        public string Creator {
            get { return m_creator; }
        }
        
        public bool IsUrgent {
            get { return m_isUrgent; }
        }
        
        public string Description {
            get { return m_desc; }
        }
    }
}
