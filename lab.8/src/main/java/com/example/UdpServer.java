package com.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * UDP Server that sends scheduled messages to registered clients.
 *
 * How it works:
 *  - Listens on PORT for registration packets from clients.
 *  - Each client sends "REGISTER:<name>" to sign up.
 *  - Server stores client address + port.
 *  - A scheduler sends targeted messages to specific clients every few seconds.
 *
 * Run:
 *   java -cp target/classes com.example.UdpServer
 */
public class UdpServer {

    static final int PORT = 9876;
    private static final int MAX_CLIENTS = 10;
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // Registered clients storage
    private static final String[]      clientNames   = new String[MAX_CLIENTS];
    private static final InetAddress[] clientAddrs   = new InetAddress[MAX_CLIENTS];
    private static final int[]         clientPorts   = new int[MAX_CLIENTS];
    private static int clientCount = 0;

    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        System.out.println("[Server] Started on port " + PORT);
        System.out.println("[Server] Waiting for clients to register...");

        // Thread: listen for client registrations
        Thread listenerThread = new Thread(() -> {
            byte[] buffer = new byte[256];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength()).trim();

                    if (msg.startsWith("REGISTER:")) {
                        String name = msg.substring(9);
                        InetAddress addr = packet.getAddress();
                        int port = packet.getPort();
                        registerClient(name, addr, port);

                        // Confirm registration
                        String confirm = "Welcome, " + name + "! You are registered.";
                        byte[] confirmBytes = confirm.getBytes();
                        DatagramPacket reply = new DatagramPacket(
                                confirmBytes, confirmBytes.length, addr, port);
                        serverSocket.send(reply);
                    }
                } catch (Exception e) {
                    System.err.println("[Server] Listener error: " + e.getMessage());
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // Scheduler: send messages to specific clients at intervals
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

        // Task 1: every 5 seconds — send broadcast to ALL clients
        scheduler.scheduleAtFixedRate(() -> {
            String time = LocalTime.now().format(TIME_FMT);
            sendToAll(serverSocket, "[" + time + "] Server broadcast: Hello to all clients!");
        }, 5, 5, TimeUnit.SECONDS);

        // Task 2: every 8 seconds — send to client #1 specifically
        scheduler.scheduleAtFixedRate(() -> {
            String time = LocalTime.now().format(TIME_FMT);
            sendToClient(serverSocket, 0,
                    "[" + time + "] Private message for Client #1: your scheduled report is ready.");
        }, 8, 8, TimeUnit.SECONDS);

        // Task 3: every 12 seconds — send to client #2 specifically
        scheduler.scheduleAtFixedRate(() -> {
            String time = LocalTime.now().format(TIME_FMT);
            sendToClient(serverSocket, 1,
                    "[" + time + "] Private message for Client #2: reminder — check your inbox.");
        }, 12, 12, TimeUnit.SECONDS);

        // Keep main thread alive
        Thread.currentThread().join();
    }

    private static synchronized void registerClient(
            String name, InetAddress addr, int port) {
        // Check if already registered
        for (int i = 0; i < clientCount; i++) {
            if (clientNames[i].equals(name)) {
                System.out.println("[Server] Client re-registered: " + name);
                clientAddrs[i] = addr;
                clientPorts[i] = port;
                return;
            }
        }
        if (clientCount < MAX_CLIENTS) {
            clientNames[clientCount]  = name;
            clientAddrs[clientCount]  = addr;
            clientPorts[clientCount]  = port;
            clientCount++;
            System.out.println("[Server] Registered client #" + clientCount
                    + ": " + name + " @ " + addr + ":" + port);
        }
    }

    private static synchronized void sendToAll(DatagramSocket socket, String message) {
        if (clientCount == 0) return;
        for (int i = 0; i < clientCount; i++) {
            sendToClient(socket, i, message);
        }
        System.out.println("[Server] Broadcast sent to " + clientCount + " client(s).");
    }

    private static synchronized void sendToClient(
            DatagramSocket socket, int index, String message) {
        if (index >= clientCount) return;
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data, data.length, clientAddrs[index], clientPorts[index]);
            socket.send(packet);
            System.out.println("[Server] -> " + clientNames[index] + ": " + message);
        } catch (Exception e) {
            System.err.println("[Server] Send error: " + e.getMessage());
        }
    }
}
