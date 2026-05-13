# Individual Work 8 — UDP Network Application

## Description
A multi-threaded UDP application consisting of a server and clients using the `java.net` library. The server sends scheduled messages to specific registered clients.

## Task
Create multi-threaded server and client console applications using `java.net` library. The server sends messages at scheduled intervals to specific clients.

## How it works
- Client sends `REGISTER:<name>` to sign up on the server
- Server stores client address and port
- Every **5 seconds** — broadcast message to all clients
- Every **8 seconds** — private message to Client #1
- Every **12 seconds** — private message to Client #2

## Project Structure
lab8/
├── src/main/java/com/example/
│   ├── UdpServer.java
│   └── UdpClient.java
└── pom.xml

## How to Run
cd lab8
mvn compile
Terminal 1 — Server:
java -cp target/classes com.example.UdpServer
Terminal 2 — Client 1:
java -cp target/classes com.example.UdpClient Client1
Terminal 3 — Client 2:
java -cp target/classes com.example.UdpClient Client2

## Technologies
- Java 17
- Maven
- `java.net.DatagramSocket`
- `java.net.DatagramPacket`
- `java.util.concurrent.ScheduledExecutorService`
