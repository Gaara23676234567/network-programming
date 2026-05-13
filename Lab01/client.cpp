#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <string>

#pragma comment(lib, "ws2_32.lib")

#define SERVER_IP   "127.0.0.1"
#define PORT        8080
#define BUFFER_SIZE 4096

int main(int argc, char* argv[]) {
    // File to send (default: test.txt, or pass as argument)
    std::string filePath = (argc > 1) ? argv[1] : "test.txt";

    // 1. Read file
    std::ifstream inFile(filePath, std::ios::binary);
    if (!inFile.is_open()) {
        std::cerr << "[Client] Cannot open file: " << filePath << "\n";
        return 1;
    }
    std::vector<char> fileData(
        (std::istreambuf_iterator<char>(inFile)),
         std::istreambuf_iterator<char>());
    inFile.close();
    std::cout << "[Client] File loaded: " << filePath
              << " (" << fileData.size() << " bytes)\n";

    // 2. Initialize WinSock2
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "[Client] WSAStartup failed.\n";
        return 1;
    }

    // 3. Create socket
    SOCKET sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (sock == INVALID_SOCKET) {
        std::cerr << "[Client] socket() failed: " << WSAGetLastError() << "\n";
        WSACleanup();
        return 1;
    }

    // 4. Connect to server
    sockaddr_in serverAddr{};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port   = htons(PORT);
    serverAddr.sin_addr.s_addr = inet_addr(SERVER_IP);

    if (connect(sock, (sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "[Client] connect() failed: " << WSAGetLastError() << "\n";
        closesocket(sock);
        WSACleanup();
        return 1;
    }
    std::cout << "[Client] Connected to server " << SERVER_IP << ":" << PORT << "\n";

    // 5. Send file size (8 bytes)
    long long fileSize = (long long)fileData.size();
    send(sock, (char*)&fileSize, sizeof(fileSize), 0);

    // 6. Send file data
    long long sent = 0;
    while (sent < fileSize) {
        int toSend = (int)std::min((long long)BUFFER_SIZE, fileSize - sent);
        int s = send(sock, fileData.data() + sent, toSend, 0);
        if (s <= 0) break;
        sent += s;
    }
    std::cout << "[Client] File sent (" << sent << " bytes).\n";

    // 7. Receive hash length
    int hashLen = 0;
    recv(sock, (char*)&hashLen, sizeof(hashLen), MSG_WAITALL);

    // 8. Receive hash string
    std::string hash(hashLen, '\0');
    recv(sock, &hash[0], hashLen, MSG_WAITALL);

    std::cout << "[Client] SHA-256 hash received from server:\n";
    std::cout << "         " << hash << "\n";

    closesocket(sock);
    WSACleanup();
    std::cout << "[Client] Done.\n";
    return 0;
}
