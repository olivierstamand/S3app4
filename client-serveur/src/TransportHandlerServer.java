
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Cette classe représente un gestionnaire de transport côté serveur.
 * Elle gère les paquets dans le cadre d'un transfert de fichier via un protocole de transport non spécifié.
 */
public class TransportHandlerServer extends BaseHandler {

    private static PrintWriter logWriter=null;

    /**
     * Constructeur de la classe TransportHandlerServer.
     * Initialise un objet PrintWriter pour écrire les logs dans un fichier spécifié.
     *
     * @throws IOException en cas d'erreur lors de l'initialisation du PrintWriter.
     */
    public TransportHandlerServer() throws IOException {
        logWriter = new PrintWriter(new FileWriter("liaisonDeDonnes.log", true));
    }

    /**
     * Gère un paquet Datagram reçu.
     *
     * @param packet le paquet Datagram à traiter.
     * @throws IOException en cas d'erreur lors du traitement du paquet.
     */
    public void handlePacket(DatagramPacket packet) throws IOException {
        byte[] fileData = null;
        byte[] packetData = packet.getData();
        packetData= ApplicationHandlerServer.trimByteArray(packetData);
        int packetNumber = ByteBuffer.wrap(packetData, 0, FileTransferClient.PACKET_NUMBER_SIZE).getInt();
        boolean hasData= packetData.length> FileTransferClient.HEADER_SIZE;

        if (hasData) {
            int fileDataLength = packetData.length - FileTransferClient.HEADER_SIZE;
            fileData = new byte[fileDataLength];
            System.arraycopy(packetData, FileTransferClient.HEADER_SIZE, fileData, 0, fileDataLength);
            long crcExpected = ByteBuffer.wrap(packetData, FileTransferClient.PACKET_NUMBER_SIZE, FileTransferClient.CRC_SIZE).getInt() & 0xFFFFFFFFL;
            long crcCalculated = FileTransferClient.getCRCValue(fileData);

            if (crcCalculated != crcExpected) {
                byte[] Returnpacket = FileTransferClient.createPacketHeader(packetNumber, crcCalculated, FileTransferClient.ERROR_CRC, null);
                FileTransferClient.sendPacket(socket, Returnpacket, packet.getAddress(), packet.getPort());
                addMessageLog(FileTransferClient.ERROR_CRC, packetNumber);
                return;
            }
        }
        else {
            int startIndex = FileTransferClient.PACKET_NUMBER_SIZE + FileTransferClient.CRC_SIZE;
            byte[] stringBytes = new byte[FileTransferClient.MESSAGE_SIZE];
            System.arraycopy(packetData, startIndex, stringBytes, 0, packetData.length-FileTransferClient.PACKET_NUMBER_SIZE-FileTransferClient.CRC_SIZE);
            stringBytes = ApplicationHandlerServer.trimByteArray(stringBytes);
            String message = new String(stringBytes);
            if (message.equals(FileTransferClient.PACKET_LOSS)) {
                addMessageLog(FileTransferClient.PACKET_LOSS, packetNumber);
                return;
            }
        }

        addMessageLog(FileTransferClient.PACKET_SENT,packetNumber);
        nextHandler.handlePacket(packet);
    }

    /**
     * Ajoute un message de log au fichier de log.
     *
     * @param message le message à ajouter au log.
     * @param packetNumber le numéro de paquet associé au message.
     */
    private static void addMessageLog(String message,int packetNumber) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        logWriter.append(timestamp +" Packet number: "+ packetNumber +" "+ message+'\n');
        logWriter.flush();
    }
}