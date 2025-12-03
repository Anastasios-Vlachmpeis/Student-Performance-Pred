package GUI.chart;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tools.ChartDataUtils;

import java.util.Map;
import java.util.TreeMap;

public class HistogramGenerator {

    /**
     * Converts a global student ID to a local StudentId.
     */
    private Integer getStudentIndex(int studentId) {
        int[] allIds = CurrentGradesModel.getAllStudentIds();
        for (int i = 0; i < allIds.length; i++) {
            if (allIds[i] == studentId) return i;
        }
        return null; // Should not happen unless dataset has inconsistencies
    }

    /**
     * Determines the bin width based on the selected X-axis metric.
     * Helps produce readable histograms without overly dense bars.
     */
    private double determineBinWidth(String xAxisData) {
        return switch (xAxisData) {
            case "Mean of Grades" -> 0.2;
            case "Median of Grades" -> 0.5;
            case "Number of NG" -> 6.0;
            case "Number of Passing Students" -> 30.0;
            case "Number of graded Courses" -> 3.0;
            case "Mode of Grades", "Number of failed Courses" -> 1.0;
            default -> 0.5;  // Safe fallback
        };
    }

    /**
     * Creates a histogram BarChart based on the chosen X and Y axis metrics.
     * All logic is performed here.
     */
    public BarChart<String, Number> createChart(
            String xAxisData, String yAxisData,
            double xAxisFilterStart, double xAxisFilterEnd,
            double yAxisFilterStart, double yAxisFilterEnd,
            String filterFeature, String filterValue
    ) {

        // Filter out students based on user-chosen feature settings
        int[] filterStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);

        // Setup axes for the histogram
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        // Creates Histogram, with title and spacing between bars
        BarChart<String, Number> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle(yAxisData + " with the " + xAxisData);
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(5);
        histogram.setBarGap(0);
        histogram.setAnimated(false);

        // Sets label for X and Y axis
        xAxis.setLabel(xAxisData);
        yAxis.setLabel(yAxisData);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName(yAxisData + " with the " + xAxisData);

        // TreeMap keeps bins sorted numerically by left-bound
        TreeMap<Double, Integer> frequencies = new TreeMap<>();

        // Adds the private determineBinWidth to a double for the following code
        double binWidth = determineBinWidth(xAxisData);

        // Y-axis 1: "Number of Courses"; with the different metrics for the X-axis
        if ("Number of Courses".equals(yAxisData)) {

            for (int courseIndex = 0; courseIndex < CurrentGradesModel.courseCount; courseIndex++) {

                double value;

                // Select X-axis metric for the Courses
                try {
                    switch (xAxisData) {
                        case "Mean of Grades" -> value = CurrentGradesModel.calcCourseMean(courseIndex);
                        case "Median of Grades" -> value = CurrentGradesModel.calcCourseMedian(courseIndex);
                        case "Mode of Grades" -> value = CurrentGradesModel.calcCourseMode(courseIndex);
                        case "Number of NG" -> value = CurrentGradesModel.getCourseNG(courseIndex);
                        case "Number of Passing Students" -> value = ChartDataUtils.getPassingStudents(courseIndex);
                        default -> {
                            continue; // Ignore unsupported combinations
                        }
                    }
                } catch (Exception e){
                        continue; // Skip problematic entries
                    }

                // Apply X-axis filter
                if (value < xAxisFilterStart || value > xAxisFilterEnd) {
                    continue;
                }

                // Compute bin index and left bound
                int binIndex = (int) Math.floor(value / binWidth);
                double left = binIndex * binWidth;

                // Count frequency for this bin
                frequencies.put(left, frequencies.getOrDefault(left, 0) + 1);
            }
        }

        // Y-axis 2: "Number of Students"; with the different metrics for the X-axis
        else if ("Number of Students".equals(yAxisData)) {

            for (int studentId : filterStudentIds) {

                Integer idx = getStudentIndex(studentId);
                if (idx == null) continue; // Should not occur if IDs are valid

                double value;

                // Select X-axis metric for the Students
                try {
                    switch (xAxisData) {
                        case "Mean of Grades" -> value = CurrentGradesModel.calcStudentMean(studentId);
                        case "Median of Grades" -> value = CurrentGradesModel.calcStudentMedian(studentId);
                        case "Mode of Grades" -> value = CurrentGradesModel.calcStudentMode(studentId);
                        case "Number of NG" -> value = CurrentGradesModel.getStudentNG(idx);
                        case "Number of graded Courses" -> {
                            int count = 0;
                            for (double g : CurrentGradesModel.getAllGradesStudent(studentId)) {
                                if (g != -1) count++;
                            }
                            value = count;
                        }
                        case "Number of failed Courses" -> value = CurrentGradesModel.getFailedCourses(idx);
                        default -> {
                            continue; // Unsupported X-axis metric
                        }
                    }
                } catch (Exception e) {
                    continue; // Skip invalid data entries
                }

                // Apply X-axis filtering
                if (value < xAxisFilterStart || value > xAxisFilterEnd) {
                    continue;
                }

                // Compute bin left boundary
                int binIndex = (int) Math.floor(value / binWidth);
                double left = binIndex * binWidth;

                // Count occurrences in this bin
                frequencies.put(left, frequencies.getOrDefault(left, 0) + 1);
            }
        }

        // Creates the bars for the chart, from the sorted bins
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {

            double left = entry.getKey();
            double right = left + binWidth;
            int count = entry.getValue();

            // Apply Y-axis frequency filter
            if (count < yAxisFilterStart || count > yAxisFilterEnd) {
                continue;
            }

            // Create readable label
            String label = (binWidth == 1)
                    ? String.format("%d-%d", (int) left, (int) right)
                    : String.format("%.1f-%.1f", left, right);

            // Add bar to histogram
            series1.getData().add(new XYChart.Data<>(label, count));
        }

        histogram.getData().add(series1);
        return histogram;
    }
}