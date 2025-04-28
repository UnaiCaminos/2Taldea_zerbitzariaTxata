package com.example.zerbitzaratxata;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Bezeroa {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private List<String> mensajes;

    public Bezeroa(Socket socket) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socketarekin arazoak daude");
        }
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.dataIn = new DataInputStream(socket.getInputStream());
        this.dataOut = new DataOutputStream(socket.getOutputStream());
        this.mensajes = new ArrayList<>();
    }

    public boolean konektatutaDago() {
        return socket != null && !socket.isClosed();
    }

    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Mezua ezin da hutsik egon.");
        }
        out.println(message);
        mensajes.add(message);
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public void closeConnection() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
            mensajes.clear();
        }
    }

    public List<String> getMensajes() {
        return new ArrayList<>(mensajes);
    }

    public DataInputStream getDataIn() {
        return dataIn;
    }

    public DataOutputStream getDataOut() {
        return dataOut;
    }

    public Socket getSocket() {
        return socket;
    }
}