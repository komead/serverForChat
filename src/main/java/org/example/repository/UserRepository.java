package org.example.repository;

import org.example.DatabaseManager;
import org.example.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    DatabaseManager databaseManager = DatabaseManager.getInstance();

    public User getUserByUsername(String username) {
        User user = new User();

        ResultSet resultSet = databaseManager.select("select * from users where username = '" + username + "';");

        try {
            if (!resultSet.next()) {
                return null;
            } else {
                do {
                    user.setId(resultSet.getInt("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    public void createUser(User user) {
        databaseManager.insert("insert into users(username, password) values('" + user.getUsername() + "','" + user.getPassword() + "');");
    }

    public boolean userIsExist(String username) {
        ResultSet resultSet = databaseManager.select("select * from users where username = '" + username + "';");

        try {
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Пользователя " + username + " не существует");
        }
        return false;
    }
}
