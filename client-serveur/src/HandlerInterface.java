import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public interface HandlerInterface {
    void handlePacket(DatagramPacket packet) throws IOException;
    void setNextHandler(HandlerInterface nextHandler);
    public void setSocket(DatagramSocket s );

}
