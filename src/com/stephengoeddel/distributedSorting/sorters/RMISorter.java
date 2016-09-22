package com.stephengoeddel.distributedSorting.sorters;


import com.stephengoeddel.distributedSorting.rmi.RMISortingService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

class RMISorter extends RemoteSorter {

    RMISorter(List<Integer> numbers, String serverAddress, int serverPort) {
        super(numbers, serverAddress, serverPort);
    }

    @Override
    public void run() {
        try {
            Registry registry = LocateRegistry.getRegistry(40000);
            RMISortingService rmiSortingService = (RMISortingService) registry.lookup(RMISortingService.SERVICE_NAME + serverPort);
            numbers = rmiSortingService.sort(numbers);
        } catch (Exception e) {
            System.out.println("Encountered exception while attempting to sort with RMI: " + e.getMessage());
        }
    }
}