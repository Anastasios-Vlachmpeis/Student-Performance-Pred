package GUI.chart;

import datamodels.*;
import GUI.style.UIStyling;
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
        
        // Find max frequency for normalization
        int maxFrequency = frequencies.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        // Store frequency data for styling
        Map<String, Integer> frequencyMap = new TreeMap<>();
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            String binLabel = String.format("%.1f", entry.getKey());
            frequencyMap.put(binLabel, entry.getValue());
            series.getData().add(new XYChart.Data<>(binLabel, entry.getValue()));
        }
        histogram.getData().add(series);
        
        // Style bars after chart is rendered
        histogram.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<String, Number> data = series.getData().get(i);
                    String binLabel = data.getXValue();
                    int frequency = frequencyMap.getOrDefault(binLabel, 1);
                    
                    // Calculate color using centralized method
                    String colorStyle = UIStyling.calculateBarColorStyle(frequency, maxFrequency);
                    
                    // Apply color styling when node is created
                    data.nodeProperty().addListener((obs2, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle(colorStyle);
                        }
                    });
                    
                    // Also try to style immediately if node already exists
                    if (data.getNode() != null) {
                        data.getNode().setStyle(colorStyle);
                    }
                }
            }
        });
        return histogram;
    }
    
    private BarChart<Number, String> createYHistogramForCourseCorrelation(String yCourse, double yFilterStart, double yFilterEnd, int[] filteredStudentIds) {
        // Swap axes for horizontal bars (perpendicular to Y-axis)
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Frequency");
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel(yCourse);
        
        //Histogram
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
        
        //Frequencies
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
        
        //Max frequency
        int maxFrequency = frequencies.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        //Frequency data
        Map<String, Integer> frequencyMap = new TreeMap<>();
        //I used natural ascending order to match scatter plot Y-axis
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            String binLabel = String.format("%.1f", entry.getKey());
            frequencyMap.put(binLabel, entry.getValue());
            series.getData().add(new XYChart.Data<>(entry.getValue(), binLabel));
        }
        histogram.getData().add(series);
        
        //Bars
        histogram.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<Number, String> data = series.getData().get(i);
                    String binLabel = data.getYValue();
                    int frequency = frequencyMap.getOrDefault(binLabel, 1);
                    
                    String colorStyle = UIStyling.calculateBarColorStyle(frequency, maxFrequency);
                    data.nodeProperty().addListener((obs2, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle(colorStyle);
                        }
                    });
                    
                    // Also try to style immediately if node already exists
                    if (data.getNode() != null) {
                        data.getNode().setStyle(colorStyle);
                    }
                }
            }
        });
        
        return histogram;
    }
    
}
