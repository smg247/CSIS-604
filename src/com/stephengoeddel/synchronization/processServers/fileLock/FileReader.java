package com.stephengoeddel.synchronization.processServers.fileLock;


import com.stephengoeddel.synchronization.enums.LockAction;
import com.stephengoeddel.synchronization.enums.LockType;
import com.stephengoeddel.synchronization.node.Node;

public class FileReader implements Runnable {
    private Node node;


    public FileReader(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20000);
                if (!node.isHasReadLock()) {
                    node.sendLockMessageForObtainOrRelinquish(LockType.read, LockAction.obtain);
                }
                Thread.sleep(5000);
                if (node.isHasReadLock()) {
                    node.relinquishThisNodesLock(LockType.read);
                    node.sendLockMessageForObtainOrRelinquish(LockType.read, LockAction.relinquish);
                }
            } catch (Exception e) {
                System.out.println("FileReader stopped.");
                System.exit(1);
            }
        }
    }
}