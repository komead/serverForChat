package org.example;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        Gson gson = new Gson();

        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("Ожидание подключения...");
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключён");
            ClientHandler clientHandler = new ClientHandler(socket);

            // В отдельном потоке слушаем клиента
            new Thread(() -> {
                String message = "";
                while (clientHandler.isConnected()) {
                    message = clientHandler.checkMessage();

//                    HashMap<String, String> data = gson.fromJson(message, HashMap.class);
//                    if (data.containsKey("code")) {
//                        clientHandler.sendMessage(new HashMap<String, String>().put("code", "ok"));
//                    }

                    System.out.println("client: " + message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // В отдельном потоке читаем вводимые данные в консоли и отправляем их клиенту
            Thread thread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    clientHandler.sendMessage(scanner.nextLine());
                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
