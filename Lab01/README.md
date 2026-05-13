# Individual Work 1 — Single Thread Client-Server Application based on TCP/IP

## Description
A TCP/IP client-server application using WinSock2.
The client sends a file to the server, and the server computes its SHA-256 hash and returns it to the client.

## Task
The server receives a file from the client and returns its hash code.

## How to Build (Visual Studio / MSVC)
```
cl server.cpp /link ws2_32.lib advapi32.lib
cl client.cpp /link ws2_32.lib
```

## How to Run
Terminal 1 — Server:
```
server.exe
```
Terminal 2 — Client:
```
client.exe test.txt
```

## Expected Output
Server:
```
[Server] Listening on port 8080...
[Server] Client connected: 127.0.0.1
[Server] Received 145 bytes.
[Server] SHA-256: a3f1...
[Server] Hash sent to client.
```
Client:
```
[Client] File loaded: test.txt (145 bytes)
[Client] Connected to server 127.0.0.1:8080
[Client] File sent (145 bytes).
[Client] SHA-256 hash received from server:
         a3f1...
```

## Technologies
- C++
- WinSock2 (`ws2_32.lib`)
- Windows CryptoAPI SHA-256 (`advapi32.lib`)
