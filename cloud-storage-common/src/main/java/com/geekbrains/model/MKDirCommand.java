package com.geekbrains.model;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class MKDirCommand extends AbstractCommand{
    private String newDir;

    public MKDirCommand(String newDir) {
        this.newDir = newDir;
        this.type = MessageType.MK_DIR;
    }
}
