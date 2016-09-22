package com.stephengoeddel.distributedSorting.sorters;


import com.stephengoeddel.distributedSorting.Driver;
import com.stephengoeddel.distributedSorting.rmi.RMISortingService;

import java.rmi.Naming;
import java.rmi.Remote;
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
            //RMISortingService rmiSortingService = (RMISortingService) Naming.lookup("rmi://" + serverAddress + ":" + serverPort + "/" + RMISortingService.SERVICE_NAME);
            Registry registry = LocateRegistry.getRegistry(Driver.SERVER_ADDRESS);
            RMISortingService rmiSortingService = (RMISortingService) registry.lookup(RMISortingService.SERVICE_NAME + serverPort);
            numbers = rmiSortingService.sort(numbers);
        } catch (Exception e) {
            System.out.println("Encountered exception while attempting to sort with RMI: " + e.getMessage());
        }
    }
}