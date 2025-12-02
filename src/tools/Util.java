package tools;

public class Util {
    public static int cap(int min, int max, int value) {
       return Math.min(Math.max(min, value), max);
    }
}
