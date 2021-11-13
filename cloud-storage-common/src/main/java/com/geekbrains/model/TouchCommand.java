package com.geekbrains.model;

import lombok.Getter;

@Getter
public class TouchCommand extends AbstractCommand {
    private String newName;

    public TouchCommand(String newName) {
        this.newName = newName;
        this.type = CommandType.TOUCH;
    }
}
