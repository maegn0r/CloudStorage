package com.geekbrains.model;

import lombok.Getter;

@Getter
public class AuthCommandData extends AbstractCommand {

    private final String login;
    private final String password;
    private boolean isNewUser;

    public AuthCommandData(String login, String password, boolean isNewUser) {
        this.login = login;
        this.password = password;
        this.type = CommandType.AUTH;
        this.isNewUser = isNewUser;
    }
}
