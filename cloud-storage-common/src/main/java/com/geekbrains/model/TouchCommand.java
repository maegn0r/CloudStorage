package com.geekbrains.model;

import lombok.Getter;

@Getter
public class TouchCommand extends AbstractCommand {
    private String newFile;

    public TouchCommand(String newFile) {
        this.newFile = newFile;
        this.type = CommandType.TOUCH;
    }
}
