package com.geekbrains.model;

import lombok.Getter;

@Getter
public class ChangeDirCommand extends AbstractCommand{
    private String destinationDir;

    public ChangeDirCommand(String destinationDir) {
        this.destinationDir = destinationDir;
        this.type = MessageType.CHANGE_DIR;
    }
}
