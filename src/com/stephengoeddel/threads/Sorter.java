package com.stephengoeddel.threads;


import java.util.Collections;
import java.util.List;

class Sorter implements Runnable {
    private List<Integer> numbers;

    Sorter(List<Integer> numbers) {
        this.numbers = numbers;
    }

    @Override
    public void run() {
        Collections.sort(numbers);
    }
}
