package com.stephengoeddel.distributedSorting;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortingServer {

    public static void main(String[] args) throws IOException {
        int serverPort = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(serverPort);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Received Connection");
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    List<Integer> numbers = new ArrayList<>();
                    String line;
                    while ((line = inputReader.readLine()) != null && !".".equals(line)) {
                        numbers.add(Integer.parseInt(line));
                    }
                    System.out.println("Received " + numbers.size() + " numbers");

                    Collections.sort(numbers);

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    for (Integer number : numbers) {
                        printWriter.println(number);
                    }

                    System.out.println("Output " + numbers.size() + " numbers");
                } finally {
                    System.out.println("Closing socket");
                    socket.close();
                }
            }
        } finally {
            System.out.println("Closing server socket");
            serverSocket.close();
        }
    }
}
