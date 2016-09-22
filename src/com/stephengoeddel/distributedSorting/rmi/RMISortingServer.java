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
        String serverAddress = args[0];
        int serverSuffix = Integer.parseInt(args[1]);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            try {
                LocateRegistry.createRegistry(1099);
            } catch (Exception ignore) {
                // The registry must be created already, ignore
            }

            Registry registry = LocateRegistry.getRegistry(serverAddress);
            System.out.println(SERVICE_NAME + serverSuffix + " found registry");
            RMISortingService service = new RMISortingServer();
            registry.bind(SERVICE_NAME + serverSuffix, service);
            System.out.println(SERVICE_NAME + serverSuffix + " bound sort method");
        } catch (Exception exception) {
            System.out.println("Exception while creating and binding the RMISortingServer: " + exception.getMessage());
        }
    }
}
