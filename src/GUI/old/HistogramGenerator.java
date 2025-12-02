package GUI.old;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Map;
import java.util.TreeMap;

public class HistogramGenerator {

    public BarChart<String, Number> createChart() {

        //Count how many courses have each NG value
        Map<Integer, Integer> frequencies = new TreeMap<>();

        for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
            int ng = CurrentGradesModel.getCourseNG(i);
            frequencies.put(ng, frequencies.getOrDefault(ng, 0) + 1);
        }

        // X-axis label
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Amount of NGs");

        // Y-axis label
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Courses");

        // Create chart, Label, Bars are bigger, no gaps between Bars
        BarChart<String, Number> histogram = new BarChart<>(xAxis,yAxis);
        histogram.setTitle("Histogram of number of Courses with the same amount of NGs");
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);
        histogram.setAnimated(false);

        // Data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
            int ngValue = entry.getKey();
            int count = entry.getValue();
            series.getData().add(new XYChart.Data<>(
                    String.valueOf(ngValue),
                    count
            ));
        }
        histogram.getData().add(series);
        return histogram;
    }
}