package GUI.chart;

import javafx.scene.chart.ScatterChart;
import javafx.scene.layout.BorderPane;
import tools.ChartDataUtils;

public class ScatterPlotGenerator {

    /**
     * This method creates the scatter plot chart for course correlation analysis
     * Returns a BorderPane with the chart and custom legend
     */
    public BorderPane createChart(double xAxisFilterStart, double xAxisFilterEnd,
        double yAxisFilterStart, double yAxisFilterEnd, String filterFeature, String filterValue,
        String xCourse, String yCourse, boolean showYEqualsX) {
        
        // Filter students based on the selected feature filter
        int[] filteredStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        
        // Create course correlation chart
        ScatterChart<Number, Number> chart = ChartDataUtils.createCourseCorrelationChart(xCourse, yCourse, xAxisFilterStart, xAxisFilterEnd,
                                           yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, showYEqualsX);
        
        // Wrap in BorderPane to allow custom legend at bottom
        BorderPane container = new BorderPane();
        container.setCenter(chart);
        
        return container;
    }
    
}

