import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
/**
 * La classe ApplicationHandlerServer est responsable du traitement de la couche d'application des paquets DatagramPacket.
 * Elle étend la classe BaseHandler.
 */
public class ApplicationHandlerServer extends BaseHandler {

    private String filename;

    /**
     * Gère le DatagramPacket en effectuant le traitement de la couche d'application.
     *
     * @param packet Le DatagramPacket à traiter.
     * @throws IOException En cas d'erreur d'E/S lors du traitement du paquet.
     */
    public void handlePacket(DatagramPacket packet) throws IOException {
        // Effectue le traitement de la couche d'application ici
        byte[] packetData = packet.getData();
        packetData = trimByteArray(packetData);
        byte[] fileDataByte = new byte[packetData.length - FileTransferClient.HEADER_SIZE];

        System.arraycopy(packetData, FileTransferClient.HEADER_SIZE, fileDataByte, 0, packetData.length - FileTransferClient.HEADER_SIZE);

        if (ByteBuffer.wrap(packetData, 0, FileTransferClient.PACKET_NUMBER_SIZE).getInt() == 1) {
            filename = new String(fileDataByte, StandardCharsets.UTF_8);
        } else {
            // Écrire les données du fichier
            try (FileOutputStream fileOutputStream = new FileOutputStream(filename, true)) {
                fileOutputStream.write(fileDataByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Passe le paquet au gestionnaire suivant dans la chaîne
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }

    /**
     * Supprime les octets nuls de fin du tableau d'octets donné.
     *
     * @param data Le tableau d'octets à découper.
     * @return Le tableau d'octets découpé.
     */
    public static byte[] trimByteArray(byte[] data) {
        int nullByteIndex = -1;
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != 0) {
                nullByteIndex = i;
                break;
            }
        }
        byte[] trimmedData = new byte[nullByteIndex + 1];
        System.arraycopy(data, 0, trimmedData, 0, nullByteIndex + 1);
        return trimmedData;
    }
}
