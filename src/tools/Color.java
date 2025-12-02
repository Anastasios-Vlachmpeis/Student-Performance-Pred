package tools;

public class Color {
    public static String heatmapRGB(double minimum, double maximum, double value) {
        var ratio = 2 * (value-minimum) / (maximum - minimum);
        var b = (int) (Math.max(0, 255*(1 - ratio)));
        var r = (int) (Math.max(0, 255*(ratio - 1)));
        var g = 255 - b - r;
        return "rgb(" + r + "," + g + "," + b + ")";
    }

    public static String heatmapRGBRedBoosted(double minimum, double maximum, double value) {
        var ratio = 2 * (value-minimum) / (maximum - minimum);
        var b = (int) (Math.max(0, 255*(1 - ratio)));
        var r = (int) (Math.max(0, 255*(ratio - 1) + 15));
        var g = 255 - b - r;
        return "rgb(" + r + "," + g + "," + b + ")";
    }
}
