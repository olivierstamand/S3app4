import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;

public class TransportHandlerServer extends BaseHandler {
    public void handlePacket(DatagramPacket packet) throws IOException{

        byte[] fileData = null;
        byte[] packetData = packet.getData();
       if (packet.getLength() > FileTransferClient.PACKET_NUMBER_SIZE + FileTransferClient.CRC_SIZE+ FileTransferClient.MESSAGE_SIZE) {
            int fileDataLength = packetData.length- (FileTransferClient.PACKET_NUMBER_SIZE + FileTransferClient.CRC_SIZE + FileTransferClient.MESSAGE_SIZE);
            fileData = new byte[fileDataLength];
            System.arraycopy(packetData, FileTransferClient.PACKET_NUMBER_SIZE + FileTransferClient.CRC_SIZE, fileData, 0, fileDataLength);
            long crcExpected = ByteBuffer.wrap(packet.getData(), FileTransferClient.PACKET_NUMBER_SIZE, FileTransferClient.CRC_SIZE).getInt() & 0xFFFFFFFFL;
            long crcCalculated = FileTransferClient.getCRCValue(fileData);
            if (crcCalculated != crcExpected) {
                // Handle logic for error detection in the Transport Layer
            }
            nextHandler.handlePacket(packet);
        }


    }
}
