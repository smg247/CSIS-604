package com.stephengoeddel.synchronization;


class CoordinatorCaller implements Runnable {
    private Node node;


    CoordinatorCaller(Node node) {
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
                System.out.println("CoordinatorCaller stopped.");
                System.exit(1);
            }
        }
    }
}
