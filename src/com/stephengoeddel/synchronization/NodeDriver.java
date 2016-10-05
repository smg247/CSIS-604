package com.stephengoeddel.synchronization;

import com.stephengoeddel.synchronization.node.Node;
import com.stephengoeddel.synchronization.processServers.election.ElectionHandler;
import com.stephengoeddel.synchronization.processServers.fileLock.FileReader;
import com.stephengoeddel.synchronization.processServers.fileLock.FileWriter;
import com.stephengoeddel.synchronization.processServers.fileLock.LockHandler;
import com.stephengoeddel.synchronization.processServers.time.TimePoller;
import com.stephengoeddel.synchronization.processServers.time.TimeSynchronizationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeDriver {

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        String host = argList.get(0);
        int port = Integer.parseInt(argList.get(1));
        String fileServerHost = argList.get(2);
        int fileServerReadPort = Integer.parseInt(argList.get(3));
        int fileServerWritePort = fileServerReadPort + 1;

        List<String> hostsInRing = new ArrayList<>();
        List<Integer> portsInRing = new ArrayList<>();
        List<String> rawRingInformation = argList.subList(4, argList.size());
        for (int i = 0; i < rawRingInformation.size(); i++) {
            String hostOrPort = rawRingInformation.get(i);
            if (i % 2 == 0) {
                hostsInRing.add(hostOrPort);
            } else {
                portsInRing.add(Integer.parseInt(hostOrPort));
            }
        }
        Node node = Node.createNode(hostsInRing, portsInRing, host, port);


        ElectionHandler electionHandler = new ElectionHandler(node);
        Thread electionHandlerThread = new Thread(electionHandler);
        electionHandlerThread.start();

        TimeSynchronizationHandler timeSynchronizationHandler = new TimeSynchronizationHandler(node);
        Thread timeSynchronizationThread = new Thread(timeSynchronizationHandler);
        timeSynchronizationThread.start();

//        CoordinatorChecker coordinatorChecker = new CoordinatorChecker(node);
//        Thread coordinatorCheckerThread = new Thread(coordinatorChecker);
//        coordinatorCheckerThread.start();

        TimePoller timePoller = new TimePoller(node);
        Thread timePollerThread = new Thread(timePoller);
        timePollerThread.start();

        LockHandler lockHandler = new LockHandler(node);
        Thread lockHandlerThread = new Thread(lockHandler);
        lockHandlerThread.start();

        FileReader fileReader = new FileReader(node, fileServerHost, fileServerReadPort);
        Thread fileReaderThread = new Thread(fileReader);
        fileReaderThread.start();

        FileWriter fileWriter = new FileWriter(node, fileServerHost, fileServerWritePort);
        Thread fileWriterThread = new Thread(fileWriter);
        fileWriterThread.start();
    }
}
