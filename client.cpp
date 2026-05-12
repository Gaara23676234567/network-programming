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

// TCP Client class
class TCPClient : public TCPSocket {
public:
    void connectTo(const std::string& ip, int port) {
        sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

        sockaddr_in serverAddr{};
        serverAddr.sin_family = AF_INET;
        serverAddr.sin_port = htons(port);
        inet_pton(AF_INET, ip.c_str(), &serverAddr.sin_addr);

        if (connect(sock, (sockaddr*)&serverAddr, sizeof(serverAddr)) == 0) {
            std::cout << "Connected to server " << ip << ":" << port << "\n";
        } else {
            std::cout << "Connection failed!\n";
        }
    }
};

int main() {
    TCPClient client;
    client.connectTo("127.0.0.1", 8080);

    std::string message;
    while (true) {
        std::cout << "Client: ";
        std::getline(std::cin, message);
        client << message;
        if (message == "exit") break;

        std::string reply;
        client >> reply;
        std::cout << "Server: " << reply << "\n";
        if (reply == "exit") break;
    }

    client.close();
    return 0;
}
