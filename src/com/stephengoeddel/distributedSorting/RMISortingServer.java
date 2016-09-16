package com.stephengoeddel.distributedSorting;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;

public class RMISortingServer extends UnicastRemoteObject implements RMISortingService {

    public RMISortingServer() throws RemoteException {
        super();
    }

    @Override
    public void sort(List<Integer> numbers) throws RemoteException {
        Collections.sort(numbers);
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            RMISortingService service = new RMISortingServer();
            Naming.bind("RMISortingService", service);
        } catch (Exception exception) {
            System.out.println("Exception while creating and binding the RMISortingServer: " + exception.getMessage());
        }
    }
}
