

import java.io.*;
import java.net.*;
/**
 * The QuoteServerThread class represents a server thread that handles the reception and processing
 * of DatagramPackets. It implements the logic for a quote server.
 */
public class QuoteServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean moreQuotes = true;
    protected HandlerInterface dataLinkHandler1 = null;
    protected HandlerInterface transportHandler = null;
    protected HandlerInterface applicationHandler = null;
    protected HandlerInterface dataLinkHandler2 = null;

    protected String filename = null;

    /**
     * Constructs a QuoteServerThread with the default name "QuoteServer" and creates a DatagramSocket.
     *
     * @throws IOException if an I/O error occurs while creating the DatagramSocket.
     */
    public QuoteServerThread() throws IOException {
        this("QuoteServer");
    }

    /**
     * Constructs a QuoteServerThread with the specified name and creates a DatagramSocket.
     *
     * @param name the name of the server thread.
     * @throws IOException if an I/O error occurs while creating the DatagramSocket.
     */
    public QuoteServerThread(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(35000);
        dataLinkHandler1 = new DataLinkHandlerServer1(socket);
        transportHandler = new TransportHandlerServer();
        applicationHandler = new ApplicationHandlerServer();
        dataLinkHandler2 = new DataLinkHandlerServer2(socket);
        dataLinkHandler1.setSocket(socket);
        transportHandler.setSocket(socket);
        applicationHandler.setSocket(socket);

        // Connect the handlers in the chain
        dataLinkHandler1.setNextHandler(transportHandler);
        transportHandler.setNextHandler(applicationHandler);
        applicationHandler.setNextHandler(dataLinkHandler2);
    }

    /**
     * Runs the server thread, continuously receiving DatagramPackets and passing them to the
     * first handler in the chain for processing.
     */
    public void run() {
        while (true) {
            try {
                byte[] buf = new byte[200];
                DatagramPacket dataPacket = new DatagramPacket(buf, buf.length);

                // Pass the packet to the first handler in the chain
                dataLinkHandler1.handlePacket(dataPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!moreQuotes)
                break;
        }

        socket.close();
    }
}
