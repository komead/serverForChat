package org.example.controller;

import org.example.entity.User;
import org.example.repository.UserRepository;

public class UserController {
    private UserRepository userRepository = new UserRepository();

    public void login(User user) {
        User savedUser = userRepository.getUserByUsername(user.getUsername());

        if (user.getPassword().equals(savedUser.getPassword())) {
            System.out.println("Login successful");
        }
    }
}
