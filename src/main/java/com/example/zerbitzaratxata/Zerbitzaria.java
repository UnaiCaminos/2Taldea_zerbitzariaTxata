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
    private final String carpetaArchivos = "archivos"; // Carpeta donde guardaremos los archivos

    public Zerbitzaria(int puerto) {
        if (puerto <= 0 || puerto > 65535) {
            throw new IllegalArgumentException("Portua 1 eta 65535-en artean egon behar da.");
        }
        this.puerto = puerto;
        this.bezeroak = new CopyOnWriteArrayList<>();
        this.mensajes = cargarMensajes();
        crearCarpetaArchivos();
    }

    public void hasi() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new ServerSocket(puerto);
            System.out.println("Zerbitzaria iniziatu den portua: " + puerto);
            System.out.println("Mensajes guardados: " + mensajes);
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
            if (!mensajes.contains(mezua)) {  // Verifica si el mensaje ya existe
                if (!mezua.startsWith("[FILE]") && mezua.length() < 500) {
                    mensajes.add(mezua);
                }
                guardarMensajes(); // Solo guarda si el mensaje es nuevo
            }
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

    // Método para crear la carpeta donde se guardarán los archivos si no existe
    private void crearCarpetaArchivos() {
        File carpeta = new File(carpetaArchivos);
        if (!carpeta.exists()) {
            carpeta.mkdirs();  // Crea la carpeta si no existe
        }
    }

    // Método para guardar un archivo en el servidor (solo en la carpeta de archivos)
    public String guardarArchivo(byte[] datosArchivo, String nombreArchivo) throws IOException {
        // Asegúrate de que la carpeta de archivos existe
        File carpeta = new File(carpetaArchivos);
        if (!carpeta.exists()) {
            carpeta.mkdirs();  // Crear la carpeta si no existe
        }

        // Crear la ruta completa para el archivo dentro de la carpeta
        File archivo = new File(carpetaArchivos + File.separator + nombreArchivo);

        // Guardar el archivo en la ubicación correcta
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            fos.write(datosArchivo);
        }

        // Retornar la ruta absoluta del archivo
        return archivo.getAbsolutePath();
    }


    // Método para enviar un archivo a un cliente
    public void enviarArchivoACliente(String rutaArchivo, Bezeroa cliente) {
        try {
            File archivo = new File(rutaArchivo);
            byte[] archivoBytes = new byte[(int) archivo.length()];
            try (FileInputStream fis = new FileInputStream(archivo)) {
                fis.read(archivoBytes);
            }

            cliente.sendFile(archivoBytes, archivo.getName());
        } catch (IOException e) {
            System.err.println("Errorea fitxategia bidaltzerakoan: " + e.getMessage());
        }
    }

    // Método para manejar la recepción de archivos de un cliente
    public void recibirArchivo(byte[] datosArchivo, String nombreArchivo, Bezeroa sender) {
        try {
            // Usar el método guardarArchivo para guardar el archivo en la carpeta correcta
            String archivoRuta = guardarArchivo(datosArchivo, nombreArchivo);
            System.out.println("Archivo recibido y guardado en: " + archivoRuta);

            // Notificar a todos los clientes que se ha recibido un archivo
            bidaliMezuaDenei("Un nuevo archivo ha sido recibido: " + nombreArchivo, sender);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo recibido: " + e.getMessage());
        }
    }
}



