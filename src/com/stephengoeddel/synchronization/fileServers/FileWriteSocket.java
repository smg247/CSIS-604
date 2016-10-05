package com.stephengoeddel.synchronization.fileServers;


import com.stephengoeddel.synchronization.FileActionDriver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileWriteSocket implements Runnable {
    private int serverPort;


    public FileWriteSocket(int serverPort) {
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
                    if (FileActionDriver.FileAccessType.write.equals(fileAccessType)) {
                        FileWriter fileWriter = null;
                        BufferedWriter bufferedWriter = null;
                        PrintWriter printWriter = null;
                        try {
                            fileWriter = new FileWriter(FileActionDriver.FILE_NAME, true);
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

                        System.out.println("Wrote lines to " + FileActionDriver.FILE_NAME + ".");
                    } else {
                        System.out.println("FileWriteSocket received malformed message.");
                    }
                } finally {
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("FileWriteSocket encountered a problem: " + e.getMessage());
        }
    }
}
