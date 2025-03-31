package org.example.controller;

import com.google.gson.Gson;
import org.example.ClientHandler;
import org.example.DatabaseManager;

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
                System.out.println("Подключён новый клиент");

                // В отдельном потоке слушаем клиента
                new Thread(() -> {
                    ClientHandler clientHandler = new ClientHandler();
                    clients.add(clientHandler);
                    clientHandler.connect(socket);

                    String message;
                    HashMap<String, String> messageMap;
                    // Слушаем клиента, пока он не отключился
                    while (true) {
                        try {
                            message = clientHandler.checkMessage();
                            System.out.println(message);
                        } catch (IOException e) {
                            System.out.println("Клиент " + clientHandler.getUsername() + " отключился");
                            clientHandler.finish();
                            break;
                        }
                        messageMap = gson.fromJson(message, HashMap.class);
                        // Обрабатываем операцию исходя из её кода
                        switch (messageMap.get("code")) {
                            case "login":
                                loginAction(clientHandler, messageMap);
                                break;
                            case "register":
                                registerAction(clientHandler, messageMap);
                                break;
                            case "message":
                                System.out.println(messageMap.get("text"));
                                messageAction(clientHandler, messageMap);
                                break;
                            default:
                                System.out.println("Неопознанное действие");
                                break;
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        finish();
    }

    /**
     * Действие при входе. Пытаемся войти. Собираем сообщение для клиента из результата и причины (при необходимости)
     * и отправляем.
     * @param clientHandler
     * @param messageMap
     */
    private void loginAction(ClientHandler clientHandler, HashMap<String, String> messageMap) {
        HashMap<String, String> buf = new HashMap<>();
        if (userController.login(messageMap.get("username"), messageMap.get("password"))) {
            buf.put("code", "ok");
        } else {
            buf.put("code", "deny");
            buf.put("body", "User not found");
        }
        clientHandler.setUsername(messageMap.get("username"));
        sendMessage(clientHandler, gson.toJson(buf));
    }

    /**
     * Действие при регистрации. Пытаемся зарегистрироваться. Собираем сообщение для клиента из результата
     * и причины (при необходимости) и отправляем.
     * @param clientHandler
     * @param messageMap
     */
    private void registerAction(ClientHandler clientHandler, HashMap<String, String> messageMap) {
        HashMap<String, String> buf = new HashMap<>();
        if (userController.register(messageMap.get("username"), messageMap.get("password"))) {
            buf.put("code", "ok");
        } else {
            buf.put("code", "deny");
            buf.put("body", "User is already registered");
        }
        clientHandler.setUsername(messageMap.get("username"));
        sendMessage(clientHandler, gson.toJson(buf));
    }

    /**
     * Действие для обычного сообщения. Проверяем кому адресовано данное сообщение. Если для всех, то пересылаем это
     * сообщение каждому клиенту. Если для определённого, то ищем получателя среди всех клиентов и отправляем
     * сообщение только ему.
     * @param clientHandler
     * @param messageMap
     */
    private void messageAction(ClientHandler clientHandler, HashMap<String, String> messageMap) {
        String message;
        messageMap.put("sender", clientHandler.getUsername());
        if ("all".equals(messageMap.get("receiver"))) {
            // Отправить всем
            message = gson.toJson(messageMap);
            for (ClientHandler client : clients) {
                sendMessage(client, message);
            }
        } else {
            for (ClientHandler client : clients) {
                // Отправить только для receiver
                if (client.getUsername().equals(messageMap.get("sender"))) {
                    message = gson.toJson(messageMap);
                    sendMessage(client, message);
                }
            }
        }
    }

    /**
     * Метод отправляет сообщение переданному в качестве параметра клиенту.
     * @param client
     * @param message
     */
    private void sendMessage(ClientHandler client, String message) {
        try {
            client.sendMessage(message);
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
