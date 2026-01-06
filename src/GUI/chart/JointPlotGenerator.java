package GUI.chart;

import datamodels.*;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Map;
import java.util.TreeMap;
import tools.ChartDataUtils;

public class JointPlotGenerator {

    public BorderPane createChart(double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String filterFeature, String filterValue, String xCourse, String yCourse, boolean showYEqualsX) {
        
        // Filter students based on feature filter
        int[] filteredStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        
        // Create main container
        BorderPane mainPane = new BorderPane();
        
        // Create course correlation scatter chart
        ScatterChart<Number, Number> scatterChart = ChartDataUtils.createCourseCorrelationChart(xCourse, yCourse, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, showYEqualsX);
        scatterChart.setPrefSize(800, 600);
        
        // Wrap scatter chart in BorderPane to allow legend at bottom
        BorderPane scatterContainer = new BorderPane();
        scatterContainer.setCenter(scatterChart);
        
        BarChart<String, Number> xHistogram = createXHistogramForCourseCorrelation(xCourse, xAxisFilterStart, xAxisFilterEnd, filteredStudentIds);
        BarChart<Number, String> yHistogram = createYHistogramForCourseCorrelation(yCourse, yAxisFilterStart, yAxisFilterEnd, filteredStudentIds);
        
        // Set X histogram size: max width = scatter width, max height = scatter height / 5
        xHistogram.setMaxWidth(scatterChart.getPrefWidth());
        xHistogram.setPrefWidth(scatterChart.getPrefWidth());
        xHistogram.setMaxHeight(scatterChart.getPrefHeight() / 5.0);
        xHistogram.setPrefHeight(scatterChart.getPrefHeight() / 5.0);
        
        // Set Y histogram size: max height = scatter height, max width = scatter width / 5
        yHistogram.setMaxHeight(scatterChart.getPrefHeight());
        yHistogram.setPrefHeight(scatterChart.getPrefHeight());
        yHistogram.setMaxWidth(scatterChart.getPrefWidth() / 5.0);
        yHistogram.setPrefWidth(scatterChart.getPrefWidth() / 5.0);
        
        // Layout: histogram on top, scatter in center, histogram on right
        VBox topBox = new VBox();
        topBox.getChildren().add(xHistogram);
        mainPane.setTop(topBox);
        
        HBox centerBox = new HBox();
        centerBox.getChildren().add(scatterContainer);
        VBox rightBox = new VBox();
        rightBox.getChildren().add(yHistogram);
        centerBox.getChildren().add(rightBox);
        mainPane.setCenter(centerBox);
        
        return mainPane;
    }
    
    private BarChart<String, Number> createXHistogramForCourseCorrelation(String xCourse, double xFilterStart, double xFilterEnd, int[] filteredStudentIds) {
        // Create the axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xCourse);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency");
        
        // Create the histogram
        BarChart<String, Number> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle("X-Axis Distribution");
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);
        histogram.setAnimated(false);
        
        int xCourseId = ChartDataUtils.findCourseId(xCourse);
        if (xCourseId == -1) {
            return histogram;
        }
        
        // Count frequencies for each grade bin
        Map<Double, Integer> frequencies = new TreeMap<>();
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, xCourseId);
                if (grade != -1 && grade >= xFilterStart && grade <= xFilterEnd) {
                    double bin = Math.floor(grade * 10) / 10.0;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            series.getData().add(new XYChart.Data<>(String.format("%.1f", entry.getKey()), entry.getValue()));
        }
        histogram.getData().add(series);
        
        return histogram;
    }
    
    private BarChart<Number, String> createYHistogramForCourseCorrelation(String yCourse, double yFilterStart, double yFilterEnd, int[] filteredStudentIds) {
        // Swap axes for horizontal bars (perpendicular to Y-axis)
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Frequency");
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel(yCourse);
        
        // Create the histogram
        BarChart<Number, String> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle("Y-Axis Distribution");
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);
        histogram.setAnimated(false);
        
        int yCourseId = ChartDataUtils.findCourseId(yCourse);
        if (yCourseId == -1) {
            return histogram;
        }
        
        // Count frequencies for each grade bin
        Map<Double, Integer> frequencies = new TreeMap<>();
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, yCourseId);
                if (grade != -1 && grade >= yFilterStart && grade <= yFilterEnd) {
                    double bin = Math.floor(grade * 10) / 10.0;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        // Use natural ascending order (lowest at top, highest at bottom) to match scatter plot Y-axis
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getValue(), String.format("%.1f", entry.getKey())));
        }
        histogram.getData().add(series);
        
        return histogram;
    }
    
}
