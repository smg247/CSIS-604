package com.stephengoeddel.distributedSorting;

import java.util.ArrayList;
import java.util.List;

public class StringUtily {

    public static String createStringFromNumbers(List<Integer> numbers) {
        StringBuilder numbersBuilder = new StringBuilder();
        for (Integer number : numbers) {
            numbersBuilder.append(number).append(",");
        }
        numbersBuilder.delete(numbersBuilder.length() - 1, numbersBuilder.length());
        return numbersBuilder.toString();
    }

    public static List<Integer> createNumbersFromString(String numberString) {
        String[] rawNumbers = numberString.split(",");
        List<Integer> numbers = new ArrayList<>();
        for (String rawNumber : rawNumbers) {
            numbers.add(Integer.parseInt(rawNumber));
        }

        return numbers;
    }
}
