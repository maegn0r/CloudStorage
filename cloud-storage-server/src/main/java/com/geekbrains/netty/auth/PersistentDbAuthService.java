package com.geekbrains.netty.auth;

import java.sql.*;

public class PersistentDbAuthService implements IAuthService {

    private static final String DB_URL = "jdbc:sqlite:auth.db";
    private Connection connection;
    private PreparedStatement checkLoginPass;
    private PreparedStatement createNewUser;

    @Override
    public void start() {
        try {
            System.out.println("Выполняем соединение с базой данных...");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Соединение с базой данных установлено.");
            checkLoginPass = createCheckStatement();
            createNewUser = createNewUserStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.err.println("Не удалось подсоединиться к базе данных по адресу: " + DB_URL);
            throw new RuntimeException("Ошибка аутентификации");
        }
    }


    @Override
    public boolean checkLoginAndPassword(String login, String password) {
        boolean result = false;
        try {
            checkLoginPass.setString(1, login);
            checkLoginPass.setString(2, password);
            ResultSet resultSet = checkLoginPass.executeQuery();

            while (resultSet.next()) {
                result = true;
                break;
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.err.printf("Не найден пользователь в базе данных. Login: %s; password: %s%n", login, password);
        }
        return result;
    }

    @Override
    public void stop() {
        if (connection != null) {
            try {
                System.out.println("Closing DB connection");
                connection.close();
                System.out.println("DB connection is closed");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                System.err.println("Failed to close connection to DB by URL: " + DB_URL);
                throw new RuntimeException("Failed to stop auth service");
            }
        }
    }

    @Override
    public boolean createNewUser(String login, String password) {
        boolean result = false;
        try {
            createNewUser.setString(1, login);
            createNewUser.setString(2, password);
            result = createNewUser.executeUpdate() == 1;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.err.printf("Произошла ошибка при создании пользователя. Login: %s; password: %s%n", login, password);
        }

        return result;
    }

    private PreparedStatement createNewUserStatement() throws SQLException {
        return connection.prepareStatement("INSERT INTO 'logins' (login,password) VALUES (?,?)");

    }

    private PreparedStatement createCheckStatement() throws SQLException {
        return connection.prepareStatement("SELECT * FROM 'logins' WHERE login = ? AND password = ?");
    }

}