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
        int objectPort = Integer.parseInt(args[0]);
        int registryPort = Integer.parseInt(args[1]);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            try {
                registry = LocateRegistry.createRegistry(registryPort);
                System.out.println(SERVICE_NAME + " created registry");
            } catch (Exception ignore) {
                // The registry must already exist
                registry = LocateRegistry.getRegistry(registryPort);
                System.out.println(SERVICE_NAME + " found existing registry");
            }

            RMISortingService service = (RMISortingService) exportObject(new RMISortingServer(), objectPort);
            registry.rebind(SERVICE_NAME, service);
            System.out.println(SERVICE_NAME + " bound sort method");
            while(true) {
                // Keep the server from exiting on its own
            }
        } catch (Exception exception) {
            System.out.println("Exception while creating and binding the RMISortingServer: " + exception.getMessage());
        }
    }
}
