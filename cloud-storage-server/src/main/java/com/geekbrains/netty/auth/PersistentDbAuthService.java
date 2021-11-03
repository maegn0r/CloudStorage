package com.geekbrains.netty.auth;

import java.sql.*;

public class PersistentDbAuthService implements IAuthService {

    private static final String DB_URL = "jdbc:sqlite:auth.db";
    private Connection connection;
    private PreparedStatement checkLoginPass;

    @Override
    public void start() {
        try {
            System.out.println("Creating DB connection...");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("DB connection is created successfully");
            checkLoginPass = createCheckStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.err.println("Failed to connect to DB by URL: " + DB_URL);
            throw new RuntimeException("Failed to start auth service");
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
            System.err.printf("Failed to fetch username from DB. Login: %s; password: %s%n", login, password);
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

    private PreparedStatement createCheckStatement() throws SQLException {
        return connection.prepareStatement("SELECT * FROM 'logins' WHERE login = ? AND password = ?");
    }

}