package com.stephengoeddel.synchronization.node;

import com.stephengoeddel.synchronization.enums.LockType;

class LockAttempt implements Comparable<LockAttempt> {
    private NodeRepresentation nodeRepresentation;
    private long time;
    private LockType lockType;

    LockAttempt(NodeRepresentation nodeRepresentation, long time, LockType lockType) {
        this.nodeRepresentation = nodeRepresentation;
        this.time = time;
        this.lockType = lockType;
    }

    @Override
    public int compareTo(LockAttempt o) {
        return (int) (time - o.time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LockAttempt that = (LockAttempt) o;

        if (nodeRepresentation != null ? !nodeRepresentation.equals(that.nodeRepresentation) : that.nodeRepresentation != null) {
            return false;
        }
        return lockType == that.lockType;

    }

    @Override
    public int hashCode() {
        int result = nodeRepresentation != null ? nodeRepresentation.hashCode() : 0;
        result = 31 * result + (lockType != null ? lockType.hashCode() : 0);
        return result;
    }

    NodeRepresentation getNodeRepresentation() {
        return nodeRepresentation;
    }

    long getTime() {
        return time;
    }

    LockType getLockType() {
        return lockType;
    }
}
