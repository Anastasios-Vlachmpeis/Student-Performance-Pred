package GUI.old;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Map;
import java.util.TreeMap;

public class HistogramGenerator {

    public BarChart<String, Number> createChart(String xAxisData, String yAxisData, int xAxisFilterStart, int xAxisFilterEnd, int yAxisFilterStart, int yAxisFilterEnd) {

        // X-axis label
        CategoryAxis xAxis = new CategoryAxis();

        // Y-axis label
        NumberAxis yAxis = new NumberAxis();

        // Create chart, Label, Bars are bigger, no gaps between Bars
        BarChart<String, Number> histogram = new BarChart<>(xAxis,yAxis);
        histogram.setTitle(yAxisData + " " + xAxisData);
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(5);
        histogram.setBarGap(0);
        histogram.setAnimated(false);

        // Data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(yAxisData + " " + xAxisData);


        if (xAxisData.equals("Mean of Grades")) {

            xAxis.setLabel("Mean of Grades");
            yAxis.setLabel(yAxisData);

            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.setName(yAxisData + "Mean of Grades");

            Map<String, Integer> frequencies = new TreeMap<>();

            double binWidth = 0.5;

            if (yAxisData.equals("Number of Courses with")) {

                int start = Math.max(0, xAxisFilterStart);
                int end = Math.min(CurrentGradesModel.courseCount - 1, xAxisFilterEnd);

                for (int i = start; i <= end; i++) {

                    double mean = CurrentGradesModel.calcCourseMean(i);

                    if (mean < yAxisFilterStart || mean > yAxisFilterEnd) {
                        continue;
                    }

                    int binIndex = (int) Math.floor(mean / binWidth);
                    double left = binIndex * binWidth;
                    double right = left + binWidth;

                    String label = String.format("%.1f-%.1f", left, right);
                    frequencies.put(label, frequencies.getOrDefault(label, 0) + 1);
                }
            }

            else if (yAxisData.equals("Number of Students with")) {

                int start = Math.max(0, xAxisFilterStart);
                int end = Math.min(CurrentGradesModel.studentCount - 1, xAxisFilterEnd);

                for (int i = start; i <= end; i++) {

                    double mean;

                    try {
                        mean = CurrentGradesModel.calcStudentMean(i);
                    }   catch (NullPointerException e) {
                        continue;
                    }

                    if (mean < yAxisFilterStart || mean > yAxisFilterEnd) {
                        continue;
                    }

                    int binIndex = (int) Math.floor(mean / binWidth);
                    double left = binIndex * binWidth;
                    double right = left + binWidth;

                    String label = String.format("%.1f-%.1f", left, right);
                    frequencies.put(label, frequencies.getOrDefault(label, 0) + 1);
                }
            }

            System.out.println("Histogram frequencies size = " + frequencies.size());

            for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
                String binlabel = entry.getKey();
                int count = entry.getValue();

                series1.getData().add(new XYChart.Data<>(binlabel, count));
            }
        histogram.getData().add(series1);
        }

        return histogram;
    }
}