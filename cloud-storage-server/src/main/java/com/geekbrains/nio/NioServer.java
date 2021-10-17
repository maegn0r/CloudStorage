package com.geekbrains.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;


// ls -> список файлов в текущей папке +
// cat file -> вывести на экран содержание файла +
// cd path -> перейти в папку
// touch file -> создать пустой файл

public class NioServer {

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buffer;
    private final Path rootDir = Paths.get("rootDir");
    private Path currentDir;


    public NioServer() throws Exception {
        currentDir = rootDir;
        buffer = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open(); // accept -> SocketChannel
        server.bind(new InetSocketAddress(8189));
        selector = Selector.open();
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws Exception {

        SocketChannel channel = (SocketChannel) key.channel();

        StringBuilder sb = new StringBuilder();

        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }
        parseCommand(sb.toString().trim(), channel);
    }

    private void parseCommand(String command, SocketChannel channel) throws IOException {
        if (command.startsWith("ls")) {
            String result = Files.list(currentDir).map(item -> item.getFileName().toString()).collect(Collectors.joining("\r\n"));
            sendAnswer(result, channel);
        } else if (command.startsWith("cat")) {
            doCat(command, channel);
        } else if (command.startsWith("cd")) {
            doCd(command, channel);
        } else if (command.startsWith("touch")) {
            doTouch(command, channel);
        } else {
            sendAnswer("[From server]: " + command, channel);
        }
    }

    private void doTouch(String command, SocketChannel channel) throws IOException {
        String[] arr = command.split(" ");
        if (arr.length == 2) {
            Path path = currentDir.resolve(arr[1]);
            if (Files.exists(path)) {
                String errMessage = "File already exists!";
                sendAnswer(errMessage, channel);
            } else {
                Files.createFile(path);
            }
        } else {
            String errMessage = "Wrong path!";
            sendAnswer(errMessage, channel);
        }
    }

    private void doCd(String command, SocketChannel channel) throws IOException {
        String[] arr = command.split(" ");
        if (arr.length == 2) {
            Path path = currentDir.resolve(arr[1]).normalize();
            if (Files.isDirectory(path) && Files.exists(path)) {
                if (path.startsWith(rootDir)) {
                    currentDir = path;
                }
            } else {
                String errMessage = "Directory not found!";
                sendAnswer(errMessage, channel);
            }

        } else {
            String errMessage = "Wrong path!";
            sendAnswer(errMessage, channel);
        }
    }

    private void doCat(String command, SocketChannel channel) throws IOException {
        String[] arr = command.split(" ");
        if (arr.length == 2) {
            Path path = currentDir.resolve(arr[1]);
            if (Files.isRegularFile(path)) {
                String result = String.join("\n", Files.readAllLines(path));
                sendAnswer(result, channel);
            } else {
                String errMessage = "File not found!";
                sendAnswer(errMessage, channel);
            }
        } else {
            String errMessage = "Wrong path to file!";
            sendAnswer(errMessage, channel);
        }
    }

    private void handleAccept(SelectionKey key) throws Exception {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, "Hello world!");
    }


    public static void main(String[] args) throws Exception {
        new NioServer();
    }

    private void sendAnswer(String message, SocketChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(message.concat("\r\n").getBytes(StandardCharsets.UTF_8)));
    }
}