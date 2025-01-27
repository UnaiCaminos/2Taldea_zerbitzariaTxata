import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        Zerbitzaria zerbitzaria=new Zerbitzaria(5555);
        try{
            zerbitzaria.hasi();
            while(zerbitzaria.konektatuta()){
                Socket bezeroarenSocketa =zerbitzaria.onartuKonexioa();
                Bezeroa bezero=new Bezeroa(bezeroarenSocketa);
                zerbitzaria.gehituBezeroa(bezero);
                BezeroenKonexioa konexioa=new BezeroenKonexioa(bezero,zerbitzaria);
                konexioa.start();
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
