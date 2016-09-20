package com.stephengoeddel.distributedSorting.sorters;


import java.util.List;

public abstract class RemoteSorter implements Sorter {
    List<Integer> numbers;
    final String serverAddress;
    final int serverPort;

    public static RemoteSorter forSockets(List<Integer> numbers, String serverAddress, int serverPort) {
        return new SocketSorter(numbers, serverAddress, serverPort);
    }

    public static RemoteSorter forRMI(List<Integer> numbers, String serverAddress, int serverPort) {
        return new RMISorter(numbers, serverAddress, serverPort);
    }

    public static RemoteSorter forMessages(List<Integer> numbers, String serverAddress, int serverPort) {
        return new MessageSorter(numbers, serverAddress, serverPort);
    }

    RemoteSorter(List<Integer> numbers, String serverAddress, int serverPort) {
        this.numbers = numbers;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public List<Integer> getNumbers() {
        return numbers;
    }
}
