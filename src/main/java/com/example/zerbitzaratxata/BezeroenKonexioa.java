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

            // Loop principal para recibir y enviar mensajes
            while (bezero.konektatutaDago()) {
                String mezua = bezero.readMessage();
                if (mezua == null || mezua.trim().isEmpty()) {
                    continue; // Skip empty messages
                }
                // Enviar el mensaje a todos los clientes menos al que lo envió
                zerbitzaria.bidaliMezuaDenei(mezua, bezero);  // Aquí pasamos el cliente que envió el mensaje
            }
        } catch (IOException e) {
            System.err.println("Bezeroa deskonektatu da");
        } finally {
            try {
                bezero.closeConnection();
            } catch (IOException e) {
                System.err.println("Errorea konexioa istean: " + e.getMessage());
            }
        }
    }
}

