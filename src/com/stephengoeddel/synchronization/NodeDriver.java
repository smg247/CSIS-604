package com.stephengoeddel.synchronization;

import com.stephengoeddel.synchronization.node.Node;
import com.stephengoeddel.synchronization.processServers.election.ElectionServer;
import com.stephengoeddel.synchronization.processServers.fileLock.FileReader;
import com.stephengoeddel.synchronization.processServers.fileLock.FileWriter;
import com.stephengoeddel.synchronization.processServers.fileLock.LockServer;
import com.stephengoeddel.synchronization.processServers.time.TimePoller;
import com.stephengoeddel.synchronization.processServers.time.TimeSynchronizationServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeDriver {

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


        ElectionServer electionServer = new ElectionServer(node);
        Thread electionServerThread = new Thread(electionServer);
        electionServerThread.start();

        TimeSynchronizationServer timeSynchronizationServer = new TimeSynchronizationServer(node);
        Thread timeSynchronizationThread = new Thread(timeSynchronizationServer);
        timeSynchronizationThread.start();

//        CoordinatorChecker coordinatorChecker = new CoordinatorChecker(node);
//        Thread coordinatorCheckerThread = new Thread(coordinatorChecker);
//        coordinatorCheckerThread.start();

        TimePoller timePoller = new TimePoller(node);
        Thread timePollerThread = new Thread(timePoller);
        timePollerThread.start();

        LockServer lockServer = new LockServer(node);
        Thread lockServerThread = new Thread(lockServer);
        lockServerThread.start();

        FileReader fileReader = new FileReader(node);
        Thread fileReaderThread = new Thread(fileReader);
        fileReaderThread.start();

        FileWriter fileWriter = new FileWriter(node);
        Thread fileWriterThread = new Thread(fileWriter);
        fileWriterThread.start();
    }
}
