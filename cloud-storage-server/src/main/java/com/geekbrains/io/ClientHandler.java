package com.geekbrains.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static int counter = 0;
    private final String userName;
    private final Server server;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final String userDirectory;

    public ClientHandler(Socket socket, Server server) throws Exception {
        this.server = server;
        counter++;
        userName = "User" + counter;
        userDirectory = "rootDir" + "\\" + userName;
        checkDir();
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = dis.readUTF();
                parseCommand(msg);
            }
        } catch (Exception e) {
            System.err.println("Connection was broken");
            e.printStackTrace();
        }
    }

    private void parseCommand(String msg) throws Exception {
        if (msg.startsWith("/upload")) {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            File file = new File(userDirectory + "\\" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[8192];
            int readBytes;
            while (true) {
                if (file.length() == fileSize) break;
                readBytes = dis.read(buf);
                fos.write(buf, 0, readBytes);
            }
        }
    }

    private void checkDir() {
        File userDir = new File(userDirectory);
        if (!userDir.exists()) {
            userDir.mkdir();
        }
    }

}