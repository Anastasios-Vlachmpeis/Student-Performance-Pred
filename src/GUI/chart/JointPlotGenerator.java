package GUI.chart;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tools.ChartDataUtils;

public class JointPlotGenerator {

    public BorderPane createChart(double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String filterFeature, String filterValue, String xCourse, String yCourse, boolean showYEqualsX) {
        
        // Filter students based on feature filter
        int[] filteredStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        
        // Main container
        BorderPane mainPane = new BorderPane();
        
        // Create course correlation scatterplot
        ScatterPlotGenerator scatterGenerator = new ScatterPlotGenerator();
        ScatterChart<Number, Number> scatterChart = scatterGenerator.createCourseCorrelationChart(xCourse, yCourse, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, showYEqualsX);
        scatterChart.setPrefSize(800, 600);
        
        // Wrap scatterplot in a BorderPane so that the legend can go to the bottom
        BorderPane scatterContainer = new BorderPane();
        scatterContainer.setCenter(scatterChart);
        
        // Histograms for top and right
        HistogramGenerator histogramGenerator = new HistogramGenerator();
        BarChart<String, Number> xHistogram = (BarChart<String, Number>) histogramGenerator.createCourseGradeHistogram(
            xCourse, xAxisFilterStart, xAxisFilterEnd, filteredStudentIds, 0.1, false);
        BarChart<Number, String> yHistogram = (BarChart<Number, String>) histogramGenerator.createCourseGradeHistogram(
            yCourse, yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, 0.1, true);
        
        // X histogram
        // width = scatter width, height = scatter height / 5
        xHistogram.setMaxWidth(scatterChart.getPrefWidth());
        xHistogram.setPrefWidth(scatterChart.getPrefWidth());
        xHistogram.setMaxHeight(scatterChart.getPrefHeight() / 5.0);
        xHistogram.setPrefHeight(scatterChart.getPrefHeight() / 5.0);
        
        // Y histogram
        // height = scatter height, width = scatter width / 5
        yHistogram.setMaxHeight(scatterChart.getPrefHeight());
        yHistogram.setPrefHeight(scatterChart.getPrefHeight());
        yHistogram.setMaxWidth(scatterChart.getPrefWidth() / 5.0);
        yHistogram.setPrefWidth(scatterChart.getPrefWidth() / 5.0);
        
        // The layout
        // A histogram on top, another histogram on the right and the scatterplot in the center,
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
    
}
