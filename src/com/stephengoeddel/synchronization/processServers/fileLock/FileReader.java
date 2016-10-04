package com.stephengoeddel.synchronization.processServers.fileLock;


import com.stephengoeddel.synchronization.FileActionDriver;
import com.stephengoeddel.synchronization.enums.LockAction;
import com.stephengoeddel.synchronization.enums.LockType;
import com.stephengoeddel.synchronization.node.Node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileReader implements Runnable {
    private Node node;
    private String fileServerHost;
    private int fileServerPort;


    public FileReader(Node node, String fileServerHost, int fileServerPort) {
        this.node = node;
        this.fileServerHost = fileServerHost;
        this.fileServerPort = fileServerPort;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20000);
                if (!node.isHasReadLock()) {
                    node.sendLockMessageForObtainOrRelinquish(LockType.read, LockAction.obtain);
                }
//                while (!node.isHasReadLock()) {
//                    Thread.sleep(3000);
//                }
                if (node.isHasReadLock()) {
                    outputFileContents();
                    node.relinquishThisNodesLock(LockType.read);
                    node.sendLockMessageForObtainOrRelinquish(LockType.read, LockAction.relinquish);
                }
            } catch (Exception e) {
                System.out.println("FileReader stopped: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private void outputFileContents() throws Exception {
        Socket socket = new Socket(fileServerHost, fileServerPort);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        printWriter.println(FileActionDriver.FileAccessType.read.name());
        printWriter.println(".");

        List<String> contentsOfFile = new ArrayList<>();
        String line;
        while ((line = inputReader.readLine()) != null && !line.equals(".")) {
            contentsOfFile.add(line);
        }

        socket.close();

        System.out.println();
        System.out.print("The file contains " + contentsOfFile.size() + " lines ");
        if (contentsOfFile.size() > 5) {
            System.out.println("here are the last 5: ");
            for (int i = contentsOfFile.size() - 5; i < contentsOfFile.size(); i++) {
                String lineInFile = contentsOfFile.get(i);
                System.out.println(lineInFile);
            }
        } else {
            System.out.println("here they are: ");
            for (String lineInFile : contentsOfFile) {
                System.out.println(lineInFile);
            }
        }
        System.out.println();
    }
}