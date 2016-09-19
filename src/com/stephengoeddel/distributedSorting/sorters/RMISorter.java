package com.stephengoeddel.distributedSorting.sorters;


import com.stephengoeddel.distributedSorting.rmi.RMISortingService;

import java.rmi.Naming;
import java.util.List;

class RMISorter extends RemoteSorter {

    RMISorter(List<Integer> numbers, String serverAddress, int serverPort) {
        super(numbers, serverAddress, serverPort);
    }

    @Override
    public void run() {
        try {
            RMISortingService rmiSortingService = (RMISortingService) Naming.lookup("rmi://" + serverAddress + ":" + serverPort + "/" + RMISortingService.SERVICE_NAME);
            numbers = rmiSortingService.sort(numbers);
        } catch (Exception e) {
            System.out.println("Encountered exception while attempting to sort with RMI: " + e.getMessage());
        }
    }
}
