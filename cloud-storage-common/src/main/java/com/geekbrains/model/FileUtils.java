package com.geekbrains.model;

import com.geekbrains.model.ClientStatus;
import com.geekbrains.model.UploadCommand;
import com.geekbrains.model.UploadDataCommand;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static void uploadPart(ClientStatus clientStatus, UploadDataCommand uploadDataCommand) throws IOException {
       try (FileOutputStream fos = new FileOutputStream(clientStatus.getCurrentFileName().toFile(), true)) {
           fos.write(uploadDataCommand.getByteArray());
        }
        }

    public static void sendFile(ChannelHandlerContext ctx, ClientStatus clientStatus) {
        File file = clientStatus.getCurrentFileName().toFile();
            long size = file.length();
            String fileName = file.getName();
            int partCount = (int) ((size/1024) + 1);
            ctx.writeAndFlush(new UploadCommand(fileName,size, partCount));
            if (size>0){
                try (FileInputStream fis = new FileInputStream(file)){
                    byte[] bytebuf = new byte[1024];
                    int readBytes;
                    for (int i = 0; i < partCount; i++) {
                        readBytes = fis.read(bytebuf);
                        ctx.writeAndFlush(new UploadDataCommand(fileName, i+1,readBytes,bytebuf));
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
    }
    public static boolean touchFile (Path path) throws IOException {
        if (!Files.exists(path)){
            Files.createFile(path);
            return true;
        } return false;
    }
}
