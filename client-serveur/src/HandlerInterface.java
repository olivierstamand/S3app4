import java.io.IOException;
import java.net.DatagramPacket;

public interface HandlerInterface {
    void handlePacket(DatagramPacket packet) throws IOException;
    void setNextHandler(HandlerInterface nextHandler);

}
