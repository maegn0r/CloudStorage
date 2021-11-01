package com.geekbrains.model;

import lombok.Getter;

@Getter
public class UploadCommand extends AbstractCommand{
    private String fileName;
    private long fileSize;
    private int partsCount;


    public UploadCommand(String fileName, long fileSize, int partsCount) {
        this.type = CommandType.UPLOAD;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.partsCount = partsCount;

    }
}
