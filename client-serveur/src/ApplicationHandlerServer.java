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
        packetData = trimByteArray(packetData);
       byte [] fileDataByte= new byte[packetData.length-FileTransferClient.HEADER_SIZE];

        System.arraycopy(packetData, FileTransferClient.HEADER_SIZE, fileDataByte, 0, packetData.length- FileTransferClient.HEADER_SIZE);

        if(ByteBuffer.wrap(packetData, 0, FileTransferClient.PACKET_NUMBER_SIZE).getInt()==1)
        {

            filename= new String(fileDataByte,StandardCharsets.UTF_8);
            filename.replaceAll("\u0000", "");
        }
        else {
            // Write the file data
            try (FileOutputStream fileOutputStream = new FileOutputStream(filename, true)) {
                fileOutputStream.write(fileDataByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Pass the packet to the next handler in the chain
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }

    public static byte[] trimByteArray(byte [] data)
    {
        int nullByteIndex = -1;
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != 0) {
                nullByteIndex = i;
                break;
            }
        }
        byte[] trimmedData = new byte[nullByteIndex + 1];
        System.arraycopy(data, 0, trimmedData, 0, nullByteIndex + 1);
        return trimmedData;
    }
}
