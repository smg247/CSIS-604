package com.stephengoeddel.threads;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RemoteSorter implements Runnable {
    private List<Integer> numbers;
    private final String serverAddress;
    private final int serverPort;

    public RemoteSorter(List<Integer> numbers, String serverAddress, int serverPort) {
        this.numbers = numbers;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
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
            System.out.println("Encountered exception while attempting to sort remotely: " + e.getMessage());
            System.exit(1);
        }
    }
}
