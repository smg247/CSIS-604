package com.stephengoeddel.distributedSorting.sorters;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

class SocketSorter extends RemoteSorter {

    SocketSorter(List<Integer> numbers, String serverAddress, int serverPort) {
        super(numbers, serverAddress, serverPort);
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            for (Integer integer : numbers) {
                printWriter.println(integer);
            }
            printWriter.println("."); // The period allows the SortingServer to know we are out of input for it

            numbers.clear();
            String line;
            while ((line = inputReader.readLine()) != null) {
                numbers.add(Integer.parseInt(line));
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Encountered exception while attempting to sort with sockets: " + e.getMessage());
            System.exit(1);
        }
    }
}
