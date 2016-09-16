package com.stephengoeddel.distributedSorting;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Driver {
    private static String UNSORTED_NUMBERS_FILENAME = "unsorted-numbers.txt";
    private static String SORTED_NUMBERS_FILENAME = "sorted-numbers.txt";
    private static String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        boolean generateFile = args[0] != null ? Boolean.valueOf(args[0]) : true;
        Procedure procedure = Procedure.valueOf(args[1]);
        final int amountOfNumbers = args[2] != null ? Integer.parseInt(args[2]) : 1000;
        final int numberOfThreadsOrPorts = args[3] != null ? Integer.parseInt(args[3]) : 3;

        List<Integer> serverPorts = new ArrayList<>();
        if (procedure == Procedure.sockets) {
            List<Object> argList = Arrays.asList(args);
            for (Object arg : argList.subList(4, argList.size())) {
                serverPorts.add(Integer.parseInt(arg.toString()));
            }
        }

        if (generateFile) {
            createRandomNumbersFile(amountOfNumbers);
        }

        List<List<Integer>> subLists = createSubLists(amountOfNumbers, numberOfThreadsOrPorts);

        List<Integer> mergeResult = null;
        try {
            List<Thread> threads;
            if (procedure == Procedure.threads) {
                threads = sortByThreads(subLists);
            } else {
                threads = sortBySockets(subLists, serverPorts);
            }

            for (Thread thread : threads) {
                thread.join();
            }

            mergeResult = mergeSort(subLists);
        } catch (InterruptedException e) {
            System.out.println("Error while thread merging: " + e.getMessage());
        }

        if (mergeResult != null) {
            outputSortedNumbersToFile(mergeResult);
        } else {
            System.out.println("Something went wrong, we don't seem to have any result...");
        }

    }

    private static List<List<Integer>> createSubLists(int amountOfNumbers, int numberOfThreads) {
        List<List<Integer>> subLists = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(UNSORTED_NUMBERS_FILENAME);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            List<Integer> numbers = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                numbers.add(Integer.parseInt(line));
            }

            int sizeOfSublist = amountOfNumbers / numberOfThreads;
            int remainder = amountOfNumbers % numberOfThreads;
            for (int i = 0; i < numberOfThreads; i++) {
                int fromIndex = sizeOfSublist * i;
                List<Integer> subList = new ArrayList<>(numbers.subList(fromIndex, fromIndex + sizeOfSublist));

                if (i == numberOfThreads - 1 && remainder != 0) { // If amountOfNumbers/numberOfThreads has a remainder we have to add the extra's to the last list
                    List<Integer> remainingNumbers = numbers.subList(amountOfNumbers - remainder, amountOfNumbers);
                    subList.addAll(remainingNumbers);
                }
                subLists.add(subList);
            }
        } catch (Exception e) {
            System.out.println("Oops...exception during number reading and thread creation: " + e.getMessage());
        }

        return subLists;
    }

    private static List<Thread> sortByThreads(List<List<Integer>> subLists) {
        List<Thread> threads = new ArrayList<>();
        for (List<Integer> subList : subLists) {
            Thread thread = new Thread(new SimpleSorter(subList));
            thread.start();
            threads.add(thread);
        }

        return threads;
    }

    private static List<Thread> sortBySockets(List<List<Integer>> subLists, List<Integer> serverPorts) throws IOException {
        if (subLists.size() == serverPorts.size()) {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < subLists.size(); i++) {
                List<Integer> subList = subLists.get(i);
                int serverPort = serverPorts.get(i);
                Thread thread = new Thread(new RemoteSorter(subList, SERVER_ADDRESS, serverPort));
                thread.start();
                threads.add(thread);
            }
            return threads;
        } else {
            throw new IllegalArgumentException("Number of subLists does not match the number of servers");
        }
    }

    private static List<Integer> mergeSort(List<List<Integer>> subLists) {
        List<Integer> mergeResult = null;
        for (int i = 0; i < subLists.size(); i++) {
            if (i == 0) {
                mergeResult = new ArrayList<>(subLists.get(i));
            } else {
                mergeResult = merge(mergeResult, subLists.get(i));
            }
        }

        return mergeResult;
    }

    private static void outputSortedNumbersToFile(List<Integer> mergeResult) {
        try {
            PrintWriter printWriter = new PrintWriter(SORTED_NUMBERS_FILENAME, "UTF-8");
            for (Integer integer : mergeResult) {
                printWriter.println(integer);
            }

            printWriter.close();
        } catch (Exception e) {
            System.out.println("Oops...exception during sorted number file creation: " + e.getMessage());
        }
    }

    private static void createRandomNumbersFile(int amountOfNumbers) {
        try {
            PrintWriter printWriter = new PrintWriter(UNSORTED_NUMBERS_FILENAME, "UTF-8");

            for (int i = 0; i < amountOfNumbers; i++) {
                printWriter.println(ThreadLocalRandom.current().nextInt(0, amountOfNumbers * 10));
            }

            printWriter.close();
        } catch (Exception e) {
            System.out.println("Oops...exception during number generation: " + e.getMessage());
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
