package com.stephengoeddel.distributedSorting.rmi;

import java.rmi.Naming;
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
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry = LocateRegistry.createRegistry(8000);
            RMISortingService service = new RMISortingServer();
            registry.bind(SERVICE_NAME, service);
        } catch (Exception exception) {
            System.out.println("Exception while creating and binding the RMISortingServer: " + exception.getMessage());
        }
    }
}
