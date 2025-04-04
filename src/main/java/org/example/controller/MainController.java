package org.example.controller;

import com.google.gson.Gson;
import org.example.ClientConnector;
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
    private Vector<ClientConnector> clients = new Vector<>();

    private Gson gson = new Gson();

    public void start() {
        dbManager = DatabaseManager.getInstance();
        dbManager.connect();

        // Поток, который читает вводимые в консоль сообщения
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                for (ClientConnector client : clients) {
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
                    ClientConnector clientConnector = new ClientConnector();
                    clientConnector.connect(socket);

                    String message;
                    HashMap<String, String> messageMap;
                    // Слушаем клиента, пока он не отключился
                    while (true) {
                        try {
                            message = clientConnector.checkMessage();
                            System.out.println(message);
                        } catch (IOException e) {
                            System.out.println("Клиент " + clientConnector.getUsername() + " отключился");
                            logoutAction(clientConnector);
                            break;
                        }
                        messageMap = gson.fromJson(message, HashMap.class);
                        // Обрабатываем операцию исходя из её кода
                        switch (messageMap.get("code")) {
                            case "login":
                                loginAction(clientConnector, messageMap);
                                break;
                            case "register":
                                registerAction(clientConnector, messageMap);
                                break;
                            case "message":
                                System.out.println(messageMap.get("text"));
                                messageAction(clientConnector, messageMap);
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
     * @param clientConnector
     * @param messageMap
     */
    private void loginAction(ClientConnector clientConnector, HashMap<String, String> messageMap) {
        HashMap<String, String> buf = new HashMap<>();

        for (ClientConnector client : clients) {
            if (messageMap.get("username").equals(client.getUsername())) {
                System.out.println("Такой клиент уже подключён");
                buf.put("code", "deny");
                buf.put("body", "User is already logged in");

                sendMessage(clientConnector, gson.toJson(buf));
            }
        }

        if (userController.login(messageMap.get("username"), messageMap.get("password"))) {
            buf.put("code", "ok");
            clientConnector.setUsername(messageMap.get("username"));
            clientConnector.setAuthorized(true);
        } else {
            buf.put("code", "deny");
            buf.put("body", "User not found");
        }
        sendMessage(clientConnector, gson.toJson(buf));

        if (buf.get("code").equals("ok")) {
            clients.add(clientConnector);
            sendUsersList();
        }
    }

    /**
     * Действие при регистрации. Пытаемся зарегистрироваться. Собираем сообщение для клиента из результата
     * и причины (при необходимости) и отправляем.
     * @param clientConnector
     * @param messageMap
     */
    private void registerAction(ClientConnector clientConnector, HashMap<String, String> messageMap) {
        HashMap<String, String> buf = new HashMap<>();
        if (userController.register(messageMap.get("username"), messageMap.get("password"))) {
            buf.put("code", "ok");
            clientConnector.setUsername(messageMap.get("username"));
            clientConnector.setAuthorized(true);
        } else {
            buf.put("code", "deny");
            buf.put("body", "User is already registered");
        }
        sendMessage(clientConnector, gson.toJson(buf));

        if (buf.get("code").equals("ok")) {
            clients.add(clientConnector);
            sendUsersList();
        }
    }

    /**
     * Действие для обычного сообщения. Проверяем кому адресовано данное сообщение. Если для всех, то пересылаем это
     * сообщение каждому клиенту. Если для определённого, то ищем получателя среди всех клиентов и отправляем
     * сообщение только ему.
     * @param clientConnector
     * @param messageMap
     */
    private void messageAction(ClientConnector clientConnector, HashMap<String, String> messageMap) {
        String message;
        messageMap.put("sender", clientConnector.getUsername());
        if ("all".equals(messageMap.get("receiver"))) {
            // Отправить всем
            message = gson.toJson(messageMap);
            for (ClientConnector client : clients) {
                sendMessage(client, message);
            }
        } else {
            for (ClientConnector client : clients) {
                // Отправить только для receiver
                if (client.getUsername().equals(messageMap.get("receiver"))) {
                    message = gson.toJson(messageMap);
                    sendMessage(client, message);
                }
            }
        }
    }

    /**
     * Метод закрывает соединение с клиентом, удаляет его из списка клиентов.
     * @param clientConnector
     */
    private void logoutAction(ClientConnector clientConnector) {
        clientConnector.finish();
        clientConnector.setAuthorized(false);
        clients.remove(clientConnector);
        sendUsersList();
    }

    private void sendUsersList() {
        HashMap<String, String> buf = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();

        for (ClientConnector client : clients) {
            stringBuilder.append(client.getUsername() + ',');
        }
        buf.put("code", "usersList");
        buf.put("users", stringBuilder.toString());

        for (ClientConnector client : clients) {
            sendMessage(client, gson.toJson(buf));
        }
    }

    /**
     * Метод отправляет сообщение переданному в качестве параметра клиенту.
     * @param client
     * @param message
     */
    private void sendMessage(ClientConnector client, String message) {
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
