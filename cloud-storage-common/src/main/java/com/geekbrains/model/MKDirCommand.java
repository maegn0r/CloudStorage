package com.geekbrains.model;

import lombok.Getter;

@Getter
public class MKDirCommand extends AbstractCommand{
    private String newDir;

    public MKDirCommand(String newDir) {
        this.newDir = newDir;
        this.type = CommandType.MK_DIR;
    }
}
