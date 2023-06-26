import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BaseHandler implements HandlerInterface{
    protected HandlerInterface nextHandler;
    protected DatagramSocket socket= null;

    public void handlePacket(DatagramPacket packet) throws IOException {
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }
    public void setNextHandler(HandlerInterface nextHandler) {
        this.nextHandler = nextHandler;
    }
    public void setSocket(DatagramSocket s ){this.socket=s;}
}