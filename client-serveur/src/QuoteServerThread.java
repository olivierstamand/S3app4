
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

public class QuoteServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean moreQuotes = true;
    protected DataLinkHandlerServer1 dataLinkHandler1 = null;
    protected TransportHandlerServer transportHandler = null;
    protected ApplicationHandlerServer applicationHandler = null;
    protected DataLinkHandlerServer2 dataLinkHandler2 = null;

    protected String filename=null;
    public QuoteServerThread() throws IOException {

       this("QuoteServer");


    }

    public QuoteServerThread(String name) throws IOException {
        super(name);
        socket= new DatagramSocket(25000);
        dataLinkHandler1= new DataLinkHandlerServer1(socket);
        transportHandler= new TransportHandlerServer();
        applicationHandler= new ApplicationHandlerServer();
        dataLinkHandler2= new DataLinkHandlerServer2(socket);
        // Connect the handlers in the chain
        dataLinkHandler1.setNextHandler(transportHandler);
        transportHandler.setNextHandler(applicationHandler);
        applicationHandler.setNextHandler(dataLinkHandler2);


    }

    public void run() {




                while (true) {
                    try {
                        byte[] buf = new byte[200];
                        DatagramPacket dataPacket = new DatagramPacket(buf, buf.length);
                        //socket.receive(dataPacket);

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