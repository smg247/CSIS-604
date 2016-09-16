package com.stephengoeddel.distributedSorting;


import java.util.Collections;
import java.util.List;

class SimpleSorter implements Runnable {
    private List<Integer> numbers;

    SimpleSorter(List<Integer> numbers) {
        this.numbers = numbers;
    }

    @Override
    public void run() {
        Collections.sort(numbers);
    }
}
