import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApplicationHandlerServer extends BaseHandler {
    public void handlePacket(DatagramPacket packet) throws IOException {
        // Perform Application Layer processing here
        byte[] packetData = packet.getData();
        String packetString = new String(packetData, 0, packet.getLength(), StandardCharsets.UTF_8);
        JSONObject packetJson = new JSONObject(packetString);
        String fileName = packetJson.getString("fileName");
        byte[] fileData = Base64.getDecoder().decode(packetJson.getString("fileData"));

        // Write the file data
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName, true)) {
            fileOutputStream.write(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Pass the packet to the next handler in the chain
        if (nextHandler != null) {
            nextHandler.handlePacket(packet);
        }
    }
}
