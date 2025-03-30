package org.example;

import java.sql.*;

public class DatabaseManager {
    private final String url = "jdbc:mysql://localhost:3306/chat";
    private final String user = "root";
    private final String password = "root";

    private Connection connection;
    private Statement statement;

    private static class DatabaseManagerHolder {
        private static final DatabaseManager instance = new DatabaseManager();
    }

    public static DatabaseManager getInstance() {
        return DatabaseManagerHolder.instance;
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws SQLException {
        statement.close();
        connection.close();
    }

    public ResultSet execute(String request) {
        ResultSet result = null;
        try {
            result = statement.executeQuery(request);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
