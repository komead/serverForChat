package org.example;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnector {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean authorized = false;
    private String username;

    public void connect(Socket socket) {
        try {
            this.socket = socket;
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String checkMessage() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        // Читаем полученную строку
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteStream.write(buffer, 0, bytesRead);

            if (buffer[bytesRead - 1] == '\n') {
                break;
            }
        }

        String receivedMessage = byteStream.toString(StandardCharsets.UTF_8).trim();

        return receivedMessage;
    }

    public void finish() {
        try {
            if (inputStream != null)
                inputStream.close();

            if (outputStream != null)
                outputStream.close();

            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        message += '\n';
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(message.getBytes(StandardCharsets.UTF_8));

        outputStream.write(byteStream.toByteArray());
        outputStream.flush();
    }

    public boolean isConnected() throws ConnectException {
        if (socket == null)
            throw new NullPointerException("Клиент недоступен");

        if (socket.isClosed())
            throw new ConnectException("Нет соединения с клиентом");

        return true;
    }

    public void reconnect() throws IOException {
        finish();
        connect(socket);
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
