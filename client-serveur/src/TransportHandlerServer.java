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

    private static PrintWriter logWriter=null;

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
            long crcCalculated = FileTransferClient.getCRCValue(fileData);
            //long crcCalculated=2;
            int packetNumber= ByteBuffer.wrap(packetData,0,FileTransferClient.PACKET_NUMBER_SIZE).getInt();

           if (crcCalculated != crcExpected) {
                byte[] Returnpacket= FileTransferClient.createPacketHeader(packetNumber,crcCalculated,FileTransferClient.ERROR_CRC,null);
                FileTransferClient.sendPacket(socket,Returnpacket,packet.getAddress(),packet.getPort());
                addMessageLog(FileTransferClient.ERROR_CRC,packetNumber);

                return;


            }
           int startIndex = FileTransferClient.HEADER_SIZE - FileTransferClient.MESSAGE_SIZE;
           byte[] stringBytes = new byte[FileTransferClient.MESSAGE_SIZE];
           System.arraycopy(packetData, startIndex, stringBytes, 0, FileTransferClient.MESSAGE_SIZE);
           stringBytes = ApplicationHandlerServer.trimByteArray(stringBytes);
           String message = new String(stringBytes);
           if(message.equals(FileTransferClient.PACKET_LOSS))
           {
               addMessageLog(FileTransferClient.PACKET_LOSS,packetNumber);
               return;
           }

            addMessageLog(FileTransferClient.PACKET_SENT,packetNumber);

            nextHandler.handlePacket(packet);
        }


    }
    private static void addMessageLog(String message,int packetNumber)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        logWriter.append(timestamp +" Packet number: "+ packetNumber +" "+ message+'\n');
        logWriter.flush();
    }
}
