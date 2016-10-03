package com.stephengoeddel.synchronization;

public class TimePoller implements Runnable {
    private Node node;


    public TimePoller(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            if (node.isCoordinator()) {
                node.pollOtherNodesForTimeAndNotifyOffsets();
            }

            try {
                Thread.sleep(30000);
            } catch (Exception e) {
                System.out.println("TimePoller stopped.");
                System.exit(1);
            }
        }
    }
}
