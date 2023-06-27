import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.zip.CRC32;

public class TransportHandlerServer extends BaseHandler {

    private PrintWriter logWriter=null;

    public TransportHandlerServer() throws IOException {

            logWriter = new PrintWriter(new FileWriter("liaisonDeDonnes.log", true));

    }

    public void handlePacket(DatagramPacket packet) throws IOException{

        byte[] fileData = null;
        byte[] packetData = packet.getData();
        packetData= ApplicationHandlerServer.trimByteArray(packetData);
       if (packetData.length> FileTransferClient.HEADER_SIZE) {
            int fileDataLength = packetData.length-FileTransferClient.HEADER_SIZE;
            fileData = new byte[fileDataLength];
            System.arraycopy(packetData, FileTransferClient.HEADER_SIZE, fileData, 0, fileDataLength);
            long crcExpected = ByteBuffer.wrap(packetData, FileTransferClient.PACKET_NUMBER_SIZE, FileTransferClient.CRC_SIZE).getInt() & 0xFFFFFFFFL;
            //long crcCalculated = FileTransferClient.getCRCValue(fileData);
            long crcCalculated=2;
            if (crcCalculated != crcExpected) {
                int packetNumber= ByteBuffer.wrap(packetData,0,FileTransferClient.PACKET_NUMBER_SIZE).getInt();
                byte[] Returnpacket= FileTransferClient.createPacketHeader(packetNumber,crcCalculated,FileTransferClient.ERROR_CRC,null);
                FileTransferClient.sendPacket(socket,Returnpacket,packet.getAddress(),packet.getPort());

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date());
                logWriter.append(timestamp + " -  " + "Erreur CRC"+'\n');
                logWriter.flush();
                return;


            }



            nextHandler.handlePacket(packet);
        }


    }
}
