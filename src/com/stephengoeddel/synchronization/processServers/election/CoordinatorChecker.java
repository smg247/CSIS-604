package com.stephengoeddel.synchronization.processServers.election;


import com.stephengoeddel.synchronization.node.Node;

public class CoordinatorChecker implements Runnable {
    private Node node;


    public CoordinatorChecker(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while(true) {
            // Let's attempt to see if the coordinator is up and running
            if (!node.isCoordinatorActive()) {
                System.out.println("Coordinator is inactive.");
                node.sendElectionMessage();
            }
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                System.out.println("CoordinatorChecker stopped.");
                System.exit(1);
            }
        }
    }
}
