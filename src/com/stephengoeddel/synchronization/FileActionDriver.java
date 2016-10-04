package com.stephengoeddel.synchronization;

public class FileActionDriver {
    private static final String FILE_NAME = "database.txt";
    public static void main(String[] args) {
        try {
//            PrintWriter printWriter = new PrintWriter(FILE_NAME, "UTF-8");
        } catch (Exception e) {
            System.out.println("FileActionDriver encountered a problem: " + e.getMessage());
        }
    }
}
