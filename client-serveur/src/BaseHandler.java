import java.io.IOException;
import java.net.DatagramPacket;
public class BaseHandler implements HandlerInterface{
    protected HandlerInterface nextHandler;

    public void handlePacket(DatagramPacket packet) throws IOException {
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }
    public void setNextHandler(HandlerInterface nextHandler) {
        this.nextHandler = nextHandler;
    }
}