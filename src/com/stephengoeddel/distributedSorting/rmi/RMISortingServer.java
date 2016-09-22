package com.stephengoeddel.distributedSorting.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;

import static java.rmi.server.UnicastRemoteObject.exportObject;

public class RMISortingServer implements RMISortingService {
    static Registry registry;

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
            try {
                registry = LocateRegistry.createRegistry(40000);
                System.out.println(SERVICE_NAME + serverSuffix + " created registry");
            } catch (Exception ignore) {
                // The registry must already exist
                registry = LocateRegistry.getRegistry(40000);
                System.out.println(SERVICE_NAME + serverSuffix + " found existing registry");
            }

            RMISortingService service = (RMISortingService) exportObject(new RMISortingServer(), 0);
            registry.rebind(SERVICE_NAME + serverSuffix, service);
            System.out.println(SERVICE_NAME + serverSuffix + " bound sort method");
            while(true) {
                // Keep the server from exiting on its own
            }
        } catch (Exception exception) {
            System.out.println("Exception while creating and binding the RMISortingServer: " + exception.getMessage());
        }
    }
}
