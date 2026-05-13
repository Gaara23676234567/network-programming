#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include <wincrypt.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <sstream>
#include <iomanip>

#pragma comment(lib, "ws2_32.lib")
#pragma comment(lib, "advapi32.lib")

#define PORT        8080
#define BUFFER_SIZE 4096

// Compute SHA-256 hash of data using Windows CryptoAPI
std::string computeSHA256(const std::vector<char>& data) {
    HCRYPTPROV hProv = 0;
    HCRYPTHASH hHash = 0;
    std::string result;

    if (!CryptAcquireContext(&hProv, NULL, NULL, PROV_RSA_AES, CRYPT_VERIFYCONTEXT)) {
        return "ERROR: CryptAcquireContext failed";
    }
    if (!CryptCreateHash(hProv, CALG_SHA_256, 0, 0, &hHash)) {
        CryptReleaseContext(hProv, 0);
        return "ERROR: CryptCreateHash failed";
    }
    if (!CryptHashData(hHash, (BYTE*)data.data(), (DWORD)data.size(), 0)) {
        CryptDestroyHash(hHash);
        CryptReleaseContext(hProv, 0);
        return "ERROR: CryptHashData failed";
    }

    DWORD hashLen = 32; // SHA-256 = 32 bytes
    std::vector<BYTE> hashBytes(hashLen);
    if (!CryptGetHashParam(hHash, HP_HASHVAL, hashBytes.data(), &hashLen, 0)) {
        CryptDestroyHash(hHash);
        CryptReleaseContext(hProv, 0);
        return "ERROR: CryptGetHashParam failed";
    }

    std::ostringstream oss;
    for (BYTE b : hashBytes) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (int)b;
    }
    result = oss.str();

    CryptDestroyHash(hHash);
    CryptReleaseContext(hProv, 0);
    return result;
}

int main() {
    // 1. Initialize WinSock2
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "[Server] WSAStartup failed.\n";
        return 1;
    }

    // 2. Create listening socket
    SOCKET listenSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (listenSock == INVALID_SOCKET) {
        std::cerr << "[Server] socket() failed: " << WSAGetLastError() << "\n";
        WSACleanup();
        return 1;
    }

    // 3. Bind to port
    sockaddr_in serverAddr{};
    serverAddr.sin_family      = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port        = htons(PORT);

    if (bind(listenSock, (sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "[Server] bind() failed: " << WSAGetLastError() << "\n";
        closesocket(listenSock);
        WSACleanup();
        return 1;
    }

    // 4. Listen
    listen(listenSock, SOMAXCONN);
    std::cout << "[Server] Listening on port " << PORT << "...\n";

    // 5. Accept client
    sockaddr_in clientAddr{};
    int clientAddrLen = sizeof(clientAddr);
    SOCKET clientSock = accept(listenSock, (sockaddr*)&clientAddr, &clientAddrLen);
    if (clientSock == INVALID_SOCKET) {
        std::cerr << "[Server] accept() failed: " << WSAGetLastError() << "\n";
        closesocket(listenSock);
        WSACleanup();
        return 1;
    }
    std::cout << "[Server] Client connected: "
              << inet_ntoa(clientAddr.sin_addr) << "\n";

    // 6. Receive file size (8 bytes)
    long long fileSize = 0;
    recv(clientSock, (char*)&fileSize, sizeof(fileSize), MSG_WAITALL);
    std::cout << "[Server] Expecting file of " << fileSize << " bytes.\n";

    // 7. Receive file data
    std::vector<char> fileData(fileSize);
    long long received = 0;
    char buf[BUFFER_SIZE];
    while (received < fileSize) {
        int toRead = (int)std::min((long long)BUFFER_SIZE, fileSize - received);
        int r = recv(clientSock, buf, toRead, 0);
        if (r <= 0) break;
        memcpy(fileData.data() + received, buf, r);
        received += r;
    }
    std::cout << "[Server] Received " << received << " bytes.\n";

    // 8. Save received file
    std::ofstream outFile("received_file.bin", std::ios::binary);
    outFile.write(fileData.data(), fileData.size());
    outFile.close();
    std::cout << "[Server] File saved as received_file.bin\n";

    // 9. Compute SHA-256 hash
    std::string hash = computeSHA256(fileData);
    std::cout << "[Server] SHA-256: " << hash << "\n";

    // 10. Send hash back to client
    int hashLen = (int)hash.size();
    send(clientSock, (char*)&hashLen, sizeof(hashLen), 0);
    send(clientSock, hash.c_str(), hashLen, 0);
    std::cout << "[Server] Hash sent to client.\n";

    closesocket(clientSock);
    closesocket(listenSock);
    WSACleanup();
    std::cout << "[Server] Done.\n";
    return 0;
}
