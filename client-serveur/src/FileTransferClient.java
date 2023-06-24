import java.io.*;
import java.net.*;
import java.util.*;
/*
public class QuoteClient {
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Usage: java QuoteClient <hostname>");
            return;
        }

        // get a datagram socket
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] buf = new byte[256];
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 25000);
        socket.send(packet);

        // get response
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Quote of the Moment: " + received);

        socket.close();
    }
}*/

import java.io.*;
import java.net.*;
import java.util.Scanner;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileTransferClient {
    private static final int MAX_PACKET_SIZE = 200;
    private static final int SERVER_PORT = 25000;

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

        // Send the file name packet to the server
        String fileName = file.getName();
        byte[] fileNameBytes = fileName.getBytes();
        DatagramPacket fileNamePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, serverAddress, SERVER_PORT);
        socket.send(fileNamePacket);

        // Calculate the total number of packets needed for file transmission
        int totalPackets = (int) Math.ceil((double) fileContent.length / MAX_PACKET_SIZE);

        // Send data packets
        for (int packetNumber = 0; packetNumber < totalPackets; packetNumber++) {
            int offset = packetNumber * MAX_PACKET_SIZE;
            int length = Math.min(MAX_PACKET_SIZE, fileContent.length - offset);
            byte[] packetData = new byte[length];
            System.arraycopy(fileContent, offset, packetData, 0, length);

            // Create the packet with sequence number and data
            byte[] packetHeader = createPacketHeader(packetNumber, totalPackets);
            byte[] fullPacket = new byte[packetHeader.length + packetData.length];
            System.arraycopy(packetHeader, 0, fullPacket, 0, packetHeader.length);
            System.arraycopy(packetData, 0, fullPacket, packetHeader.length, packetData.length);

            // Create the datagram packet with the full packet data
            DatagramPacket dataPacket = new DatagramPacket(fullPacket, fullPacket.length, serverAddress, SERVER_PORT);

            // Send the data packet to the server
            socket.send(dataPacket);
        }

        System.out.println("File sent successfully.");

        socket.close();
    }

    private static byte[] createPacketHeader(int packetNumber, int totalPackets) {
        String header = "Packet: " + packetNumber + "/" + totalPackets;
        return header.getBytes();
    }
}
