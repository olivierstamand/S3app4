import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class DataLinkHandlerServer2 extends BaseHandler{
    private HandlerInterface nextHandler;
    private DatagramSocket socket;

    public DataLinkHandlerServer2(DatagramSocket socket) {
        this.socket = socket;
    }
    public void handlePacket(DatagramPacket packet) throws IOException{
        byte[] buf = new byte[200];

        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        int packetNumber= ByteBuffer.wrap(packet.getData(),0,FileTransferClient.PACKET_NUMBER_SIZE).getInt();
        FileTransferClient.createPacketHeader(packetNumber,0,FileTransferClient.PACKET_SENT,null);
        socket.send(packet);
    }
}
