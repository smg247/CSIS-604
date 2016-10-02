package com.stephengoeddel.synchronization;

class NodeRepresentation implements Comparable<NodeRepresentation> {
    private static String PORT_DELIMITER = ":";
    private static String POSITION_DELIMITER = "-";
    private String host;
    private int port;
    private int positionInRing;


    public static NodeRepresentation fromName(String name) {
        int portDelimiter = name.indexOf(PORT_DELIMITER);
        int positionDelimiter = name.indexOf(POSITION_DELIMITER);
        String host = name.substring(0, portDelimiter);
        int port = Integer.parseInt(name.substring(portDelimiter + 1, positionDelimiter));
        int positionInRing = Integer.parseInt(name.substring(positionDelimiter + 1, name.length()));
        return new NodeRepresentation(host, port, positionInRing);
    }


    NodeRepresentation(String host, int port, int positionInRing) {
        this.host = host;
        this.port = port;
        this.positionInRing = positionInRing;
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    String getName() {
        return host + PORT_DELIMITER + port + POSITION_DELIMITER + positionInRing;
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

        if (port != that.port) {
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
        result = 31 * result + port;
        result = 31 * result + positionInRing;
        return result;
    }
}