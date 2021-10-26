package com.geekbrains.model;

import lombok.Getter;

@Getter
public class InfoMessage extends AbstractCommand {
    private String message;

    public InfoMessage(String message) {
        this.message = message;
        this.type = MessageType.INFO;
    }
}
