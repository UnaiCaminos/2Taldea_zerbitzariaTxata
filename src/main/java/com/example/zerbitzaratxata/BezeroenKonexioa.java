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
            bezero.sendMessage("Kaixo ongi etorri gure mezularitza zerbitzura!");

            while (bezero.konektatutaDago()) {
                String mezua = bezero.readMessage();
                if (mezua == null || mezua.trim().isEmpty()) {
                    continue; // Skip empty messages
                }
                zerbitzaria.bidaliMezuaDenei(mezua);
            }
        } catch (IOException e) {
            System.err.println("Errore bat gertatu da bezeroaren komunikazioan: " + e.getMessage());
        } finally {
            try {
                bezero.closeConnection();
            } catch (IOException e) {
                System.err.println("Errorea konexioa istean: " + e.getMessage());
            }
        }
    }
}
