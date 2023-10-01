#include "spdlog/spdlog.h"
#include "spdlog/fmt/ranges.h"
#include "spdlog/sinks/basic_file_sink.h"

static void SetupLogToFile(const std::string& a_logfilePath)
{
    auto fileLogger = spdlog::basic_logger_mt("main", a_logfilePath.c_str(), true);
    spdlog::set_default_logger(fileLogger);
    spdlog::flush_every(std::chrono::seconds(1));
}

int main()
{
    SetupLogToFile("hello_world.log");
    spdlog::info("Hello Secure World!");
}
