package com.stephengoeddel.synchronization;


import com.stephengoeddel.synchronization.fileServers.FileReadSocket;
import com.stephengoeddel.synchronization.fileServers.FileWriteSocket;

public class FileActionDriver {
    public static final String FILE_NAME = "database.txt";
    public static void main(String[] args) {
        int readPort = Integer.parseInt(args[0]);
        int writePort = readPort + 1;

        FileReadSocket fileReadSocket = new FileReadSocket(readPort);
        Thread fileReadSocketThread = new Thread(fileReadSocket);
        fileReadSocketThread.start();

        FileWriteSocket fileWriteSocket = new FileWriteSocket(writePort);
        Thread fileWriteSocketThread = new Thread(fileWriteSocket);
        fileWriteSocketThread.start();
    }

    public enum FileAccessType {
        write,
        read;
    }
}
