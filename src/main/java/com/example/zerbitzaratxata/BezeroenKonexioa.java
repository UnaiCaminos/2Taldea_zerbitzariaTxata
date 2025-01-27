package com.example.zerbitzaratxata;

import java.io.IOException;

public class BezeroenKonexioa extends Thread {

    private final Bezeroa bezero;
    private final Zerbitzaria zerbitzaria;

    public BezeroenKonexioa(Bezeroa bezero, Zerbitzaria zerbitzaria) {
        if (bezero == null || zerbitzaria == null) {
            throw new IllegalArgumentException("Bezeroa eta Zerbitzaria ezin dira null izan.");
        }
        this.bezero = bezero;
        this.zerbitzaria = zerbitzaria;
    }

    @Override
    public void run() {
        try {
            // Enviar un mensaje de bienvenida
            bezero.sendMessage("Kaixo ongi etorri gure mezularitza zerbitzura!");

            // Enviar los mensajes anteriores almacenados al cliente si existen
            for (String mensaje : bezero.getMensajes()) {
                bezero.sendMessage(mensaje);  // Envía cada mensaje almacenado previamente
            }

            // Loop principal para recibir y enviar mensajes
            while (bezero.konektatutaDago()) {
                String mezua = bezero.readMessage();
                if (mezua == null || mezua.trim().isEmpty()) {
                    continue; // Skip empty messages
                }
                zerbitzaria.bidaliMezuaDenei(mezua);  // Enviar el mensaje a todos los clientes
            }

        } catch (IOException e) {
            System.err.println("Errore bat gertatu da bezeroaren komunikazioan: " + e.getMessage());
        } finally {
            try {
                bezero.closeConnection();  // Cerrar la conexión cuando el cliente se desconecta
            } catch (IOException e) {
                System.err.println("Errorea konexioa istean: " + e.getMessage());
            }
        }
    }
}
