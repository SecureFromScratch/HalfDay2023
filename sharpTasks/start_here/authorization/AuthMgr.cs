using Microsoft.Extensions.Logging;

namespace AuthorizationNS
{
    public static class AuthMgr
    {
        public const string URGENT_TASK = "urgenttask";
        public const string VIEW_ACTIVE = "viewactive";

        internal static readonly Dictionary<string, string> INVALID_AUTH_EXPLANATIONS = new Dictionary<string, string>
        {
            { URGENT_TASK, "mark a task as urgent" },
            { VIEW_ACTIVE, "view active tasks" }
        };

        private static readonly string AUTH_FILENAME = "auth.txt";

        public static bool IsAllowed(Authorization a_authorization, string a_right)
        {
            return a_authorization.Allows(a_right);
        }

        public static Authorization GetAuthorization(string a_username, ILogger a_logger)
        {
            ISet<string> allowed = new HashSet<string>();
            try
            {
                string[] lines = File.ReadAllLines(AUTH_FILENAME);
                foreach (string a_line in lines)
                {
                    if (string.IsNullOrWhiteSpace(a_line))
                    {
                        continue; // skip empty lines
                    }

                    string[] a_parts = a_line.Split(':');
                    if (a_parts.Length != 2)
                    {
                        a_logger.LogError("Auth line invalid: {line}", a_line);
                        allowed.Clear(); // if there's a format error I mistrust EVERYTHING
                        break;
                    }
                    else if (string.IsNullOrEmpty(a_parts[0]) || a_parts[0] == a_username)
                    {
                        allowed.Add(a_parts[1]);
                    }
                }
            }
            catch (Exception a_ex)
            {
                a_logger.LogWarning("No auth file {filename} found -or- empty, or an error happened: {ex}", AUTH_FILENAME, a_ex.Message);
            }
            return new Authorization(a_username, allowed);
        }
    }
}
