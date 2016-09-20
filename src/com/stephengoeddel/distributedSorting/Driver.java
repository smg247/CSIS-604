package com.stephengoeddel.distributedSorting;

import com.stephengoeddel.distributedSorting.messages.SortedNumberConsumer;
import com.stephengoeddel.distributedSorting.sorters.RemoteSorter;
import com.stephengoeddel.distributedSorting.sorters.SimpleSorter;
import com.stephengoeddel.distributedSorting.sorters.Sorter;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Driver {
    private static String UNSORTED_NUMBERS_FILENAME = "unsorted-numbers.txt";
    private static String SORTED_NUMBERS_FILENAME = "sorted-numbers.txt";
    public static String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        boolean generateFile = args[0] != null ? Boolean.valueOf(args[0]) : true;
        Procedure procedure = Procedure.valueOf(args[1]);
        final int amountOfNumbers = args[2] != null ? Integer.parseInt(args[2]) : 1000;
        final int numberOfThreadsOrPorts = args[3] != null ? Integer.parseInt(args[3]) : 3;

        List<Integer> serverPorts = new ArrayList<>();
        if (procedure == Procedure.threads || procedure == Procedure.rmi) {
            List<Object> argList = Arrays.asList(args);
            for (Object arg : argList.subList(4, argList.size())) {
                serverPorts.add(Integer.parseInt(arg.toString()));
            }
        } else if (procedure == Procedure.messages) {
            // All ports will be the same in the Message Queue
            for (int i = 0; i < numberOfThreadsOrPorts; i++) {
                serverPorts.add(Integer.parseInt(args[4]));
            }
        }

        if (procedure == Procedure.rmi && System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        if (generateFile) {
            createRandomNumbersFile(amountOfNumbers);
        }

        List<List<Integer>> subLists = createSubLists(amountOfNumbers, numberOfThreadsOrPorts);

        List<Integer> mergeResult = null;
        try {
            Map<Thread, Sorter> threads;
            if (procedure == Procedure.threads) {
                threads = sortByThreads(subLists);
            } else {
                threads = sortRemotely(subLists, serverPorts, procedure);
            }

            List<List<Integer>> sortedSubLists = new ArrayList<>();
            for (Thread thread : threads.keySet()) {
                thread.join();
                if (procedure != Procedure.messages) {
                    sortedSubLists.add(threads.get(thread).getNumbers());
                } else {
                    // A Pseudo multi-threaded approach to grabbing the sorted numbers back from the Queue
                    SortedNumberConsumer sortedNumberConsumer = new SortedNumberConsumer(serverPorts.get(0));
                    Thread sortedNumberConsumerThread = new Thread(sortedNumberConsumer);
                    sortedNumberConsumerThread.start();
                    sortedNumberConsumerThread.join();
                    sortedSubLists.add(sortedNumberConsumer.getNumbers());
                }
            }

            mergeResult = mergeSort(sortedSubLists);
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

    private static Map<Thread, Sorter> sortByThreads(List<List<Integer>> subLists) {
        Map<Thread, Sorter> threads = new HashMap<>();
        for (List<Integer> subList : subLists) {
            Sorter simpleSorter = new SimpleSorter(subList);
            Thread thread = new Thread(simpleSorter);
            thread.start();
            threads.put(thread, simpleSorter);
        }

        return threads;
    }

    private static Map<Thread, Sorter> sortRemotely(List<List<Integer>> subLists, List<Integer> serverPorts, Procedure procedure) throws IOException {
        if (subLists.size() == serverPorts.size()) {
            Map<Thread, Sorter> threads = new HashMap<>();
            for (int i = 0; i < subLists.size(); i++) {
                List<Integer> subList = subLists.get(i);
                int serverPort = serverPorts.get(i);
                Sorter sorter;
                if (procedure == Procedure.sockets) {
                    sorter = RemoteSorter.forSockets(subList, SERVER_ADDRESS, serverPort);
                } else if (procedure == Procedure.rmi) {
                    sorter = RemoteSorter.forRMI(subList, SERVER_ADDRESS, serverPort);
                } else if (procedure == Procedure.messages) {
                    sorter = RemoteSorter.forMessages(subList, SERVER_ADDRESS, serverPort);
                } else {
                    throw new IllegalArgumentException("Procedure: " + procedure.name() + " is not valid for sortingRemotely");
                }
                Thread thread = new Thread(sorter);
                thread.start();
                threads.put(thread, sorter);
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

    private static void populateSortedNumbersFromQueue(List<List<Integer>> sortedSubLists) {

    }
}
