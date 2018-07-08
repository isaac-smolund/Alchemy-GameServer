package com.ismolund;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class App
{
    private static ServerSocket server;
    private static Socket clientSocket;
    private static BufferedReader reader;

    private static String mostRecentState = "";
    private static final Map<ConnectionName, ConnectionHandler> connections
            = new HashMap<>();
    private static final Map<ConnectionName, String> messageQueue
            = new HashMap<>();

    private static boolean running = true;
    private static boolean lock = false;

    public enum ConnectionName {A, B, SURPLUS};

    public static boolean running() {
        return running;
    }

    private static class ConnectionThread extends Thread {
        @Override
        public void run() {
            while (running) {
                // Check for connections:
                try {
                    clientSocket = server.accept();
                } catch (IOException e) {
                    System.out.println("Could not establish connection to client.");
                    e.printStackTrace();
                }
                ConnectionName name = ConnectionName.SURPLUS;
                if (!connections.containsKey(ConnectionName.A)) {
                    name = ConnectionName.A;
                } else if (!connections.containsKey(ConnectionName.B)) {
                    name = ConnectionName.B;
                }
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket, name);
                connections.put(name, connectionHandler);
                new Thread(connectionHandler).start();
                System.out.println("Started thread " + name + ".");

                // Check for messages:
                if (!messageQueue.isEmpty()) {
                    //TODO: Factor this into helper method:
                    for (Map.Entry<ConnectionName, String> message : messageQueue.entrySet()) {
                        System.out.println("Sending message \"" + message.getValue() + "\" to client " + message.getKey());
                        ConnectionHandler handler = connections.get(message.getKey());
                        if (handler != null) {
                            handler.sendMessage(message.getValue());
                            //TODO: move this to ConnectionHandler
                            if (message.getValue().contains("disconnected")) {
                                System.out.println("Client " + message.getKey() + " left! Resetting socket...");
                                handler.shutdown();
                                connections.remove(message.getKey());
                            }
                        } else {
                            System.out.println("Handler for client " + message.getKey() + " was null!");
                        }
                    }
                }
            }
        }
    }

    public static void messageReceived(ConnectionName connection, String message) {
        if (connection.equals(ConnectionName.SURPLUS)) {
            return;
        }
        if (!lock) {
            System.out.println("Queuing message for connection " + connection);
            lock = true;
            ConnectionName toSend = connection == ConnectionName.A ? ConnectionName.B : ConnectionName.A;
            messageQueue.put(toSend, message);
//            ConnectionHandler handler = connections.get(toSend);
//            handler.sendMessage(message);
            lock = false;
        }
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

        new ConnectionThread().start();

//        try {
            while (true) {
                if (!messageQueue.isEmpty()) {
                    for (Map.Entry<ConnectionName, String> message : messageQueue.entrySet()) {
                        System.out.println("Sending message \"" + message.getValue() + "\" to client " + message.getKey());
                        ConnectionHandler handler = connections.get(message.getKey());
                        if (handler != null) {
                            handler.sendMessage(message.getValue());
                        } else {
                            System.out.println("Handler for client " + message.getKey() + " was null!");
                        }
                    }
                    //TODO: remove commented code
//                setUpConnection();
//                String clientOutput = reader.readLine();
//                if (clientOutput != null) {
//                    if (clientOutput.contains("quit")) {
//                        running = false;
//                        break;
//                    } else {
//                        if (!clientOutput.startsWith("{")) {
//                            System.out.println(clientOutput + "\n");
//                        } else {
//                            mostRecentState = clientOutput;
//                        }
//                        if (clientOutput.contains("Player 2's turn")) {
//                            Thread.sleep(1000);
//                            String response = mostRecentState.replaceAll("30", "25");
//                            outputA.writeBytes(response + "\n");
//                            outputA.writeBytes("end turn\n");
//                        }
//                    }
//
//                    if (clientOutput.contains("disconnected")) {
//                        System.out.println("Resetting socket...");
//                        inputA.close();
//                        outputA.close();
//                        clientSocket.close();
//                        setUpConnection();
//                    }
                }
            }

//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private static void shutdown() {
        try {
            clientSocket.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
