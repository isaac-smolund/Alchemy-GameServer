package com.ismolund;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class ConnectionHandler implements Runnable {

    private final Socket socket;
    private final App.ConnectionName name;

    private DataInputStream input;
    private DataOutputStream output;

    private BufferedReader reader;

    private final int MAX_MESSAGE_LENGTH = 50;

    public ConnectionHandler(Socket socket, App.ConnectionName name) {
        this.socket = socket;
        this.name = name;
    }

    private String format(String message) {
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return message.substring(0, MAX_MESSAGE_LENGTH) + "... [truncated]";
        }
        return message;
    }

    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            output.writeBytes("Connection to server for client \"" + name + "\" made successfully.\n");
            reader = new BufferedReader(new InputStreamReader(input));

            while (App.running()) {
                String clientInput = reader.readLine();
                if (clientInput != null) {
                    System.out.println("[" + LocalDateTime.now() + " CLIENT " + name.name() + "]: " +
                            format(clientInput));
                    App.messageReceived(name, clientInput);
                }
            }
        } catch (IOException e) {
            System.out.print("Could not connect to client \"" + name + "\".");
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            output.writeBytes(message);
        } catch (IOException e) {
            System.out.println("Unable to send message \"" + message + "\" to client " + name + ".");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            output.close();
            input.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
