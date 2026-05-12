# Lab 1 — TCP Client/Server Application

## Description

Simple TCP client/server application using Winsock2.  
Both client and server use overloaded `<<` and `>>` operators for sending/receiving string messages.

## How to Build

```bash
mkdir build && cd build
cmake ..
cmake --build .
```

## How to Run

1. Run `server.exe` first
2. Run `client.exe` in a separate terminal
3. Type messages in the client terminal, server replies back
4. Type `exit` to end the session

## Architecture

- `TCPSocket` — base class: initializes WSA, overloads `<<` and `>>`
- `TCPServer` — listens on port 8080, accepts one client
- `TCPClient` — connects to server via `127.0.0.1:8080`
