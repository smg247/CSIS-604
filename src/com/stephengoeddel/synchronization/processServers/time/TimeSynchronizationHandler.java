package com.stephengoeddel.synchronization.processServers.time;


import com.stephengoeddel.synchronization.enums.MessageType;
import com.stephengoeddel.synchronization.node.Node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeSynchronizationHandler implements Runnable {
    private Node node;


    public TimeSynchronizationHandler(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(node.getTimePollingPort());
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String header = inputReader.readLine();
                    if (MessageType.timePoll.getHeader().equals(header)) {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        printWriter.println(MessageType.timeResponse.getHeader());
                        printWriter.println(node.getTimeWithOffsetIncluded());
                        printWriter.println(".");
                    } else if (MessageType.timeOffset.getHeader().equals(header)) {
                        long offset = Long.valueOf(inputReader.readLine());
                        System.out.println(node.getName() + " has received an updated time offset of " + offset);
                        node.updateTimeOffset(offset);
                    } else {
                        System.out.println("Received malformed time polling message with header: " + header);
                        System.exit(1);
                    }
                } catch (Exception ignore) {
                    System.out.println(node.getName() + " noticed that the coordinator was down while trying to respond to a time polling message, going to request a new election.");
                    node.sendElectionMessage();
                } finally {
                    socket.close();
                }
            }
        } catch(Exception e) {
            System.out.println("Issue in the TimeSynchronizationHandler: " + e.getMessage());
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
