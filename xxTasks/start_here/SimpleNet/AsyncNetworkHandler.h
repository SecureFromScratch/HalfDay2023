#ifndef __AsyncNetworkHandler__
#define __AsyncNetworkHandler__

#ifdef _WIN32
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <windows.h>
typedef FD_SET FD_SET_TYPE;
#else
#include <errno.h>
#include <sys/socket.h>
#include <sys/select.h>
#define SOCKET int
#define ULONG unsigned long
typedef fd_set FD_SET_TYPE;
#endif

#include <functional>
#include <memory>
#include <vector>
#include <string>

namespace simplenet
{

class AsyncNetworkHandler
{
public:
	using SocketId = size_t;
	using Callback = std::function<void(SocketId)>;

private:
	static const size_t READ_DATA_BUFSIZE = 1024;
	static const size_t WRITE_DATA_BUFSIZE = 8192 * 6;

	struct SOCKET_INFORMATION {
		SOCKET_INFORMATION(SOCKET _s) : readBuffer{}, writeBuffer{}, writeLen(0), Socket(_s), BytesRECV(0), bufferMaybeReady(false) { }
		char readBuffer[READ_DATA_BUFSIZE];
		char writeBuffer[WRITE_DATA_BUFSIZE];
		size_t writeLen;
		SOCKET Socket;
		size_t BytesRECV;
		bool bufferMaybeReady;
	};

	enum { USERS_LISTENER, NUM_LISTENERS };

	static void InitWsa();
	static void InitListener(SOCKET *_socket, unsigned short _port);

public:
	AsyncNetworkHandler(unsigned short _usersPort, Callback _newConnectionFunc, Callback _closedConnectionFunc);
	AsyncNetworkHandler(const AsyncNetworkHandler&) = delete;
	AsyncNetworkHandler& operator=(const AsyncNetworkHandler&) = delete;

	bool GetInput(size_t *_socketid, std::string *_input, unsigned long _timeoutMillisecs);
	void GetInput(size_t *_socketid, std::string *_input);
	void PutOutput(size_t _socketid, const std::string &_output);
	void CloseConnection(size_t _socketid);

private:
	void Init();
	size_t CreateSocketInformation(SOCKET s);
	void FreeSocketInformation(size_t Index);
	bool FillInputString(SOCKET_INFORMATION &_socketInfo, size_t _prevBytes, std::string *_input);

	unsigned short m_usersPort;
	unsigned short m_remoteServerPort;
	//size_t m_numListeners;
	size_t m_totalSockets;

	// from here they are initialized by Init()
	Callback m_newConnectionFunc;
	Callback m_closedConnectionFunc;
	SOCKET m_listenSocket;
	std::unique_ptr<SOCKET_INFORMATION> m_socketArray[FD_SETSIZE];
	FD_SET_TYPE m_lastReadSet;
	int m_nextReadSocketToTraverse;
	size_t m_totalSocketsReady;
	//std::vector<size_t> m_virtualSocketIdToRealSocketId;
};

} // simplenet

#endif
