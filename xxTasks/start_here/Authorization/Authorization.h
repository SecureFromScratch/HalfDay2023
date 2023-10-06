#ifndef __AUTHORIZATION_H__
#define __AUTHORIZATION_H__

#include <string>
#include <set>

class Authorization {
public:
	Authorization(const std::string &a_username, const std::set<std::string> &a_allowed)
        {
            m_username = a_username;
            m_allowed = a_allowed;
        }

        public string Username => m_username;

        public bool Allows(string a_right)
        {
            return m_allowed.Contains(a_right);
        }

        public void ThrowIfNotAllowed(string a_right)
        {
            if (!Allows(a_right))
            {
                throw new InvalidAuthException(a_right);
            }
        }

private:
	const std::string m_username;
	const std::set<std::string> m_allowed;
};

class InvalidAuthException : public std::exception
{
    private string m_right;

    public InvalidAuthException(string a_right) : base($"Unauthorized: {a_right}")
    {
        m_right = a_right;
    }

    public string Right => m_right;

    public string Explanation => "You do not have autherization to " + AuthMgr.INVALID_AUTH_EXPLANATIONS[m_right];
};

#endif
