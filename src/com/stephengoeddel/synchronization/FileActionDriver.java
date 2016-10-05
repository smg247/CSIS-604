package com.stephengoeddel.synchronization;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileActionDriver {
    private static final String FILE_NAME = "database.txt";
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    FileAccessType fileAccessType = FileAccessType.valueOf(inputReader.readLine());
                    if (FileAccessType.read.equals(fileAccessType)) {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader fileReader = new BufferedReader(new FileReader(FILE_NAME));
                        List<String> linesInFile = new ArrayList<>();
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            linesInFile.add(line);
                        }

                        for (String lineInFile : linesInFile) {
                            printWriter.println(lineInFile);
                        }
                        printWriter.println(".");

                        fileReader.close();
                        System.out.println("Transmitted the lines of " + FILE_NAME + ".");
                        socket.close();
                    } else if (FileAccessType.write.equals(fileAccessType)) {
                        FileWriter fileWriter = null;
                        BufferedWriter bufferedWriter = null ;
                        PrintWriter printWriter = null;
                        try {
                            fileWriter = new FileWriter(FILE_NAME, true);
                            bufferedWriter = new BufferedWriter(fileWriter);
                            printWriter = new PrintWriter(bufferedWriter);

                            String line;
                            while ((line = inputReader.readLine()) != null && !line.equals(".")) {
                                printWriter.println(line);
                            }
                        } finally {
                            printWriter.close();
                            bufferedWriter.close();
                            fileWriter.close();
                        }

                        System.out.println("Wrote lines to " + FILE_NAME + ".");
                        socket.close();
                    } else {
                        System.out.println("Received malformed message.");
                    }

                } finally {
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("FileActionDriver encountered a problem: " + e.getMessage());
        }
    }

    public enum FileAccessType {
        write,
        read;
    }
}
