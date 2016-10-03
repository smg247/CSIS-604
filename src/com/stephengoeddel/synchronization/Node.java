package com.stephengoeddel.synchronization;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Node {
    private NodeRepresentation thisNode;
    private List<NodeRepresentation> nodesInRing;
    private NodeRepresentation coordinatorNode;


    static Node createNode(List<String> hostsInRing, List<Integer> portsInRing, String host, int port) {
        if (hostsInRing.size() != portsInRing.size()) {
            throw new IllegalArgumentException("Hosts list size does not match ports list size");
        }

        List<NodeRepresentation> nodesInRing = new ArrayList<>();
        int thisNodesPosition = -1;
        for (int i = 0; i < hostsInRing.size(); i++) {
            nodesInRing.add(new NodeRepresentation(hostsInRing.get(i), portsInRing.get(i), i));

            if (hostsInRing.get(i).equals(host) && portsInRing.get(i).equals(port)) {
                thisNodesPosition = i;
            }
        }

        return new Node(new NodeRepresentation(host, port, thisNodesPosition), nodesInRing);
    }


    private Node(NodeRepresentation thisNode, List<NodeRepresentation> nodesInRing) {
        this.thisNode = thisNode;
        this.nodesInRing = nodesInRing;
    }

    void sendElectionMessage() {
        System.out.println(thisNode.getName() + " is requesting a new election.");
        sendMessage(MessageType.election, new ArrayList<>());
    }

    void handleIncomingElectionMessage(List<String> nodes) {
        System.out.println(thisNode.getName() + " is handling an incoming election message.");
        boolean electionIsComplete = nodes.contains(thisNode.getName());
        if (electionIsComplete) {
            coordinatorNode = determineCoordinatorNode(nodes);
            System.out.println(thisNode.getName() + " has determined the new Coordinator to be " + coordinatorNode.getName() + " and is sending a new coordinator message.");
            sendMessage(MessageType.coordinator, new ArrayList<>());
        } else {
            System.out.println(thisNode.getName() + " is forwarding along an election message");
            sendMessage(MessageType.election, nodes);
        }
    }

    void handleIncomingCoordinationMessage(String coordinatorName, List<String> nodes) {
        coordinatorNode = NodeRepresentation.fromName(coordinatorName);
        System.out.println(thisNode.getName() + " has updated its coordinator to " + coordinatorNode.getName());
        boolean coordinationIsComplete = nodes.contains(thisNode.getName());
        if (!coordinationIsComplete) {
            sendMessage(MessageType.coordinator, nodes);
        }
    }

    boolean isCoordinatorActive() {
        if (!isCoordinator()) {
            try {
                Socket socket = new Socket(coordinatorNode.getHost(), coordinatorNode.getPort());
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println(MessageType.ping.getHeader());
                printWriter.println(".");
                socket.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private void sendMessage(MessageType messageType, List<String> currentNodesInMessage) {
        NodeRepresentation successor = determineSuccessor(thisNode.getName());
        while (successor != null) {
            if (successor.equals(thisNode)) {
                System.out.println(thisNode.getName() + " has determined that it is the only active node in the ring and is setting itself as the coordinator.");
                coordinatorNode = successor;
                break;
            } else {
                try {
                    Socket socket = new Socket(successor.getHost(), successor.getPort());
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

                    printWriter.println(messageType.getHeader());
                    if (MessageType.coordinator == messageType) {
                        printWriter.println(coordinatorNode.getName());
                    }

                    for (String nodeInMessage : currentNodesInMessage) {
                        printWriter.println(nodeInMessage);
                    }
                    printWriter.println(thisNode.getName());
                    printWriter.println(".");

                    socket.close();
                    successor = null;
                } catch (IOException e) {
                    System.out.print(successor.getName() + " is down " + thisNode.getName() + "'s new successor is ");
                    successor = determineSuccessor(successor.getName());
                    System.out.println(successor.getName());
                }
            }
        }
    }

    private NodeRepresentation determineSuccessor(String checkFromNodeName) {
        for (int i = 0; i < nodesInRing.size(); i++) {
            String node = nodesInRing.get(i).getName();
            if (node.equals(checkFromNodeName)) {
                if (i == nodesInRing.size() - 1) {
                    return nodesInRing.get(0);
                } else {
                    return nodesInRing.get(i + 1);
                }
            }
        }

        return null;
    }

    private NodeRepresentation determineCoordinatorNode(List<String> nodeNames) {
        List<NodeRepresentation> nodeRepresentations = new ArrayList<>();
        for (String nodeName : nodeNames) {
            nodeRepresentations.add(NodeRepresentation.fromName(nodeName));
        }

        Collections.sort(nodeRepresentations);
        return nodeRepresentations.get(nodeRepresentations.size() - 1);
    }

    private boolean isCoordinator() {
        return thisNode.equals(coordinatorNode);
    }

    String getName() {
        return thisNode.getName();
    }
}
