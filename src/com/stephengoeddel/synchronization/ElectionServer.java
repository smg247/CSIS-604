package com.stephengoeddel.synchronization;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ElectionServer implements Runnable {
    private Node node;


    public ElectionServer(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(node.getElectionPort());
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    MessageType messageType;
                    String coordinator = null;
                    List<String> nodes = new ArrayList<>();
                    String line = inputReader.readLine();
                    messageType = MessageType.fromHeader(line);
                    if (MessageType.coordinator.equals(messageType)) {
                        // The coordinator will be the first line after the message type
                        coordinator = inputReader.readLine();
                    }
                    if (MessageType.coordinator.equals(messageType) || MessageType.election.equals(messageType)) {
                        while ((line = inputReader.readLine()) != null && !".".equals(line)) {
                            nodes.add(line);
                        }
                    }

                    if (messageType != null) {
                        System.out.println("Received a(n) " + messageType + " message with " + nodes.size() + " nodes");
                        if (MessageType.coordinator.equals(messageType)) {
                            node.handleIncomingCoordinationMessage(coordinator, nodes);
                        } else if (MessageType.election.equals(messageType)) {
                            node.handleIncomingElectionMessage(nodes);
                        }
                    } else {
                        System.out.println("Malformed messageType.");
                        System.exit(1);
                    }

                } finally {
                    System.out.println("Closing socket for " + node.getName());
                }
            }
        } catch(Exception e) {
            System.out.println("Issue in the ElectionServer: " + e.getMessage());
        } finally {
            System.out.println("Exiting " + node.getName() + ".");
            try {
                serverSocket.close();
            } catch (Exception e) {
                System.exit(1);
            }
        }
    }
}
