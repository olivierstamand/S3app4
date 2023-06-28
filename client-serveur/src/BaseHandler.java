import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
/**
 * La classe BaseHandler implémente l'interface HandlerInterface et fournit une base pour les gestionnaires de paquets DatagramPacket.
 */
public class BaseHandler implements HandlerInterface {

    protected HandlerInterface nextHandler;
    protected DatagramSocket socket = null;

    /**
     * Gère le DatagramPacket en le passant au gestionnaire suivant dans la chaîne.
     *
     * @param packet Le DatagramPacket à traiter.
     * @throws IOException En cas d'erreur d'E/S lors du traitement du paquet.
     */
    public void handlePacket(DatagramPacket packet) throws IOException {
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }

    /**
     * Définit le gestionnaire suivant dans la chaîne.
     *
     * @param nextHandler Le gestionnaire suivant.
     */
    public void setNextHandler(HandlerInterface nextHandler) {
        this.nextHandler = nextHandler;
    }

    /**
     * Définit le socket DatagramSocket à utiliser par le gestionnaire.
     *
     * @param socket Le socket DatagramSocket.
     */
    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
}
