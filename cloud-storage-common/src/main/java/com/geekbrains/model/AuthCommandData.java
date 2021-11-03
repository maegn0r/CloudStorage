package com.geekbrains.model;

import lombok.Getter;

@Getter
public class AuthCommandData extends AbstractCommand {

    private final String login;
    private final String password;

    public AuthCommandData(String login, String password) {
        this.login = login;
        this.password = password;
        this.type = CommandType.AUTH;
    }
}
