package com.stephengoeddel.synchronization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeServer {

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        String host = argList.get(0);
        int port = Integer.parseInt(argList.get(1));

        List<String> hostsInRing = new ArrayList<>();
        List<Integer> portsInRing = new ArrayList<>();
        List<String> rawRingInformation = argList.subList(2, argList.size());
        for (int i = 0; i < rawRingInformation.size(); i++) {
            String hostOrPort = rawRingInformation.get(i);
            if (i % 2 == 0) {
                hostsInRing.add(hostOrPort);
            } else {
                portsInRing.add(Integer.parseInt(hostOrPort));
            }
        }
        Node node = Node.createNode(hostsInRing, portsInRing, host, port);

        CoordinatorCaller coordinatorCaller = new CoordinatorCaller(node);
        Thread thread = new Thread(coordinatorCaller);
        thread.start();

        ServerSocket serverSocket = new ServerSocket(port);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Received Connection");
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String messageType = null;
                    String coordinator = null;
                    List<String> nodes = new ArrayList<>();
                    String line;
                    while ((line = inputReader.readLine()) != null && !".".equals(line)) {
                        if (line.equals(Node.COORDINATOR) || line.equals(Node.ELECTION)) {
                            messageType = line;
                            if (messageType.equals(Node.COORDINATOR)) {
                                // The coordinator will be the first line after the message type
                                coordinator = inputReader.readLine();
                            }
                        } else {
                            nodes.add(line);
                        }
                    }

                    if (messageType != null) {
                        System.out.println("Received a(n) " + messageType + " message with " + nodes.size() + " nodes");
                        if (Node.COORDINATOR.equals(messageType)) {
                            node.handleIncomingCoordinationMessage(coordinator, nodes);
                        } else if (Node.ELECTION.equals(messageType)) {
                            node.handleIncomingElectionMessage(nodes);
                        } else {
                            System.out.println("Malformed messageType.");
                            System.exit(1);
                        }
                    } else {
                        System.out.println("Received a simple ping.");
                    }

                } finally {
                    System.out.println("Closing socket for " + node.getName());
                }
            }
        } finally {
            System.out.println("Exiting " + node.getName() + ".");
            serverSocket.close();
        }
    }
}
