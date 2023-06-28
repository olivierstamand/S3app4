import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.CRC32;

import org.json.JSONObject;

/**
 * La classe FileTransferClient permet de transférer des fichiers via le protocole UDP.
 */
public class FileTransferClient {

    public static final int PACKET_NUMBER_SIZE = 4; // 4 bytes
    public static final int CRC_SIZE = 4; // 4 bytes
    public static final int MESSAGE_SIZE = 12; // 8 bytes
    private static DatagramSocket socket = null;

    public static final String ERROR_CRC = "Error CRC";
    public static final String PACKET_LOSS = "Packet Lost";
    public static final String PACKET_SENT = "Packet Sent";

    public static final int SERVER_PORT = 35000;
    public static final int HEADER_SIZE = CRC_SIZE + PACKET_NUMBER_SIZE + MESSAGE_SIZE;

    public static final int MAX_PACKET_SIZE_DATA = 200 - HEADER_SIZE;
    private static int errorCount = 0;

    public static void main(String[] args) throws IOException {
        String hostname = args[0];
        String filePath = args[1];

        // Lire le contenu du fichier dans un tableau de bytes
        File file = new File(filePath);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileContent);
        }
        int totalPackets = (int) Math.ceil((double) fileContent.length / MAX_PACKET_SIZE_DATA);

        // Obtenir l'adresse du serveur
        InetAddress serverAddress = InetAddress.getByName(hostname);
        socket = new DatagramSocket();

        // Créer le paquet contenant le nom de fichier
        byte[] packetFileName = createPacketHeader(1, getCRCValue(file.getName().getBytes()), "", file.getName().getBytes());
        sendPacket(socket, packetFileName, serverAddress, SERVER_PORT);

        // Recevoir le paquet de confirmation
        byte[] buffer = new byte[HEADER_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivedPacket);
        while (!checkPacketLoss(receivedPacket)) {
            buffer = new byte[HEADER_SIZE];
            sendPacket(socket, packetFileName, serverAddress, SERVER_PORT);
            receivedPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivedPacket);
            errorCount++;
            if (errorCount > 2) {
                byte[] errorPacket = createPacketHeader(1, 0, PACKET_LOSS, null);
                sendPacket(socket, errorPacket, serverAddress, SERVER_PORT);
                break;
            }
        }

        // Envoyer les paquets de données
        for (int packetNumber = 0; packetNumber < totalPackets; packetNumber++) {
            errorCount = 0;

            int offset = packetNumber * MAX_PACKET_SIZE_DATA;
            int length = Math.min(MAX_PACKET_SIZE_DATA, fileContent.length - offset);
            byte[] packetData = new byte[length];
            System.arraycopy(fileContent, offset, packetData, 0, length);

            // Créer le paquet avec le numéro de séquence et les données
            byte[] fullPacket = createPacketHeader(packetNumber + 2, getCRCValue(packetData), "", packetData);
            if (args[2].equals("d") && packetNumber == 0) {
                Random rand = new Random();
                fullPacket[fullPacket.length - 1] = (byte) rand.nextInt();
            }

            sendPacket(socket, fullPacket, serverAddress, SERVER_PORT);
            buffer = new byte[HEADER_SIZE];
            receivedPacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivedPacket);

            while (!checkPacketLoss(receivedPacket)) {
                buffer = new byte[HEADER_SIZE];
                sendPacket(socket, fullPacket, serverAddress, SERVER_PORT);
                receivedPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivedPacket);
                errorCount++;

                if (errorCount >= 2) {
                    byte[] errorPacket = createPacketHeader(packetNumber + 2, 0, PACKET_LOSS, null);
                    sendPacket(socket, errorPacket, serverAddress, SERVER_PORT);
                    break;
                }
            }
        }

        System.out.println("File sent successfully.");
        socket.close();
    }

    /**
     * Crée l'en-tête du paquet avec le numéro de séquence, le CRC, le message et les données.
     *
     * @param packetNumber Le numéro de séquence du paquet.
     * @param crc          La valeur CRC.
     * @param message      Le message.
     * @param data         Les données.
     * @return Le paquet complet avec l'en-tête et les données.
     */
    public static byte[] createPacketHeader(int packetNumber, long crc, String message, byte[] data) {
        byte[] header = new byte[HEADER_SIZE];

        // Définir le numéro de séquence (4 bytes)
        ByteBuffer.wrap(header, 0, PACKET_NUMBER_SIZE).putInt(packetNumber);

        // Définir le CRC (4 bytes)
        ByteBuffer.wrap(header, PACKET_NUMBER_SIZE, CRC_SIZE).putInt((int) crc);

        if (message != null) {
            byte[] fileNameBytes = message.getBytes(StandardCharsets.UTF_8);
            int copyLength = Math.min(fileNameBytes.length, MESSAGE_SIZE);
            System.arraycopy(fileNameBytes, 0, header, HEADER_SIZE - MESSAGE_SIZE, copyLength);
        }

        // Ajouter les données (si présentes) à l'en-tête
        if (data != null) {
            byte[] fullPacket = new byte[header.length + data.length];
            System.arraycopy(header, 0, fullPacket, 0, header.length);
            System.arraycopy(data, 0, fullPacket, header.length, data.length);
            return fullPacket;
        } else {
            return header;
        }
    }

    /**
     * Calcule la valeur CRC d'un tableau de bytes.
     *
     * @param data Les données.
     * @return La valeur CRC.
     */
    public static long getCRCValue(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    /**
     * Envoie un paquet via un socket DatagramSocket.
     *
     * @param socket  Le socket DatagramSocket.
     * @param packet  Le paquet à envoyer.
     * @param address L'adresse du destinataire.
     * @param port    Le port du destinataire.
     * @throws IOException En cas d'erreur d'E/S lors de l'envoi du paquet.
     */
    public static void sendPacket(DatagramSocket socket, byte[] packet, InetAddress address, int port) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, address, port);
        socket.send(datagramPacket);
    }

    /**
     * Vérifie si le paquet a été perdu en se basant sur le message reçu.
     *
     * @param p Le DatagramPacket reçu.
     * @return True si le paquet a été perdu, False sinon.
     * @throws IOException En cas d'erreur d'E/S lors de la lecture du message.
     */
    public static boolean checkPacketLoss(DatagramPacket p) throws IOException {
        byte[] data = p.getData();
        int startIndex = PACKET_NUMBER_SIZE + CRC_SIZE;
        byte[] stringBytes = new byte[FileTransferClient.MESSAGE_SIZE];
        System.arraycopy(data, startIndex, stringBytes, 0, FileTransferClient.MESSAGE_SIZE);
        stringBytes = ApplicationHandlerServer.trimByteArray(stringBytes);
        String message = new String(stringBytes);

        if (message.equals(FileTransferClient.ERROR_CRC)) {
            return false;
        }

        return true;
    }
}
