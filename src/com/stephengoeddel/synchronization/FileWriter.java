package com.stephengoeddel.synchronization;


class FileWriter implements Runnable {
    private Node node;


    FileWriter(Node node) {
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