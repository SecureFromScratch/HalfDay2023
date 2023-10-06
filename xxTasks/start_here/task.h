#ifndef __TASK_H__
#define __TASK_H__

#pragma once

namespace tasksserver {

class Task {
public:
    Task(const std::string &a_creator, bool a_isUrgent, const std::string &a_desc) 
        : m_creator(a_creator)
        , m_isUrgent(a_isUrgent)
        , m_desc(a_desc)
    {
    }
    
    const std::string &GetCreator() const {
        return m_creator;
    }
    
    bool IsUrgent() const {
        return m_isUrgent;
    }
    
    const std::string &GetDescription() const {
        return m_desc;
    }

private:
    const std::string m_creator;
    const std::string m_desc;
    const bool m_isUrgent;
};

} // tasksserver

#endif
