import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * L'interface HandlerInterface définit les méthodes requises pour gérer les paquets Datagram.
 */
public interface HandlerInterface {
    /**
     * Traite un paquet Datagram spécifié.
     *
     * @param packet le paquet Datagram à traiter.
     * @throws IOException en cas d'erreur lors du traitement du paquet.
     */
    void handlePacket(DatagramPacket packet) throws IOException;

    /**
     * Définit le prochain gestionnaire (Handler) de paquet à utiliser.
     *
     * @param nextHandler le prochain gestionnaire de paquet.
     */
    void setNextHandler(HandlerInterface nextHandler);

    /**
     * Définit le socket Datagram à utiliser pour les opérations de communication.
     *
     * @param s le socket Datagram à utiliser.
     */
    void setSocket(DatagramSocket s);
}
