package com.stephengoeddel.synchronization.node;

import com.stephengoeddel.synchronization.enums.LockAction;
import com.stephengoeddel.synchronization.enums.LockType;
import com.stephengoeddel.synchronization.enums.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Node {
    private NodeRepresentation thisNode;
    private List<NodeRepresentation> nodesInRing;
    private NodeRepresentation coordinatorNode;
    private Set<NodeRepresentation> nodesWithReadLocks;
    private NodeRepresentation nodeWithWriteLock;
    private List<LockAttempt> lockQueue;
    private long timeOffset;
    private boolean hasReadLock;
    private boolean hasWriteLock;


    public static Node createNode(List<String> hostsInRing, List<Integer> portsInRing, String host, int port) {
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
        nodesWithReadLocks = new HashSet<>();
        lockQueue = new ArrayList<>();
    }

    public void sendElectionMessage() {
        System.out.println(thisNode.getName() + " is requesting a new election.");
        sendMessageIncludingNodes(MessageType.election, new ArrayList<>());
    }

    public void handleIncomingElectionMessage(List<String> nodes) {
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

    public void handleIncomingCoordinationMessage(String coordinatorName, List<String> nodes) {
        coordinatorNode = NodeRepresentation.fromName(coordinatorName);
        System.out.println(thisNode.getName() + " has updated its coordinator to " + coordinatorNode.getName());
        hasWriteLock = false;
        hasReadLock = false;
        System.out.println(thisNode.getName() + " has relinquished any active locks due to receiving a new coordinator.");
        boolean coordinationIsComplete = nodes.contains(thisNode.getName());
        if (!coordinationIsComplete) {
            sendMessageIncludingNodes(MessageType.coordinator, nodes);
        }
    }

    public boolean isCoordinatorActive() {
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

    public void pollOtherNodesForTimeAndNotifyOffsets() {
        Map<NodeRepresentation, Long> timeFromNodes = new HashMap<>();
        for (NodeRepresentation node : nodesInRing) {
            if (node.equals(thisNode)) {
                timeFromNodes.put(node, getTimeWithOffsetIncluded());
            } else {
                try {
                    long startTime = getTimeWithOffsetIncluded();
                    Socket socket = new Socket(node.getHost(), node.getTimePollingPort());
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                    printWriter.println(MessageType.timePoll.getHeader());
                    printWriter.println(".");

                    String line = inputReader.readLine();
                    long endTime = getTimeWithOffsetIncluded();
                    long roundTripTime = endTime - startTime;
                    if (MessageType.timeResponse.getHeader().equals(line)) {
                        long timeFromNode = Long.valueOf(inputReader.readLine());
                        timeFromNodes.put(node, timeFromNode + (roundTripTime/2));
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

    public long getTimeWithOffsetIncluded() {
        return System.currentTimeMillis() + timeOffset;
    }

    public void sendLockMessageForObtainOrRelinquish(LockType lockType, LockAction lockAction) {
        if (isCoordinator()) {
            // No need to send out a message if we are the coordinator
            if (LockAction.obtain.equals(lockAction)) {
                obtainLock(thisNode.getName(), lockType, getTimeWithOffsetIncluded());
            } else if (LockAction.relinquish.equals(lockAction)) {
                relinquishLock(thisNode.getName(), lockType);
            }
        } else {
            try {
                Socket socket = new Socket(coordinatorNode.getHost(), coordinatorNode.getLockPort());
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                MessageType messageType = MessageType.forLockTypeAndLockAction(lockType, lockAction);
                if (messageType != null) {
                    printWriter.println(messageType.getHeader());
                    printWriter.println(thisNode.getName());
                    printWriter.println(getTimeWithOffsetIncluded());
                    printWriter.println(".");
                } else {
                    System.out.println("Lock Type is not valid.");
                    System.exit(1);
                }

                socket.close();

                System.out.println(thisNode.getName() + " has requested to " + lockAction.name() + " a " + lockType.name() + " lock.");
            } catch (Exception ignore) {
                System.out.println(thisNode.getName() + " noticed that the coordinator was down while attempting to " + lockAction.name() + " a lock.");
                sendElectionMessage();
            }
        }
    }

    private List<LockAttempt> determineLockAttemptsToBeGranted() {
        List<LockAttempt> lockAttempts = new ArrayList<>();
        if (!lockQueue.isEmpty() && !writeLockCurrentlyOut()) {
            Collections.sort(lockQueue);
            for (LockAttempt lockAttempt : lockQueue) {
                if (LockType.read.equals(lockAttempt.getLockType())) {
                    lockAttempts.add(lockAttempt);
                } else if (!readLockCurrentlyOut() && lockAttempts.isEmpty()) {
                    lockAttempts.add(lockAttempt);
                    break;
                }
            }
        }

        return lockAttempts;
    }

    private void grantAccessToApplicableLocksAndSendMessage() {
        List<LockAttempt> lockAttemptsToBeGranted = determineLockAttemptsToBeGranted();
        for (LockAttempt lockAttempt : lockAttemptsToBeGranted) {
            lockQueue.remove(lockAttempt);
            NodeRepresentation nodeRepresentation = lockAttempt.getNodeRepresentation();
            LockType lockType = lockAttempt.getLockType();

            if (thisNode.equals(nodeRepresentation)) {
                grantThisNodesLock(lockAttempt.getLockType());
                addLock(lockType, thisNode);
            } else {
                try {
                    Socket socket = new Socket(nodeRepresentation.getHost(), nodeRepresentation.getLockPort());
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    MessageType messageType = MessageType.forLockTypeAndLockAction(lockType, LockAction.grant);
                    if (messageType != null) {
                        printWriter.println(messageType.getHeader());
                        printWriter.println(".");
                    } else {
                        System.out.println("Lock Type is not valid.");
                        System.exit(1);
                    }

                    addLock(lockType, nodeRepresentation);
                    socket.close();
                } catch (Exception ignore) {
                    System.out.println(thisNode.getName() + " attempted to grant access to a " + lockType + " lock, but " + nodeRepresentation.getName() + " was unavailable");
                }
            }
        }

        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("After handing out " + lockAttemptsToBeGranted.size() + " lock(s) the queue now has " + lockQueue.size() + " lock request(s) left.");
        System.out.println("There are now " + nodesWithReadLocks.size() + " node(s) with read locks.");
        System.out.println("The write lock is currently " + (writeLockCurrentlyOut() ? "in use" : "free"));
        System.out.println("------------------------------------------------");
        System.out.println();
    }

    public void relinquishLock(String nodeName, LockType lockType) {
        NodeRepresentation nodeRepresentation = NodeRepresentation.fromName(nodeName);
        if (LockType.write.equals(lockType)) {
            removeWriteLock(nodeRepresentation);
        } else if (LockType.read.equals(lockType)) {
            removeReadLock(nodeRepresentation);
        }

        grantAccessToApplicableLocksAndSendMessage();
    }

    public void obtainLock(String nodeName, LockType lockType, long timeOfAttempt) {
        NodeRepresentation nodeRepresentation = NodeRepresentation.fromName(nodeName);
        LockAttempt lockAttempt = new LockAttempt(nodeRepresentation, timeOfAttempt, lockType);
        addLockAttemptToLockQueue(lockAttempt);
        grantAccessToApplicableLocksAndSendMessage();
    }

    private boolean writeLockCurrentlyOut() {
        return nodeWithWriteLock != null;
    }

    private void removeWriteLock(NodeRepresentation nodeRepresentation) {
        if (nodeWithWriteLock != null && nodeRepresentation.equals(nodeWithWriteLock)) {
            nodeWithWriteLock = null;
            System.out.println(thisNode.getName() + " just removed " + nodeRepresentation.getName() + "'s write lock.");
        }
    }

    private boolean readLockCurrentlyOut() {
        return nodesWithReadLocks.size() > 0;
    }

    private void removeReadLock(NodeRepresentation nodeRepresentation) {
        Iterator<NodeRepresentation> iterator = nodesWithReadLocks.iterator();
        while (iterator.hasNext()) {
            NodeRepresentation nodeWithReadLock = iterator.next();
            if (nodeWithReadLock.equals(nodeRepresentation)) {
                iterator.remove();
                System.out.print(thisNode.getName() + " just removed the read lock from " + nodeRepresentation.getName() + " ");
                break;
            }
        }

        System.out.println("there are now " + nodesWithReadLocks.size() + " nodes with read locks.");
    }

    private void addLock(LockType lockType, NodeRepresentation nodeRepresentation) {
        if (LockType.write.equals(lockType)) {
            nodeWithWriteLock = nodeRepresentation;
        } else if (LockType.read.equals(lockType)) {
            nodesWithReadLocks.add(nodeRepresentation);
        }
    }

    private void addLockAttemptToLockQueue(LockAttempt lockAttempt) {
        if (!lockQueue.contains(lockAttempt)) {
            lockQueue.add(lockAttempt);
            System.out.println(thisNode.getName() + " added " + lockAttempt.getNodeRepresentation().getName() + "'s lock request to the queue in slot " + lockQueue.size());
        }
    }

    public void updateTimeOffset(long timeOffsetChange) {
        timeOffset += timeOffsetChange;
    }

    public boolean isCoordinator() {
        return thisNode.equals(coordinatorNode);
    }

    public String getName() {
        return thisNode.getName();
    }

    public int getElectionPort() {
        return thisNode.getElectionPort();
    }

    public int getTimePollingPort() {
        return thisNode.getTimePollingPort();
    }

    public int getLockPort() {
        return thisNode.getLockPort();
    }

    public boolean isHasReadLock() {
        return hasReadLock;
    }

    public void relinquishThisNodesLock(LockType lockType) {
        if (LockType.write.equals(lockType)) {
            hasWriteLock = false;
        } else if (LockType.read.equals(lockType)) {
            hasReadLock = false;
        }

        System.out.println(thisNode.getName() + " has relinquished its " + lockType.name() + " lock locally.");
    }

    public void grantThisNodesLock(LockType lockType) {
        if (LockType.write.equals(lockType)) {
            hasWriteLock = true;
        } else if (LockType.read.equals(lockType)) {
            hasReadLock = true;
        }

        System.out.println(thisNode.getName() + " has been granted a " + lockType.name() + " lock.");
    }

    public boolean isHasWriteLock() {
        return hasWriteLock;
    }
}
