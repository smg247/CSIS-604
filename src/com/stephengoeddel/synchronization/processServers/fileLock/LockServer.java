package com.stephengoeddel.synchronization.processServers.fileLock;


import com.stephengoeddel.synchronization.enums.LockAction;
import com.stephengoeddel.synchronization.enums.LockType;
import com.stephengoeddel.synchronization.enums.MessageType;
import com.stephengoeddel.synchronization.node.Node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class LockServer implements Runnable {
    private Node node;


    public LockServer(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(node.getLockPort());
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    MessageType messageType = MessageType.fromHeader(inputReader.readLine());
                    if (messageType != null) {
                        LockType lockType = messageType.getLockType();
                        LockAction lockAction = messageType.getLockAction();
                        if (!node.isCoordinator() && !lockAction.equals(LockAction.grant)) {
                            System.out.println(node.getName() + " received a " + lockAction.name() + " lock request, but it is not the coordinator, ignoring.");
                        } else {
                            if (LockAction.grant.equals(lockAction)) {
                                node.grantThisNodesLock(lockType);
                            } else {
                                String nodeName = inputReader.readLine();
                                long timeOfAttempt = Long.valueOf(inputReader.readLine());
                                if (LockAction.obtain.equals(lockAction)) {
                                    node.obtainLock(nodeName, lockType, timeOfAttempt);
                                } else if (LockAction.relinquish.equals(lockAction)) {
                                    node.relinquishLock(nodeName, lockType);
                                }
                            }
                        }
                    } else {
                        System.out.println("LockServer received malformed message.");
                    }
                } finally {
                    socket.close();
                }
            }
        } catch(Exception e) {
            System.out.println("Issue in the LockServer: " + e.getMessage());
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
