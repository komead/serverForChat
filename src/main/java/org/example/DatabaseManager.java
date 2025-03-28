package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
        statement = connection.createStatement();
    }

    public void disconnect() throws SQLException {
        statement.close();
        connection.close();
    }

    public void execute(String request) throws SQLException {
        statement.execute(request);
    }
}
