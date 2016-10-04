package com.stephengoeddel.synchronization.processServers.fileLock;


import com.stephengoeddel.synchronization.FileActionDriver;
import com.stephengoeddel.synchronization.enums.LockAction;
import com.stephengoeddel.synchronization.enums.LockType;
import com.stephengoeddel.synchronization.node.Node;

import java.io.PrintWriter;
import java.net.Socket;

public class FileWriter implements Runnable {
    private Node node;
    private String fileServerHost;
    private int fileServerPort;


    public FileWriter(Node node, String fileServerHost, int fileServerPort) {
        this.node = node;
        this.fileServerHost = fileServerHost;
        this.fileServerPort = fileServerPort;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(40000);
                if (!node.isHasWriteLock()) {
                    node.sendLockMessageForObtainOrRelinquish(LockType.write, LockAction.obtain);
                }
                if (node.isHasWriteLock()) {
                    writeToFile();
                    node.relinquishThisNodesLock(LockType.write);
                    node.sendLockMessageForObtainOrRelinquish(LockType.write, LockAction.relinquish);
                }
            } catch (Exception e) {
                System.out.println("FileWriter stopped: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private void writeToFile() throws Exception {
        Socket socket = new Socket(fileServerHost, fileServerPort);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

        printWriter.println(FileActionDriver.FileAccessType.write);
        printWriter.println(node.getName() + " was here at " + node.getTimeWithOffsetIncluded());
        printWriter.println(".");

        socket.close();
        System.out.println("Wrote a message on the file.");
    }
}