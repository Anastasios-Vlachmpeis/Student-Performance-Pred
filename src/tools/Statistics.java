package tools;

public class Statistics {

    public static double average(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty in order to calculate its mean");
        }

        double sum = 0;
        for (double e: array) {
            sum += e;
        }
        return sum / array.length;
    }
}
