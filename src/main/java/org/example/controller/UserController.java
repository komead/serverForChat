package org.example.controller;

import org.example.entity.User;
import org.example.enums.OperationCode;
import org.example.repository.UserRepository;

import java.util.HashMap;

public class UserController {
    private UserRepository userRepository = new UserRepository();

    public HashMap<String, String> login(String username, String password) {
        HashMap<String, String> buf = new HashMap<>();

        User user = userRepository.getUserByUsername(username);

        if (user == null) {
            buf.put("code", OperationCode.ACCESS_DENIED.stringValue());
            buf.put("body", "Пользователь не найден");
            return buf;
        }

        if (!user.getPassword().equals(password)) {
            buf.put("code", OperationCode.ACCESS_DENIED.stringValue());
            buf.put("body", "Неверный пароль");
            return buf;
        }

        buf.put("code", OperationCode.ACCESS_GRANTED.stringValue());
        return buf;
    }

    public HashMap<String, String> register(String username, String password) {
        HashMap<String, String> buf = new HashMap<>();

        if (userRepository.userIsExist(username)) {
            buf.put("code", OperationCode.ACCESS_DENIED.stringValue());
            buf.put("body", "Пользователь уже зарегистрирован");
            return buf;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        userRepository.createUser(user);

        buf.put("code", OperationCode.ACCESS_GRANTED.stringValue());
        return buf;
    }
}
