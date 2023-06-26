import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;

public class DataLinkHandlerServer1 extends BaseHandler {
    private HandlerInterface nextHandler;
    private DatagramSocket socket;

    public DataLinkHandlerServer1(DatagramSocket socket) {
        this.socket = socket;
    }
    public void handlePacket(DatagramPacket packet) throws IOException {
        socket.receive(packet);

        // Pass the packet to the next handler in the chain
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }
}

