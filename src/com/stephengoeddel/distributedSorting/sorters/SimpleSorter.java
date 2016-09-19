package com.stephengoeddel.distributedSorting.sorters;


import java.util.Collections;
import java.util.List;

public class SimpleSorter implements Sorter {
    private List<Integer> numbers;

    public SimpleSorter(List<Integer> numbers) {
        this.numbers = numbers;
    }

    @Override
    public void run() {
        Collections.sort(numbers);
    }

    public List<Integer> getNumbers() {
        return numbers;
    }
}
