package com.geekbrains.model;

import lombok.Getter;

@Getter
public class DeleteCommand extends AbstractCommand{
    private String fileName;
    public DeleteCommand(String selected) {
        this.fileName = selected;
        this.type = CommandType.DELETE;
    }
}
