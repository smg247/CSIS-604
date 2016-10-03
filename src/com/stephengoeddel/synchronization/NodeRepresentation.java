package com.stephengoeddel.synchronization;

class NodeRepresentation implements Comparable<NodeRepresentation> {
    private static String PORT_DELIMITER = ":";
    private static String POSITION_DELIMITER = "-";
    private String host;
    private int electionPort;
    private int timePollingPort;
    private int positionInRing;


    static NodeRepresentation fromName(String name) {
        int portDelimiter = name.indexOf(PORT_DELIMITER);
        int positionDelimiter = name.indexOf(POSITION_DELIMITER);
        String host = name.substring(0, portDelimiter);
        int electionPort = Integer.parseInt(name.substring(portDelimiter + 1, positionDelimiter));
        int positionInRing = Integer.parseInt(name.substring(positionDelimiter + 1, name.length()));
        return new NodeRepresentation(host, electionPort, positionInRing);
    }


    NodeRepresentation(String host, int electionPort, int positionInRing) {
        this.host = host;
        this.electionPort = electionPort;
        this.positionInRing = positionInRing;

        timePollingPort = electionPort + 1;
    }

    String getHost() {
        return host;
    }

    int getElectionPort() {
        return electionPort;
    }

    int getTimePollingPort() {
        return timePollingPort;
    }

    String getName() {
        return host + PORT_DELIMITER + electionPort + POSITION_DELIMITER + positionInRing;
    }

    @Override
    public int compareTo(NodeRepresentation o) {
        return positionInRing - o.positionInRing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeRepresentation that = (NodeRepresentation) o;

        if (electionPort != that.electionPort) {
            return false;
        }
        if (positionInRing != that.positionInRing) {
            return false;
        }
        return host.equals(that.host);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + electionPort;
        result = 31 * result + positionInRing;
        return result;
    }
}