package tools;

public class Color {
    /**
     * After much experimenting and unsatisfying attempt at crafting a home made heat map color gradient function
     * we have found an already existing one that worked exceptionally: https://stackoverflow.com/a/20792531
     * The idea is to evenly distribute the colors after normalization.
     */
    public static String heatmapRGBinCSS(double minimum, double maximum, double value) {
        double ratio = 2 * (value-minimum) / (maximum - minimum);
        int b = (int)(Math.max(0, 255*(1 - ratio)));
        int r = (int)(Math.max(0, 255*(ratio - 1)));
        int g = 255 - b - r;
        return String.format("rgb(%s, %s, %s)" , r, g, b);
    }

    public static String heatmapHSLinCSS(double minimum, double maximum, double value) {
        double normalized = (value-minimum) / (maximum - minimum);
        return "hsl("+ normalized * 100  +"%, 100%, 50%)";  // lightness cannot be 100 otherwise it gets too bright
    }
}
