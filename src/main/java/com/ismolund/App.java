package com.ismolund;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class App
{
    private static ServerSocket server;
    private static Socket clientSocket;
    private static DataInputStream input;
    private static DataOutputStream output;
    private static BufferedReader reader;

    private static String mostRecentState = "";

    private static void setUpConnection() throws IOException {
        clientSocket = server.accept();
        input = new DataInputStream(clientSocket.getInputStream());
        output = new DataOutputStream(clientSocket.getOutputStream());
        output.writeBytes("hello i am server\n");

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
                String clientOutput = reader.readLine();
                if (clientOutput != null) {
                    if (clientOutput.contains("quit")) {
                        break;
                    } else {
                        if (!clientOutput.startsWith("{")) {
                            System.out.println(clientOutput + "\n");
                        } else {
                            mostRecentState = clientOutput;
                        }
                        if (clientOutput.contains("Player 2's turn")) {
                            Thread.sleep(1000);
                            String response = mostRecentState.replaceAll("30", "25");
                            output.writeBytes(response + "\n");
                            output.writeBytes("end turn\n");
//                            System.out.println("Turn started - choosing energy");
//                            output.writeBytes("yellow\n");
//                            Thread.sleep(1000);
//                            System.out.println("playing card");
//                            output.writeBytes("play Scrumpo Bungus\n");
//                            Thread.sleep(1000);
//                            System.out.println("ending turn");
//                            output.writeBytes("end turn\n");

                        }
                    }

                    if (clientOutput.contains("disconnected")) {
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
        } catch (InterruptedException e) {
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
