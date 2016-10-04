package com.stephengoeddel.synchronization.enums;

public enum MessageType {
    election("ELECTION"),
    coordinator("COORDINATOR"),
    timePoll("TIME_POLL"),
    timeResponse("TIME_RESPONSE"),
    timeOffset("TIME_OFFSET"),
    obtainReadLock("OBTAIN_READ_LOCK", LockType.read, LockAction.obtain),
    readLockGranted("READ_LOCK_GRANTED", LockType.read, LockAction.grant),
    readLockRelinquished("READ_LOCK_RELINQUISHED", LockType.read, LockAction.relinquish),
    obtainWriteLock("OBTAIN_WRITE_LOCK", LockType.write, LockAction.obtain),
    writeLockGranted("WRITE_LOCK_GRANTED", LockType.write, LockAction.grant),
    writeLockRelinquished("WRITE_LOCK_RELINQUISHED", LockType.write, LockAction.relinquish),
    ping("PING");

    public static MessageType fromHeader(String header) {
        for (MessageType messageType : values()) {
            if (header.equals(messageType.getHeader())) {
                return messageType;
            }
        }

        return null;
    }

    public static MessageType forLockTypeAndLockAction(LockType lockType, LockAction lockAction) {
        for (MessageType messageType : values()) {
            if (lockType.equals(messageType.getLockType()) && lockAction.equals(messageType.getLockAction())) {
                return messageType;
            }
        }

        return null;
    }


    private String header;
    private LockType lockType;
    private LockAction lockAction;

    MessageType(String header) {
        this(header, null, null);
    }

    MessageType(String header, LockType lockType, LockAction lockAction) {
        this.header = header;
        this.lockType = lockType;
        this.lockAction = lockAction;
    }

    public String getHeader() {
        return header;
    }

    public LockType getLockType() {
        return lockType;
    }

    public LockAction getLockAction() {
        return lockAction;
    }
}
