#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#pragma comment(lib, "ws2_32.lib")

// Base TCP Socket class
class TCPSocket {
protected:
    SOCKET sock;
    WSADATA wsaData;

public:
    TCPSocket() : sock(INVALID_SOCKET) {
        WSAStartup(MAKEWORD(2, 2), &wsaData);
    }

    virtual ~TCPSocket() {
        close();
        WSACleanup();
    }

    // Operator << to send data
    TCPSocket& operator<<(const std::string& message) {
        send(sock, message.c_str(), (int)message.size(), 0);
        return *this;
    }

    // Operator >> to receive data
    TCPSocket& operator>>(std::string& message) {
        char buffer[1024] = {};
        int bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (bytes > 0) message = std::string(buffer, bytes);
        return *this;
    }

    void close() {
        if (sock != INVALID_SOCKET) {
            closesocket(sock);
            sock = INVALID_SOCKET;
        }
    }
};

// TCP Server class
class TCPServer : public TCPSocket {
    SOCKET clientSock;
public:
    TCPServer() : clientSock(INVALID_SOCKET) {}

    void startListening(int port) {
        sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

        sockaddr_in serverAddr{};
        serverAddr.sin_family = AF_INET;
        serverAddr.sin_addr.s_addr = INADDR_ANY;
        serverAddr.sin_port = htons(port);

        bind(sock, (sockaddr*)&serverAddr, sizeof(serverAddr));
        listen(sock, SOMAXCONN);

        std::cout << "Server listening on port " << port << "...\n";
    }

    void acceptConnection() {
        clientSock = accept(sock, nullptr, nullptr);
        std::cout << "Client connected!\n";
    }

    TCPServer& operator<<(const std::string& message) {
        send(clientSock, message.c_str(), (int)message.size(), 0);
        return *this;
    }

    TCPServer& operator>>(std::string& message) {
        char buffer[1024] = {};
        int bytes = recv(clientSock, buffer, sizeof(buffer) - 1, 0);
        if (bytes > 0) message = std::string(buffer, bytes);
        else message = "exit";
        return *this;
    }

    void close() {
        if (clientSock != INVALID_SOCKET) {
            closesocket(clientSock);
            clientSock = INVALID_SOCKET;
        }
        TCPSocket::close();
    }
};

int main() {
    TCPServer server;
    server.startListening(8080);
    server.acceptConnection();

    std::string message;
    while (true) {
        server >> message;
        std::cout << "Client: " << message << "\n";
        if (message == "exit") break;

        std::string reply;
        std::cout << "Server: ";
        std::getline(std::cin, reply);
        server << reply;
        if (reply == "exit") break;
    }

    server.close();
    return 0;
}
