

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.zip.CRC32;

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

        byte [] packetFileName= createPacketHeader(1,getCRCValue(file.getName().getBytes()), file.getName(),null);
        sendPacket(socket,packetFileName,serverAddress,SERVER_PORT);




        // Calculate the total number of packets needed for file transmission
        int totalPackets = (int) Math.ceil((double) fileContent.length / MAX_PACKET_SIZE);

        // Send data packets
        // Send data packets
        for (int packetNumber = 1; packetNumber < totalPackets; packetNumber++) {

            int offset = packetNumber * MAX_PACKET_SIZE;
            int length = Math.min(MAX_PACKET_SIZE, fileContent.length - offset);
            byte[] packetData = new byte[length];
            System.arraycopy(fileContent, offset, packetData, 0, length);


            // Create the packet with sequence number and data
            byte[] fullPacket = createPacketHeader(packetNumber+1,getCRCValue(packetData),file.getName(), packetData);

            sendPacket(socket,fullPacket,serverAddress,SERVER_PORT);



        }
        System.out.println("File sent successfully.");

        socket.close();

    }





    private static byte[] createPacketHeader(int packet_number,long CRC,  String fileName,byte [] data) {
        // Create a JSON object for the header
        JSONObject headerJson = new JSONObject();
        headerJson.put("packet_number",packet_number);
        headerJson.put("CRC", CRC);
        headerJson.put("fileName", fileName);
        if(data!=null)
            headerJson.put("fileData", Base64.getEncoder().encodeToString(data));

        // Convert the JSON object to a byte array
        String headerString = headerJson.toString();
        return headerString.getBytes(StandardCharsets.UTF_8);
    }
    public static long getCRCValue(byte[] data )
    {
        CRC32 crc= new CRC32();
        crc.update(data);
        return crc.getValue();

    }
    public static void sendPacket(DatagramSocket socket, byte[] packet, InetAddress address ,int port) throws IOException {
        DatagramPacket datagramPacket= new DatagramPacket(packet,packet.length,address,port);
        socket.send(datagramPacket);

    }



}



