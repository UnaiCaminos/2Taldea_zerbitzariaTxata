package com.example.zerbitzaratxata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Bezeroa {

    private Socket socket;
    BufferedReader in;
    PrintWriter out;

    public Bezeroa(Socket socket) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socketarekin arazoak daude");
        }
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean konektatutaDago() {
        return socket != null && !socket.isClosed();
    }

    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Mensajea ezin da hutsik egon.");
        }
        out.println(message);
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public void closeConnection() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }
}