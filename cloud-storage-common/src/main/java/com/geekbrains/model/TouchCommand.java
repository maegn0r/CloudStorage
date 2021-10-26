package com.geekbrains.model;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class TouchCommand extends AbstractCommand {
    private String newFile;

    public TouchCommand(String newFile) {
        this.newFile = newFile;
        this.type = MessageType.TOUCH;
    }
}
