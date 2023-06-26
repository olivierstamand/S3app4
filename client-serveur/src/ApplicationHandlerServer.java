import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApplicationHandlerServer extends BaseHandler {
    String filename=null;
    public void handlePacket(DatagramPacket packet) throws IOException {
        // Perform Application Layer processing here
        byte[] packetData = packet.getData();
        int fileDataLength = packetData.length - (FileTransferClient.PACKET_NUMBER_SIZE + FileTransferClient.CRC_SIZE);
        byte [] fileDataByte = new byte[fileDataLength];
        System.arraycopy(packetData, FileTransferClient.PACKET_NUMBER_SIZE + FileTransferClient.CRC_SIZE, fileDataByte, 0, fileDataLength);

        if(ByteBuffer.wrap(packetData, 0, FileTransferClient.PACKET_NUMBER_SIZE).getInt()==1)
        {

            filename= fileDataByte.toString();
        }

        // Write the file data
        try (FileOutputStream fileOutputStream = new FileOutputStream(filename, true)) {
            fileOutputStream.write(fileDataByte);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Pass the packet to the next handler in the chain
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }
}
