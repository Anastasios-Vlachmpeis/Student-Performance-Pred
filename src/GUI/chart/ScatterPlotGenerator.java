package GUI.chart;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import tools.ChartDataUtils;

public class ScatterPlotGenerator {

    public ScatterChart<Number, Number> createChart(String xAxisData, String yAxisData, double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String selectedCourse, String selectedFeature, boolean showActualGrades, String filterFeature, String filterValue, boolean courseCorrelationMode, String xCourse, String yCourse, boolean showYEqualsX) {
        
        // Filter students based on feature filter
        int[] filteredStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        
        // Handle Course Correlation Mode
        if (courseCorrelationMode && xCourse != null && yCourse != null) {
            return ChartDataUtils.createCourseCorrelationChart(xCourse, yCourse, xAxisFilterStart, xAxisFilterEnd,
                                               yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, showYEqualsX);
        }
        
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
                // Create two series: one for predicted, one for actual
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
    
}

