package com.geekbrains.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class UploadDataCommand extends AbstractCommand{
    private String fileName;
    private int partsCount;
    private int bufferSize;
    private byte [] byteArray;

    public UploadDataCommand(String fileName, int partsCount, int bufferSize, byte [] byteArray) {
        this.fileName = fileName;
        this.partsCount = partsCount;
        this.bufferSize = bufferSize;
        this.byteArray = Arrays.copyOf(byteArray,bufferSize);
        this.type = MessageType.UPLOAD_DATA_COMMAND;
    }
}

