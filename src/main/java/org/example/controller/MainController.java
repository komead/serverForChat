package org.example.controller;

import org.example.ClientHandler;
import org.example.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MainController {
    public void start() {
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("РћР¶РёРґР°РЅРёРµ РїРѕРґРєР»СЋС‡РµРЅРёСЏ...");
            Socket socket = serverSocket.accept();
            System.out.println("РљР»РёРµРЅС‚ РїРѕРґРєР»СЋС‡С‘РЅ");
            ClientHandler clientHandler = new ClientHandler(socket);

            // Р’ РѕС‚РґРµР»СЊРЅРѕРј РїРѕС‚РѕРєРµ СЃР»СѓС€Р°РµРј РєР»РёРµРЅС‚Р°
            new Thread(() -> {
                String message = "";
                while (true) {
                    message = clientHandler.checkMessage();

                    // Р•СЃР»Рё РєР»РёРµРЅС‚ РѕС‚РєР»СЋС‡Р°РµС‚СЃСЏ, С‚Рѕ Р±РѕР»СЊС€Рµ РµРіРѕ РЅРµ СЃР»СѓС€Р°РµРј
                    if (message.equals("/end")) {
                        System.out.println("РљР»РёРµРЅС‚ РѕС‚РєР»СЋС‡РёР»СЃСЏ!");
                        clientHandler.finish();
                        break;
                    }

                    System.out.println("client: " + message);
                }
            }).start();

            // Р’ РѕС‚РґРµР»СЊРЅРѕРј РїРѕС‚РѕРєРµ С‡РёС‚Р°РµРј РІРІРѕРґРёРјС‹Рµ РґР°РЅРЅС‹Рµ РІ РєРѕРЅСЃРѕР»Рё Рё РѕС‚РїСЂР°РІР»СЏРµРј РёС… РєР»РёРµРЅС‚Сѓ
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
