using System;
using Microsoft.Extensions.Logging;

namespace HelloSecureSharpWorld
{
    class HelloSecureWorld
    {
        static ILoggerFactory s_loggerFactory = LoggerFactory.Create(builder =>
        {
            builder.AddDebug();
        });

        private static ILogger<T> CreateLogger<T>()
        {
            ILogger<T> logger = s_loggerFactory.CreateLogger<T>();
            return logger;
        }

        static ILogger<HelloSecureWorld> theLogger = CreateLogger<HelloSecureWorld>();

        static void Main(string[] args)
        {
            theLogger.LogInformation("Hello Secure World!");
        }
    }
}
