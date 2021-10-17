package com.geekbrains.io;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server {

    private final ConcurrentLinkedDeque<ClientHandler> clients;

    public Server() {
        File rootDir = new File("rootDir");
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }
        ;
        clients = new ConcurrentLinkedDeque<>();
        try (ServerSocket server = new ServerSocket(8189)) {
            System.out.println("Server started...");
            while (true) {
                Socket socket = server.accept();
                System.out.println("Client accepted");
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}