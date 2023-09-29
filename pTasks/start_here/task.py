class Task:
    def __init__(self, creator, is_urgent, description):
        self.m_creator = creator
        self.m_is_urgent = is_urgent
        self.m_description = description

    def get_creator(self):
        return self.m_creator

    def is_urgent(self):
        return self.m_is_urgent

    def get_description(self):
        return self.m_description
