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

            while (bezero.konektatutaDago()) {
                String mezua = bezero.readMessage();
                System.out.println(mezua);
                if (mezua == null || mezua.trim().isEmpty()) {
                    continue; // Skip empty messages
                }
                zerbitzaria.bidaliMezuaDenei(mezua);
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
