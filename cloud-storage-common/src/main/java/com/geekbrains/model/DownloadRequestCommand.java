package com.geekbrains.model;


import lombok.Getter;

@Getter
public class DownloadRequestCommand extends AbstractCommand{
    private String fileName;

    public DownloadRequestCommand(String fileName) {
        this.fileName = fileName;
        this.type = MessageType.DOWNLOAD;
    }
}
