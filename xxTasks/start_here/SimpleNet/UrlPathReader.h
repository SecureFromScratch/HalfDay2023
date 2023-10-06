#pragma once

#include "SimpleNetMT.h"
#include <utility>

namespace simplenet
{
std::pair<std::string, bool> TryReadUrlPath(SimpleNetMT::Connection &a_connection);
void EmitHttpResponse(SimpleNetMT::Connection &a_connection, const std::string& a_completeResponse);
} // simplenet
