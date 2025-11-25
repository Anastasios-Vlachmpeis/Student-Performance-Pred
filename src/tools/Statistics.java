package tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Statistics {

    public static double average(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its mean");
        }

        double sum = 0;
        for (double num: array) {
            sum += num;
        }
        return sum / array.length;
    }

    public static double median(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its median");
        }
        // make a safe copy for sorting
        // primitive array, therefore shallow copying is enough
        double[] safeArray = array.clone();
        Arrays.sort(safeArray);

        // find middle element
        double median = 0;
        if (safeArray.length % 2 == 0) {
            median = (safeArray[safeArray.length / 2] + safeArray[safeArray.length / 2 + 1]) / 2;
        }
        else {
            median = safeArray[safeArray.length / 2];
        }

        return median;
    }

    public static double mode(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its median");
        }

        // cound each element how many times they appeared
        Map<Double, Integer> frequencies = new HashMap<>();
        for (double num : array) {
            frequencies.put(num, frequencies.getOrDefault(num, 0) + 1);
        }

        // find first with the highest frequency
        int highestCount = 0;
        double mode = array[0];
        for (Map.Entry<Double, Integer> frequency: frequencies.entrySet()) {
           if (frequency.getValue() > highestCount) {
               highestCount = frequency.getValue();
               mode = frequency.getKey();
           }
        }

        return mode;
    }

}
