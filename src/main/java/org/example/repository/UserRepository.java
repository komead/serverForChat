package org.example.repository;

import org.example.DatabaseManager;
import org.example.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    DatabaseManager databaseManager = DatabaseManager.getInstance();

    public User getUserByUsername(String username) {
        User user = new User();

        ResultSet resultSet = databaseManager.execute("select * from users where username = '" + username + "';");

        try {
            while (resultSet.next()) {
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }
}
