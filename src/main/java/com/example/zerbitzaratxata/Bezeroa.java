package com.example.zerbitzaratxata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Bezeroa {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<String> mensajes;  // Lista para almacenar los mensajes

    public Bezeroa(Socket socket) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IllegalArgumentException("Socketarekin arazoak daude");
        }
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.mensajes = new ArrayList<>();  // Inicializar lista de mensajes
    }

    public boolean konektatutaDago() {
        return socket != null && !socket.isClosed();
    }

    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Mensajea ezin da hutsik egon.");
        }
        out.println(message);  // Enviar el mensaje al cliente
        mensajes.add(message);  // Guardarlo en la lista de mensajes
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public void closeConnection() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
            mensajes.clear();  // Eliminar todos los mensajes cuando la conexión se cierre
        }
    }

    // Método para obtener los mensajes almacenados
    public List<String> getMensajes() {
        return new ArrayList<>(mensajes);
    }
}
