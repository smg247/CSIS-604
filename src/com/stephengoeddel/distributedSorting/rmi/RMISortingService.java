package com.stephengoeddel.distributedSorting.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMISortingService extends Remote {
    String SERVICE_NAME = "RMISortingService";
    List<Integer> sort(List<Integer> numbers) throws RemoteException;
}
