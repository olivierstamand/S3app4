import java.io.IOException;
import java.net.DatagramSocket;


public class QuoteServer {
    public static void main(String[] args) throws IOException {
        new QuoteServerThread().start();
    }
}



