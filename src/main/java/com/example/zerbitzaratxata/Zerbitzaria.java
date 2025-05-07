package com.example.zerbitzaratxata;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Zerbitzaria {

    private ServerSocket socket;
    private final int puerto;
    private final List<Bezeroa> bezeroak;
    private final List<String> mensajes;
    private final String archivoMensajes = "mensajes.ser";

    public Zerbitzaria(int puerto) {
        if (puerto <= 0 || puerto > 65535) {
            throw new IllegalArgumentException("Portua 1 eta 65535-en artean egon behar da.");
        }
        this.puerto = puerto;
        this.bezeroak = new CopyOnWriteArrayList<>();
        this.mensajes = cargarMensajes();
    }

    public void hasi() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new ServerSocket(puerto);
            System.out.println("Zerbitzaria iniziatu den portua: " + puerto);
            System.out.println("Gordetako mezuak: " + mensajes);
        } else {
            throw new IllegalStateException("Zerbitzaria martxan dago.");
        }

        new Thread(() -> {
            while (true) {
                try {
                    Socket bezeroSocket = socket.accept();
                    Bezeroa bezero = new Bezeroa(bezeroSocket);
                    gehituBezeroa(bezero);

                    BezeroenKonexioa bezeroKonexioa = new BezeroenKonexioa(bezero, this);
                    bezeroKonexioa.start();

                    System.out.println("Bezeroa konektatu da");
                } catch (IOException e) {
                    System.err.println("Arazoa konektatzean: " + e.getMessage());
                }
            }
        }).start();
    }

    public boolean konektatuta() {
        return socket != null && !socket.isClosed();
    }

    public Socket onartuKonexioa() throws IOException {
        if (socket != null) {
            return socket.accept();
        } else {
            throw new IllegalStateException("Zerbitzaria ez dago martxan.");
        }
    }

    public void gehituBezeroa(Bezeroa bezero) {
        if (bezero == null) {
            throw new IllegalArgumentException("Bezeroa ezin da null izan.");
        }
        bezeroak.add(bezero);

        // Enviar todos los mensajes guardados SOLO si hay mensajes previos
        synchronized (mensajes) {
            if (!mensajes.isEmpty()) {
                for (String mensaje : mensajes) {
                    bezero.sendMessage(mensaje);
                }
            }
        }
    }

public void bidaliMezuaDenei(String mezua, Bezeroa sender) {
    if (mezua == null || mezua.trim().isEmpty()) {
        System.err.println("Mezua ezin da hutsik egon edo null izan.");
        return;
    }

    // Evitar guardar mensajes duplicados en la lista
    synchronized (mensajes) {

            if (!mezua.startsWith("[FILE]") && mezua.length() < 500) {
                mensajes.add(mezua);
            }
            guardarMensajes();

    }

    // Enviar el mensaje a todos los clientes, EXCEPTO al remitente
    for (Bezeroa bezero : bezeroak) {
        if (!bezero.equals(sender)) {
            try {
                bezero.sendMessage(mezua);
            } catch (Exception e) {
                System.err.println("Arazoa bezeroei mezua bidaltzerakoan: " + e.getMessage());
            }
        }
    }
}


// Método para guardar los mensajes en un archivo
    private void guardarMensajes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivoMensajes))) {
            oos.writeObject(mensajes);
        } catch (IOException e) {
            System.err.println("Arazoa mezua gordetzerakoan: " + e.getMessage());
        }
    }

    // Método para cargar los mensajes desde un archivo
    private List<String> cargarMensajes() {
        File archivo = new File(archivoMensajes);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivoMensajes))) {
            return (List<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Arzaoa mezua kargatzean: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}


