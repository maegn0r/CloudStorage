package com.geekbrains.netty.auth;



public interface IAuthService {

    default void start() {
        // Do nothing
    };

    boolean checkLoginAndPassword(String login, String password);

    default void stop() {
        // Do nothing
    }

}
