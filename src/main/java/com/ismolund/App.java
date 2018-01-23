package com.ismolund;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class App
{
    private static ServerSocket server;
    private static Socket clientSocket;
    private static DataInputStream input;
    private static PrintStream output;
    private static BufferedReader reader;

    private static void setUpConnection() throws IOException {
        clientSocket = server.accept();
        input = new DataInputStream(clientSocket.getInputStream());
        output = new PrintStream(clientSocket.getOutputStream());

        reader = new BufferedReader(new InputStreamReader(input));
    }

    public static void main( String[] args )
    {
        System.out.println( "Initializing Server..." );

        try {
            server = new ServerSocket(7000);
        } catch (IOException e) {
            e.printStackTrace();
            shutdown();
            return;
        }

        try {
            setUpConnection();

            while (true) {
                String outputString = reader.readLine();
                if (outputString != null) {
                    if (outputString.contains("quit")) {
                        break;
                    } else {
                        System.out.println(outputString + "\n");
                    }

                    if (outputString.contains("disconnected")) {
                        System.out.println("Resetting socket...");
                        input.close();
                        output.close();
                        clientSocket.close();
                        setUpConnection();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void shutdown() {
        try {
            output.close();
            input.close();
            clientSocket.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
