package com.stephengoeddel.synchronization;

public class FileServer {
    private static final String FILE_NAME = "database.txt";
    public static void main(String[] args) {
        try {
//            PrintWriter printWriter = new PrintWriter(FILE_NAME, "UTF-8");
        } catch (Exception e) {
            System.out.println("FileServer encountered a problem: " + e.getMessage());
        }
    }
}
