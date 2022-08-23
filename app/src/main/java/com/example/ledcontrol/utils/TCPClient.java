package com.example.ledcontrol.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

public class TCPClient implements Callable<Void> {
    private final String ipAddress;
    private final String port;
    private final String message;

    public TCPClient(String ipAddress, String port, String message) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.message = message;
    }

    @Override
    public Void call() throws IOException {
        //Create TCP connection
        Socket socket = new Socket(ipAddress, Integer.parseInt(port));
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        //Write required data to TCP stream
        outputStream.write(message.getBytes());
        return null;
    }
}
