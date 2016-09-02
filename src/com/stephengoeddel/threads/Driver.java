package com.stephengoeddel.threads;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Driver {
    private static int AMOUNT_OF_NUMBERS = 1000;
    private static int NUMBER_OF_THREADS = 3;
    private static String UNSORTED_NUMBERS_FILENAME = "unsorted-numbers.txt";
    private static String SORTED_NUMBERS_FILENAME = "sorted-numbers.txt";

    public static void main(String[] args) {
        boolean generateFile = Boolean.valueOf(args[0]);
        if (generateFile) {
            try {
                // I am not going to write random numbers into a file by hand...
                PrintWriter printWriter = new PrintWriter(UNSORTED_NUMBERS_FILENAME, "UTF-8");

                for (int i = 0; i < AMOUNT_OF_NUMBERS; i++) {
                    printWriter.println(ThreadLocalRandom.current().nextInt(0, AMOUNT_OF_NUMBERS * 10));
                }

                printWriter.close();
            } catch (Exception e) {
                System.out.println("Oops...exception during number generation: " + e.getMessage());
            }
        }

        List<Thread> threads = new ArrayList<>();
        List<List<Integer>> subLists = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(UNSORTED_NUMBERS_FILENAME);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            List<Integer> numbers = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                numbers.add(Integer.parseInt(line));
            }

            int sizeOfSublist = AMOUNT_OF_NUMBERS/NUMBER_OF_THREADS;
            int remainder = AMOUNT_OF_NUMBERS % NUMBER_OF_THREADS;
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                int fromIndex = sizeOfSublist * i;
                List<Integer> subList = new ArrayList<>(numbers.subList(fromIndex, fromIndex + sizeOfSublist));

                if (i == NUMBER_OF_THREADS - 1 && remainder != 0) { // If AMOUNT_OF_NUMBERS/NUMBER_OF_THREADS has a remainder we have to add the extra's to the last list
                    List<Integer> remainingNumbers = numbers.subList(AMOUNT_OF_NUMBERS - remainder, AMOUNT_OF_NUMBERS);
                    subList.addAll(remainingNumbers);
                }

                Thread thread = new Thread(new Sorter(subList));
                thread.start();
                threads.add(thread);
                subLists.add(subList);
            }
        } catch (Exception e) {
            System.out.println("Oops...exception during number reading and thread creation: " + e.getMessage());
        }

        List<Integer> mergeResult = null;
        try {
            for (Thread thread : threads) {
                thread.join();
            }

            for (int i = 0; i < subLists.size(); i++) {
                if (i == 0) {
                    mergeResult = new ArrayList<>(subLists.get(i));
                } else {
                    mergeResult = merge(mergeResult, subLists.get(i));
                }
            }

        } catch (Exception e) {
            System.out.println("Oops..exception during thread merging: " + e.getMessage());
        }
        
        if (mergeResult != null) {
            try {
                // I am not going to write random numbers into a file by hand...
                PrintWriter printWriter = new PrintWriter(SORTED_NUMBERS_FILENAME, "UTF-8");
                for (Integer integer : mergeResult) {
                    printWriter.println(integer);
                }

                printWriter.close();
            } catch (Exception e) {
                System.out.println("Oops...exception during sorted number file creation: " + e.getMessage());
            }
        } else {
            System.out.println("Something went wrong, we don't seem to have any result...");
        }

    }

    private static List<Integer> merge(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();

        int currentLeftIndex = 0;
        int currentRightIndex = 0;
        while (!hasGoneThroughList(currentLeftIndex, left) && !hasGoneThroughList(currentRightIndex, right)) {
            if (left.get(currentLeftIndex) <= right.get(currentRightIndex)) {
                result.add(left.get(currentLeftIndex));
                currentLeftIndex++;
            } else {
                result.add(right.get(currentRightIndex));
                currentRightIndex++;
            }
        }

        while (!hasGoneThroughList(currentLeftIndex, left)) {
            result.add(left.get(currentLeftIndex));
            currentLeftIndex++;
        }

        while (!hasGoneThroughList(currentRightIndex, right)) {
            result.add(right.get(currentRightIndex));
            currentRightIndex++;
        }

        return result;
    }

    private static boolean hasGoneThroughList(int currentIndex, List<Integer> list) {
        return currentIndex >= list.size();
    }
}
