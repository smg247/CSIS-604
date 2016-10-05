package com.stephengoeddel.synchronization.fileServers;


import com.stephengoeddel.synchronization.FileActionDriver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileReadSocket implements Runnable {
    private int serverPort;


    public FileReadSocket(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    FileActionDriver.FileAccessType fileAccessType = FileActionDriver.FileAccessType.valueOf(inputReader.readLine());
                    if (FileActionDriver.FileAccessType.read.equals(fileAccessType)) {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader fileReader = new BufferedReader(new FileReader(FileActionDriver.FILE_NAME));
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
                        System.out.println("Transmitted the lines of " + FileActionDriver.FILE_NAME + ".");
                    } else {
                        System.out.println("FileReadSocket received malformed message.");
                    }
                } finally {
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("FileReadSocket encountered a problem: " + e.getMessage());
        }
    }
}
