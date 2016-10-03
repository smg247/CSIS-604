package com.stephengoeddel.synchronization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

class Node {
    private NodeRepresentation thisNode;
    private List<NodeRepresentation> nodesInRing;
    private NodeRepresentation coordinatorNode;
    private long timeOffset;


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
        timeOffset = 0;
    }

    void sendElectionMessage() {
        System.out.println(thisNode.getName() + " is requesting a new election.");
        sendMessageIncludingNodes(MessageType.election, new ArrayList<>());
    }

    void handleIncomingElectionMessage(List<String> nodes) {
        System.out.println(thisNode.getName() + " is handling an incoming election message.");
        boolean electionIsComplete = nodes.contains(thisNode.getName());
        if (electionIsComplete) {
            coordinatorNode = determineCoordinatorNode(nodes);
            System.out.println(thisNode.getName() + " has determined the new Coordinator to be " + coordinatorNode.getName() + " and is sending a new coordinator message.");
            sendMessageIncludingNodes(MessageType.coordinator, new ArrayList<>());
        } else {
            System.out.println(thisNode.getName() + " is forwarding along an election message");
            sendMessageIncludingNodes(MessageType.election, nodes);
        }
    }

    void handleIncomingCoordinationMessage(String coordinatorName, List<String> nodes) {
        coordinatorNode = NodeRepresentation.fromName(coordinatorName);
        System.out.println(thisNode.getName() + " has updated its coordinator to " + coordinatorNode.getName());
        boolean coordinationIsComplete = nodes.contains(thisNode.getName());
        if (!coordinationIsComplete) {
            sendMessageIncludingNodes(MessageType.coordinator, nodes);
        }
    }

    boolean isCoordinatorActive() {
        if (!isCoordinator()) {
            try {
                Socket socket = new Socket(coordinatorNode.getHost(), coordinatorNode.getElectionPort());
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

    private void sendMessageIncludingNodes(MessageType messageType, List<String> currentNodesInMessage) {
        NodeRepresentation successor = determineSuccessor(thisNode.getName());
        while (successor != null) {
            if (successor.equals(thisNode)) {
                System.out.println(thisNode.getName() + " has determined that it is the only active node in the ring and is setting itself as the coordinator.");
                coordinatorNode = successor;
                break;
            } else {
                try {
                    Socket socket = new Socket(successor.getHost(), successor.getElectionPort());
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

    void pollOtherNodesForTimeAndNotifyOffsets() {
        Map<NodeRepresentation, Long> timeFromNodes = new HashMap<>();
        for (NodeRepresentation node : nodesInRing) {
            if (node.equals(thisNode)) {
                timeFromNodes.put(node, getTimeWithOffsetIncluded());
            } else {
                try {
                    Socket socket = new Socket(node.getHost(), node.getTimePollingPort());
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                    printWriter.println(MessageType.timePoll.getHeader());
                    printWriter.println(".");

                    String line = inputReader.readLine();
                    if (MessageType.timeResponse.getHeader().equals(line)) {
                        long timeFromNode = Long.valueOf(inputReader.readLine());
                        timeFromNodes.put(node, timeFromNode);
                    } else {
                        System.out.println("Received message with a header of: " + line + " which was unexpected");
                    }
                    socket.close();
                } catch (Exception ignore) {
                    System.out.println(thisNode.getName() + " attempted to poll " + node.getName() + " for it's time, but it was unavailable.");
                }
            }
        }

        long averageTime = 0;
        for (long time : timeFromNodes.values()) {
            averageTime += time;
        }

        averageTime = averageTime/timeFromNodes.size();
        System.out.println(thisNode.getName() + " computed the average time to be " + averageTime);
        notifyNodesOfOffsets(averageTime, timeFromNodes);
    }

    private void notifyNodesOfOffsets(long averageTime, Map<NodeRepresentation, Long> timeFromNodes) {
        for (NodeRepresentation node : timeFromNodes.keySet()) {
            long offsetForNode = averageTime - timeFromNodes.get(node);
            if (node.equals(thisNode)) {
                timeOffset = offsetForNode;
                System.out.println(thisNode.getName() + " is setting it's own offset to " + timeOffset);
            } else {
                try {
                    System.out.println("Computed " + node.getName() + "'s time to be " + + timeFromNodes.get(node) + " making its offset " + offsetForNode);
                    Socket socket = new Socket(node.getHost(), node.getTimePollingPort());
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(MessageType.timeOffset.getHeader());
                    printWriter.println(offsetForNode);
                    printWriter.println(".");
                    socket.close();
                } catch (Exception ignore) {
                    System.out.println(thisNode.getName() + " attempted to send " + node.getName() + " it's time offset, but it was down.");
                }
            }
        }
    }

    long getTimeWithOffsetIncluded() {
        return System.currentTimeMillis() + timeOffset;
    }

    void updateTimeOffset(long timeOffsetChange) {
        timeOffset += timeOffsetChange;
    }

    boolean isCoordinator() {
        return thisNode.equals(coordinatorNode);
    }

    String getName() {
        return thisNode.getName();
    }

    int getElectionPort() {
        return thisNode.getElectionPort();
    }

    int getTimePollingPort() {
        return thisNode.getTimePollingPort();
    }
}
