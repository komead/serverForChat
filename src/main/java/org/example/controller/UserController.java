package org.example.controller;

import org.example.entity.User;
import org.example.repository.UserRepository;

public class UserController {
    private UserRepository userRepository = new UserRepository();

    public boolean login(String username, String password) {
        User user = userRepository.getUserByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            return false;
        }
        return true;
    }

    public boolean register(String username, String password) {
        if (userRepository.userIsExist(username)) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        userRepository.createUser(user);
        return true;
    }
}
