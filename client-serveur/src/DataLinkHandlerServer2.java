import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * La classe DataLinkHandlerServer2 étend la classe BaseHandler et représente un gestionnaire de paquets au niveau de la liaison de données.
 */
public class DataLinkHandlerServer2 extends BaseHandler {

    /**
     * Constructeur de la classe DataLinkHandlerServer2.
     *
     * @param socket Le socket DatagramSocket à utiliser.
     */
    public DataLinkHandlerServer2(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Gère le DatagramPacket en envoyant un nouveau paquet avec des informations spécifiées au même destinataire.
     *
     * @param packet Le DatagramPacket à traiter.
     * @throws IOException En cas d'erreur d'E/S lors du traitement du paquet.
     */
    public void handlePacket(DatagramPacket packet) throws IOException {
        byte[] buf = new byte[200];

        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        int packetNumber = ByteBuffer.wrap(packet.getData(), 0, FileTransferClient.PACKET_NUMBER_SIZE).getInt();
        FileTransferClient.createPacketHeader(packetNumber, 0, FileTransferClient.PACKET_SENT, null);
        socket.send(packet);
    }
}

