package org.example.controller;

import com.google.gson.Gson;
import org.example.ClientHandler;
import org.example.DatabaseManager;
import org.example.entity.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class MainController {
    private UserController userController = new UserController();
    private DatabaseManager dbManager;
    private Vector<ClientHandler> clients = new Vector<>();

    private Gson gson = new Gson();

    public void start() {
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        // Поток, который читает вводимые в консоль сообщения
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                for (ClientHandler client : clients) {
                    sendMessage(client, scanner.nextLine());
                }
            }
        });
//        thread.setDaemon(true);
//        thread.start();

        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключён");

                // В отдельном потоке слушаем клиента
                new Thread(() -> {
                    ClientHandler clientHandler = new ClientHandler();
                    clients.add(clientHandler);
                    clientHandler.connect(socket);

                    String message;
                    HashMap<String, String> map;
                    HashMap<String, String> buf = new HashMap<>();
                    while (true) {
                        try {
                            message = clientHandler.checkMessage();
                            System.out.println(message);
                        } catch (IOException e) {
                            System.out.println("Ошибка подключения к клиенту");
                            clientHandler.finish();
                            e.printStackTrace();
                            break;
                        }
                        map = gson.fromJson(message, HashMap.class);
                        switch (map.get("code")) {
                            case "login":
                                if (userController.login(map.get("username"), map.get("password"))) {
                                    buf.put("code", "ok");
                                    message = gson.toJson(buf);
                                    sendMessage(clientHandler, message);
                                } else {
                                    buf.put("code", "deny");
                                    buf.put("body", "User not found");
                                    message = gson.toJson(buf);
                                    sendMessage(clientHandler, message);
                                }
                                break;
                            case "register":
                                if (userController.register(map.get("username"), map.get("password"))) {
                                    buf.put("code", "ok");
                                    message = gson.toJson(buf);
                                    sendMessage(clientHandler, message);
                                } else {
                                    buf.put("code", "deny");
                                    buf.put("body", "User is already registered");
                                    message = gson.toJson(buf);
                                    sendMessage(clientHandler, message);
                                }
                                break;
                            case "message":
                                break;
                            default:
                                System.out.println("Неопознанное действие");
                                break;
                        }
                        buf.clear();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        finish();
    }

    private void sendMessage(ClientHandler clientHandler, String message) {
        try {
            clientHandler.sendMessage(message);
        } catch (IOException e) {
            System.out.println("Ошибка подключения к клиенту");
            e.printStackTrace();
        }
    }

    private void finish() {
        try {
            dbManager.disconnect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
