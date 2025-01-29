import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Zerbitzaria {

    private ServerSocket socket;
    private final int puerto;
    private final List<Bezeroa> bezeroak;
    private final List<String> mensajes; // Lista para almacenar los mensajes
    private final String archivoMensajes = "mensajes.ser"; // Archivo para persistir mensajes

    public Zerbitzaria(int puerto) {
        if (puerto <= 0 || puerto > 65535) {
            throw new IllegalArgumentException("Puerto debe estar entre 1 y 65535.");
        }
        this.puerto = puerto;
        this.bezeroak = new CopyOnWriteArrayList<>();
        this.mensajes = cargarMensajes(); // Cargar mensajes guardados al iniciar el servidor
    }

    public void hasi() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en el puerto: " + puerto);
        } else {
            throw new IllegalStateException("El servidor ya está iniciado.");
        }

        // Iniciar un hilo para aceptar conexiones de clientes sin detener la ejecución
        new Thread(() -> {
            while (true) {
                try {
                    Socket bezeroSocket = socket.accept(); // Esperar un nuevo cliente
                    Bezeroa bezero = new Bezeroa(bezeroSocket);
                    gehituBezeroa(bezero);

                    // Crear una nueva conexión para manejar la comunicación con el cliente
                    BezeroenKonexioa bezeroKonexioa = new BezeroenKonexioa(bezero, this);
                    bezeroKonexioa.start();

                    System.out.println("Nuevo cliente conectado.");
                } catch (IOException e) {
                    System.err.println("Error aceptando conexiones: " + e.getMessage());
                }
            }
        }).start(); // Iniciar el hilo en segundo plano
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

        // Enviar todos los mensajes guardados al nuevo cliente
        synchronized (mensajes) {
            for (String mensaje : mensajes) {
                bezero.sendMessage(mensaje);
            }
        }
    }

    public void bidaliMezuaDenei(String mezua) {
        if (mezua == null || mezua.trim().isEmpty()) {
            System.err.println("El mensaje no puede estar vacío ni ser null.");
            return;
        }

        // Agregar el mensaje a la lista y guardarlo
        synchronized (mensajes) {
            mensajes.add(mezua);
            guardarMensajes(); // Persistir mensajes
        }


    }

    // Método para guardar los mensajes en un archivo
    private void guardarMensajes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivoMensajes))) {
            oos.writeObject(mensajes);
        } catch (IOException e) {
            System.err.println("Error al guardar los mensajes: " + e.getMessage());
        }
    }

    // Método para cargar los mensajes desde un archivo
    private List<String> cargarMensajes() {
        File archivo = new File(archivoMensajes);
        if (!archivo.exists()) {
            return new ArrayList<>(); // Si no existe, retornar una lista vacía
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivoMensajes))) {
            return (List<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar los mensajes: " + e.getMessage());
            return new ArrayList<>(); // En caso de error, retornar lista vacía
        }
    }
}
