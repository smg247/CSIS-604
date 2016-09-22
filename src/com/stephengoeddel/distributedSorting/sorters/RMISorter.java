package com.stephengoeddel.distributedSorting.sorters;


import com.stephengoeddel.distributedSorting.Driver;
import com.stephengoeddel.distributedSorting.rmi.RMISortingService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

class RMISorter extends RemoteSorter {
    private final int registryPort;

    RMISorter(List<Integer> numbers, String serverAddress, int serverPort, int registryPort) {
        super(numbers, serverAddress, serverPort);
        this.registryPort = registryPort;
    }

    @Override
    public void run() {
        try {
            Registry registry = LocateRegistry.getRegistry(Driver.SERVER_ADDRESS, registryPort);
            RMISortingService rmiSortingService = (RMISortingService) registry.lookup(RMISortingService.SERVICE_NAME);
            numbers = rmiSortingService.sort(numbers);
        } catch (Exception e) {
            System.out.println("Encountered exception while attempting to sort with RMI: " + e.getMessage());
        }
    }
}