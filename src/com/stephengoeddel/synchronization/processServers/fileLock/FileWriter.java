package com.stephengoeddel.synchronization.processServers.fileLock;


import com.stephengoeddel.synchronization.enums.LockAction;
import com.stephengoeddel.synchronization.enums.LockType;
import com.stephengoeddel.synchronization.node.Node;

public class FileWriter implements Runnable {
    private Node node;


    public FileWriter(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(40000);
                if (!node.isHasWriteLock()) {
                    node.sendLockMessageForObtainOrRelinquish(LockType.write, LockAction.obtain);
                }
                Thread.sleep(10000);
                if (node.isHasWriteLock()) {
                    node.relinquishThisNodesLock(LockType.write);
                    node.sendLockMessageForObtainOrRelinquish(LockType.write, LockAction.relinquish);
                }
            } catch (Exception e) {
                System.out.println("FileWriter stopped.");
                System.exit(1);
            }
        }
    }
}