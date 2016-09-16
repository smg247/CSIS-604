package com.stephengoeddel.distributedSorting;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMISortingService extends Remote {
    void sort(List<Integer> numbers) throws RemoteException;
}
