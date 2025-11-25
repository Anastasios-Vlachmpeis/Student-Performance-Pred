package tools;

import java.util.*;


/**
 * Static helper class for performing descriptive statistics on double values datasets.
 * - mean
 * - median
 * - mode
 * - standard variation
 */
public class Statistics {

    /**
     * Helper function to convert list of doubles into arrays of doubles
     * so they are compatible with double[] argument methods
     */
    private static double[] convertDoubleListToArray(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static double mean(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its mean");
        }

        double sum = 0;
        for (double num : array) {
            sum += num;
        }
        return sum / array.length;
    }

    public static double mean(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its mean");
        }
        return mean(convertDoubleListToArray(list));
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
        } else {
            median = safeArray[safeArray.length / 2];
        }

        return median;
    }

    public static double median(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its mean");
        }
        return median(convertDoubleListToArray(list));
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
        for (Map.Entry<Double, Integer> frequency : frequencies.entrySet()) {
            if (frequency.getValue() > highestCount) {
                highestCount = frequency.getValue();
                mode = frequency.getKey();
            }
        }

        return mode;
    }

    public static double mode(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its mean");
        }
        return mode(convertDoubleListToArray(list));
    }

    public static double sampleVariance(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its sample variance");
        }
        double mean = mean(array);
        double sumSquaredDifference = 0;
        for (double num : array) {
            sumSquaredDifference += Math.pow(num - mean, 2);
        }
        return sumSquaredDifference / (array.length - 1);
    }

    public static double sampleVariance(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its sample variance");
        }
        return sampleVariance(convertDoubleListToArray(list));
    }

    public static double populationVariance(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its population variance");
        }
        double mean = mean(array);
        double sumSquaredDifference = 0;
        for (double num : array) {
            sumSquaredDifference += Math.pow(num - mean, 2);
        }
        return sumSquaredDifference / array.length;
    }

    public static double populationVariance(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its population variance");
        }
        return populationVariance(convertDoubleListToArray(list));
    }

    public static double sampleStandardDeviation(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its sample standard variation");
        }
        return Math.sqrt(sampleVariance(array));

    }

    public static double sampleStandardDeviation(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its sample standard deviation");
        }
        return sampleStandardDeviation(convertDoubleListToArray(list));

    }

    public static double populationStandardDeviation(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its population standard variation");
        }
        return Math.sqrt(populationVariance(array));
    }

    public static double populationStandardDeviation(List<Double> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty in order to calculate its population standard deviation");
        }
        return populationStandardDeviation(convertDoubleListToArray(list));

    }
}
