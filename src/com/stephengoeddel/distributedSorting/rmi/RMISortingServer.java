package com.stephengoeddel.distributedSorting.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;

public class RMISortingServer extends UnicastRemoteObject implements RMISortingService {

    public RMISortingServer() throws RemoteException {
        super();
    }

    @Override
    public List<Integer> sort(List<Integer> numbers) throws RemoteException {
        Collections.sort(numbers);
        System.out.println("Sorted " + numbers.size() + " numbers");
        return numbers;
    }

    public static void main(String[] args) {
        int serverSuffix = Integer.parseInt(args[0]);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println(SERVICE_NAME + serverSuffix + " created registry");
            } catch (Exception ignore) {
                // The registry must already exist
                registry = LocateRegistry.getRegistry(1099);
                System.out.println(SERVICE_NAME + serverSuffix + " found existing registry");
            }

            RMISortingService service = new RMISortingServer();
            registry.bind(SERVICE_NAME + serverSuffix, service);
            System.out.println(SERVICE_NAME + serverSuffix + " bound sort method");
        } catch (Exception exception) {
            System.out.println("Exception while creating and binding the RMISortingServer: " + exception.getMessage());
        }
    }
}
