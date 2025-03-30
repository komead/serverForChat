package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    private boolean authorized = false;

    public void connect(Socket socket) {
        try {
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String checkMessage() throws IOException {
        String receivedMessage = "";
        receivedMessage = inputStream.readUTF();

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
        outputStream.writeUTF(message);
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
}
