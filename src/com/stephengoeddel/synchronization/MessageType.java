package com.stephengoeddel.synchronization;

public enum MessageType {
    election("ELECTION"),
    coordinator("COORDINATOR"),
    timePoll("TIME_POLL"),
    timeResponse("TIME_RESPONSE"),
    timeOffset("TIME_OFFSET"),
    ping("PING");

    public static MessageType fromHeader(String header) {
        for (MessageType messageType : values()) {
            if (header.equals(messageType.getHeader())) {
                return messageType;
            }
        }

        return null;
    }


    private String header;

    MessageType(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}