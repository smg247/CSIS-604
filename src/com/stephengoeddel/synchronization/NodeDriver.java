package com.stephengoeddel.synchronization;

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

        CoordinatorChecker coordinatorChecker = new CoordinatorChecker(node);
        Thread coordinatorCheckerThread = new Thread(coordinatorChecker);
        coordinatorCheckerThread.start();

        TimePoller timePoller = new TimePoller(node);
        Thread timePollerThread = new Thread(timePoller);
        timePollerThread.start();
    }
}
