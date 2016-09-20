package com.stephengoeddel.distributedSorting.sorters;


import java.util.List;

public interface Sorter extends Runnable {
    List<Integer> getNumbers();
}
