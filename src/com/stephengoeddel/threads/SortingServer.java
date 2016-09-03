package com.stephengoeddel.threads;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SortingServer {

    public static void main(String[] args) throws IOException {
        int serverPort = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(serverPort);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    List<Integer> numbers = new ArrayList<>();
                    String line;
                    while ((line = inputReader.readLine()) != null && !".".equals(line)) {
                        numbers.add(Integer.parseInt(line));
                    }

                    new Sorter(numbers).run();

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    for (Integer number : numbers) {
                        printWriter.println(number);
                    }
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
