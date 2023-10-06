#include <iostream>
#include <thread>
#include <chrono>
#include "SimpleNetMT.h"
#include "UrlPathReader.h"

using namespace std::chrono_literals;

int main()
{
	simplenet::SimpleNetMT net{4010};
	while (true)
	{
		simplenet::SimpleNetMT::Connection c = net.WaitForConnection();

		try 
		{
			/* before c++17
			std::string path;
			bool isHttp;
			std::tie(path, isHttp) = simplenet::TryReadUrlPath(c);
			*/
			auto [path, isHttp] = simplenet::TryReadUrlPath(c);
			// line above is same as writing:
			// std::pair<std::string, bool> result = simplenet::TryReadUrlPath(c);
			// std::string path = result.first;
			// bool isHttp = result.second;
			if (isHttp)
			{
				if (path.find("yariv") != std::string::npos)
				{
					simplenet::EmitHttpResponse(c, "Wow Yariv! So glad you called!");
				}
				else
				{
					simplenet::EmitHttpResponse(c, "Bummer. I hoped you were Yariv");
				}
			}
			else
			{
				c.Write("Welcome!\n");

				//while (true)
				//{
					std::string s = c.ReadLine();
					std::cout << "GOT: {" << s << "}\n";
					c.Write("1Get back " + s + "\n");
					c.Write("2Get back " + s + "\n");
					c.Write("3Get back " + s + "\n");
					c.Write("4Get back " + s + "\n");
					c.Write("5Get back " + s + "\n");
					c.Write("6Get back " + s + "\n");
					//std::this_thread::sleep_for(10000ms);
				//}
			}
		}
		catch (const simplenet::SimpleNetMT::ConnectionClosed&)
		{
			std::cout << "CLOSED\n";
		}
	}
}
