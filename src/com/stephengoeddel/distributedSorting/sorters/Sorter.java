package com.stephengoeddel.distributedSorting.sorters;


import java.util.List;

public interface Sorter extends Runnable {
    public List<Integer> getNumbers();
}
