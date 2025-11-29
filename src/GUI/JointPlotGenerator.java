package GUI;

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

    public BorderPane createChart(String xAxisData, String yAxisData, double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String selectedCourse, String selectedFeature, boolean showActualGrades, String filterFeature, String filterValue, boolean courseCorrelationMode, String xCourse, String yCourse, boolean showYEqualsX) {
        
        // Filter students based on feature filter
        int[] filteredStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        
        // Create main container
        BorderPane mainPane = new BorderPane();
        
        // Handle Course Correlation Mode
        if (courseCorrelationMode && xCourse != null && yCourse != null) {
            ScatterChart<Number, Number> scatterChart = ChartDataUtils.createCourseCorrelationChart(xCourse, yCourse, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, showYEqualsX);
            scatterChart.setPrefSize(800, 600);
            
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
            centerBox.getChildren().add(scatterChart);
            VBox rightBox = new VBox();
            rightBox.getChildren().add(yHistogram);
            centerBox.getChildren().add(rightBox);
            mainPane.setCenter(centerBox);
            
            return mainPane;
        }
        
        // Create scatter chart
        ScatterChart<Number, Number> scatterChart = createScatterChart(xAxisData, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature, showActualGrades, filteredStudentIds);
        
        // Set scatter chart preferred size (will be used for histogram sizing)
        scatterChart.setPrefSize(800, 600);
        
        // Create histograms with proper sizing
        BarChart<String, Number> xHistogram = createXHistogram(xAxisData, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature, filteredStudentIds);
        BarChart<Number, String> yHistogram = createYHistogram(xAxisData, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature, filteredStudentIds);
        
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
        centerBox.getChildren().add(scatterChart);
        VBox rightBox = new VBox();
        rightBox.getChildren().add(yHistogram);
        centerBox.getChildren().add(rightBox);
        mainPane.setCenter(centerBox);
        
        return mainPane;
    }
    
    private ScatterChart<Number, Number> createScatterChart(String xAxisData, String yAxisData, double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String selectedCourse, String selectedFeature, boolean showActualGrades, int[] filteredStudentIds) {
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisData);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData);
        
        // Create the scatter chart
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        String title = showActualGrades ? "Predicted vs Actual Grades vs " + xAxisData : yAxisData + " vs " + xAxisData;
        scatterChart.setTitle(title);
        
        // Generate data based on axis selections
        if (xAxisData.equals("Per Student")) {
            if (showActualGrades && yAxisData.equals("Predicted Grade")) {
                XYChart.Series<Number, Number> predictedSeries = new XYChart.Series<>();
                predictedSeries.setName("Predicted Grades");
                XYChart.Series<Number, Number> actualSeries = new XYChart.Series<>();
                actualSeries.setName("Actual Grades");
                
                ChartDataUtils.generatePredictedVsActualData(predictedSeries, actualSeries, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature, filteredStudentIds);
                
                scatterChart.getData().add(predictedSeries);
                scatterChart.getData().add(actualSeries);
                
                ChartDataUtils.addTooltipsToSeries(predictedSeries, xAxisData, "Predicted Grade", selectedCourse);
                ChartDataUtils.addTooltipsToSeries(actualSeries, xAxisData, "Actual Grade", selectedCourse);
            } else {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName("Data Points");
                ChartDataUtils.generateStudentData(series, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature, filteredStudentIds);
                scatterChart.getData().add(series);
                ChartDataUtils.addTooltipsToSeries(series, xAxisData, yAxisData, selectedCourse);
            }
        } else if (xAxisData.equals("Per Course")) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data Points");
            ChartDataUtils.generateCourseData(series, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd);
            scatterChart.getData().add(series);
            ChartDataUtils.addTooltipsToSeries(series, xAxisData, yAxisData, null);
        }
        
        return scatterChart;
    }
    
    private BarChart<String, Number> createXHistogram(String xAxisData, String yAxisData, double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        // Create the axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisData);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency");
        
        // Create the histogram
        BarChart<String, Number> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle("X-Axis Distribution");
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);
        histogram.setAnimated(false);
        
        // Count frequencies for each bin
        Map<Double, Integer> frequencies = new TreeMap<>();
        
        if (xAxisData.equals("Per Student")) {
            for (int i = 0; i < filteredStudentIds.length; i++) {
                double xValue = i;
                if (xValue >= xAxisFilterStart && xValue <= xAxisFilterEnd) {
                    double bin = Math.floor(xValue / 10) * 10;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
            }
        } else if (xAxisData.equals("Per Course")) {
            for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
                double xValue = i;
                if (xValue >= xAxisFilterStart && xValue <= xAxisFilterEnd) {
                    double bin = i;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
            }
        }
        
        // Add data to histogram
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey().intValue()), entry.getValue()));
        }
        histogram.getData().add(series);
        
        return histogram;
    }
    
    private BarChart<Number, String> createYHistogram(String xAxisData, String yAxisData, double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        // Swap axes for horizontal bars (perpendicular to Y-axis)
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Frequency");
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel(yAxisData);
        
        // Create the histogram
        BarChart<Number, String> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle("Y-Axis Distribution");
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);
        histogram.setAnimated(false);
        
        // Count frequencies for each bin
        Map<Double, Integer> frequencies = new TreeMap<>();
        
        if (xAxisData.equals("Per Student")) {
            for (int i = 0; i < filteredStudentIds.length; i++) {
                int studentId = filteredStudentIds[i];
                double yValue = ChartDataUtils.getStudentYValueForFiltered(studentId, yAxisData, selectedCourse, selectedFeature);
                if (yValue >= yAxisFilterStart && yValue <= yAxisFilterEnd) {
                    double bin = Math.floor(yValue * 10) / 10.0;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
            }
        } else if (xAxisData.equals("Per Course")) {
            for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
                double yValue = ChartDataUtils.getCourseYValue(i, yAxisData);
                if (yValue >= yAxisFilterStart && yValue <= yAxisFilterEnd) {
                    double bin = Math.floor(yValue * 10) / 10.0;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
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
