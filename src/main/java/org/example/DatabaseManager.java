package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url = "jdbc:mysql://localhost:3306/chat";
    private final String user = "root";
    private final String password = "root";

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
    }

    public void disconnect() throws SQLException {
        connection.close();
    }
}
