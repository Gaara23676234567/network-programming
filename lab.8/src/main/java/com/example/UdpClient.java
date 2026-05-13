package com.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * UDP Client that registers with the server and receives scheduled messages.
 *
 * How it works:
 *  - Connects to server at SERVER_HOST:SERVER_PORT.
 *  - Sends "REGISTER:<name>" to sign up.
 *  - Listens indefinitely for incoming messages from the server.
 *
 * Run (provide a name as argument):
 *   java -cp target/classes com.example.UdpClient Client1
 *   java -cp target/classes com.example.UdpClient Client2
 */
public class UdpClient {

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = UdpServer.PORT;
    private static final int    CLIENT_PORT = 0; // OS assigns a free port
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) throws Exception {
        String clientName = (args.length > 0) ? args[0] : "Client1";

        DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
        InetAddress serverAddr = InetAddress.getByName(SERVER_HOST);

        System.out.println("[" + clientName + "] Starting, connecting to "
                + SERVER_HOST + ":" + SERVER_PORT);

        // Send REGISTER message to server
        String registerMsg = "REGISTER:" + clientName;
        byte[] regBytes = registerMsg.getBytes();
        DatagramPacket regPacket = new DatagramPacket(
                regBytes, regBytes.length, serverAddr, SERVER_PORT);
        socket.send(regPacket);
        System.out.println("[" + clientName + "] Registration sent.");

        // Listen for messages from server
        byte[] buffer = new byte[512];
        System.out.println("[" + clientName + "] Waiting for messages...");

        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(inPacket);

            String received = new String(
                    inPacket.getData(), 0, inPacket.getLength()).trim();
            String time = LocalTime.now().format(TIME_FMT);

            System.out.println("[" + clientName + "] [" + time + "] Received: " + received);
        }
    }
}
