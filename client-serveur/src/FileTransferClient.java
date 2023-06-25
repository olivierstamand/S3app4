

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

import org.json.JSONObject;

public class FileTransferClient {
    private static final int MAX_PACKET_SIZE = 200;
    private static final int SERVER_PORT = 25000;
    private static final int HEADER_SIZE = 10;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java FileTransferClient <hostname> <filePath>");
            return;
        }

        String hostname = args[0];
        String filePath = args[1];

        // Read file content into a byte array
        File file = new File(filePath);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileContent);
        }

        // Get the server address
        InetAddress serverAddress = InetAddress.getByName(hostname);

        // Create a datagram socket
        DatagramSocket socket = new DatagramSocket();


        // Calculate the total number of packets needed for file transmission
        int totalPackets = (int) Math.ceil((double) fileContent.length / MAX_PACKET_SIZE);

        // Send data packets
        // Send data packets
        for (int packetNumber = 0; packetNumber < totalPackets; packetNumber++) {
            int offset = packetNumber * MAX_PACKET_SIZE;
            int length = Math.min(MAX_PACKET_SIZE, fileContent.length - offset);
            byte[] packetData = new byte[length];
            System.arraycopy(fileContent, offset, packetData, 0, length);

            // Create the packet with sequence number and data
            byte[] fullPacket = createPacketHeader(packetNumber, totalPackets, file.getName(), packetData);



            // Create the datagram packet with the full packet data
            DatagramPacket dataPacket = new DatagramPacket(fullPacket, fullPacket.length, serverAddress, SERVER_PORT);

            // Send the data packet to the server
            socket.send(dataPacket);
        }
        System.out.println("File sent successfully.");

        socket.close();

    }





    private static byte[] createPacketHeader(int packetNumber, int totalPackets, String fileName,byte [] data) {
        // Create a JSON object for the header
        JSONObject headerJson = new JSONObject();
        headerJson.put("packetNumber", packetNumber);
        headerJson.put("totalPackets", totalPackets);
        headerJson.put("fileName", fileName);
        headerJson.put("fileData", Base64.getEncoder().encodeToString(data));

        // Convert the JSON object to a byte array
        String headerString = headerJson.toString();
        return headerString.getBytes(StandardCharsets.UTF_8);
    }


}
