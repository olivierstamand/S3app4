import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;

public class TransportHandlerServer extends BaseHandler {
    public void handlePacket(DatagramPacket packet) throws IOException{
        String packetString = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        JSONObject packetJson = new JSONObject(packetString);
        String fileName = packetJson.getString("fileName");
        byte[] fileData = Base64.getDecoder().decode(packetJson.getString("fileData"));
        long crcExpected = packetJson.getLong("CRC");
        CRC32 crc32 = new CRC32();
        crc32.update(fileData);
        long crcCalculated = crc32.getValue();
        if (crcCalculated != crcExpected) {
            // Handle logic for error detection in the Transport Layer
        }
    }
}
