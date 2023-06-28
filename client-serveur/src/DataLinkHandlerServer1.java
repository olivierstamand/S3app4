import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;
/**
 * La classe DataLinkHandlerServer1 étend la classe BaseHandler et représente un gestionnaire de paquets au niveau de la liaison de données.
 */
public class DataLinkHandlerServer1 extends BaseHandler {

    /**
     * Constructeur de la classe DataLinkHandlerServer1.
     *
     * @param socket Le socket DatagramSocket à utiliser.
     */
    public DataLinkHandlerServer1(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Gère le DatagramPacket en recevant le paquet à l'aide du socket et en le passant au gestionnaire suivant dans la chaîne.
     *
     * @param packet Le DatagramPacket à traiter.
     * @throws IOException En cas d'erreur d'E/S lors du traitement du paquet.
     */
    public void handlePacket(DatagramPacket packet) throws IOException {
        socket.receive(packet);

        // Passe le paquet au gestionnaire suivant dans la chaîne
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }
}
