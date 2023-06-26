

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import java.util.zip.CRC32;

import org.json.JSONObject;

public class FileTransferClient {


    public static final int PACKET_NUMBER_SIZE = 4; // 4 bytes
    public static final int CRC_SIZE = 4; // 4 bytes
    public static final int MESSAGE_SIZE = 12; // 8 bytes
    public static final int MAX_PACKET_SIZE_DATA =  200-PACKET_NUMBER_SIZE-CRC_SIZE-MESSAGE_SIZE;

    private static final int SERVER_PORT = 35000;
    private static final int HEADER_SIZE = CRC_SIZE+PACKET_NUMBER_SIZE+MESSAGE_SIZE;

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

        byte [] packetFileName= createPacketHeader(1,getCRCValue(file.getName().getBytes()), null,file.getName().getBytes());
        sendPacket(socket,packetFileName,serverAddress,SERVER_PORT);




        // Calculate the total number of packets needed for file transmission
        int totalPackets = (int) Math.ceil((double) fileContent.length / MAX_PACKET_SIZE_DATA);

        // Send data packets
        // Send data packets
        for (int packetNumber = 0; packetNumber < totalPackets; packetNumber++) {

            int offset = packetNumber * MAX_PACKET_SIZE_DATA;
            int length = Math.min(MAX_PACKET_SIZE_DATA, fileContent.length - offset);
            byte[] packetData = new byte[length];
            System.arraycopy(fileContent, offset, packetData, 0, length);

            // Create the packet with sequence number and data
            byte[] fullPacket = createPacketHeader(packetNumber+2,getCRCValue(packetData), null, packetData);

            sendPacket(socket,fullPacket,serverAddress,SERVER_PORT);



        }
        System.out.println("File sent successfully.");

        socket.close();

    }





    private static byte[] createPacketHeader(int packetNumber, long crc, String message, byte[] data) {
        byte[] header = new byte[HEADER_SIZE];

        // Set packet number (4 bytes)
        ByteBuffer.wrap(header, 0, PACKET_NUMBER_SIZE).putInt(packetNumber);

        // Set CRC (4 bytes)
        ByteBuffer.wrap(header, PACKET_NUMBER_SIZE, CRC_SIZE).putInt((int) crc);


        if(message!=null) {
            byte[] fileNameBytes = message.getBytes(StandardCharsets.UTF_8);
            int copyLength = Math.min(fileNameBytes.length, MESSAGE_SIZE);
            System.arraycopy(fileNameBytes, 0, header, PACKET_NUMBER_SIZE + CRC_SIZE + MESSAGE_SIZE, copyLength);
        }

        // Append the data (if present) to the header
        if (data != null) {
            byte[] fullPacket = new byte[header.length + data.length];
            System.arraycopy(header, 0, fullPacket, 0, header.length);
            System.arraycopy(data, 0, fullPacket, header.length, data.length);
            return fullPacket;
        } else {
            return header;
        }
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



