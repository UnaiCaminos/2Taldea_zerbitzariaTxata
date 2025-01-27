package com.example.zerbitzaratxata;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Zerbitzaria {

    private ServerSocket socket;
    private final int puerto;
    private final List<Bezeroa> bezeroak;

    public Zerbitzaria(int puerto) {
        if (puerto <= 0 || puerto > 65535) {
            throw new IllegalArgumentException("Puerto debe estar entre 1 y 65535.");
        }
        this.puerto = puerto;
        this.bezeroak = new CopyOnWriteArrayList<>();
    }

    public void hasi() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en el puerto: " + puerto);
        } else {
            throw new IllegalStateException("El servidor ya está iniciado.");
        }
    }

    public void itxi() throws IOException {
        for (Bezeroa bezero : bezeroak) {
            try {
                bezero.closeConnection();
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión del cliente: " + e.getMessage());
            }
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean konektatuta() {
        return socket != null && !socket.isClosed();
    }

    public Socket onartuKonexioa() throws IOException {
        if (socket != null) {
            return socket.accept();
        } else {
            throw new IllegalStateException("El servidor no está iniciado.");
        }
    }

    public void gehituBezeroa(Bezeroa bezero) {
        if (bezero == null) {
            throw new IllegalArgumentException("El cliente no puede ser null.");
        }
        bezeroak.add(bezero);
    }

    public void bidaliMezuaDenei(String mezua) {
        if (mezua == null || mezua.trim().isEmpty()) {
            System.err.println("El mensaje no puede estar vacío ni ser null.");
            return;
        }
        for (Bezeroa bezero : bezeroak) {
            try {
                bezero.sendMessage(mezua);
            } catch (Exception e) {
                System.err.println("Error al enviar mensaje a un cliente: " + e.getMessage());
            }
        }
    }
}
