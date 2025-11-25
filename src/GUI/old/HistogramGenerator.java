package GUI.old;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;

import java.util.Map;
import java.util.TreeMap;

public class HistogramGenerator {

    public BarChart<String, Number> createChart(String xAxisData, String yAxisData, int xAxisFilterStart, int xAxisFilterEnd, int yAxisFilterStart, int yAxisFilterEnd) {

        //Count how many courses have each NG value
        Map<Integer, Integer> frequencies = new TreeMap<>();

        for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
            int ng = CurrentGradesModel.getCourseNG(i);
            frequencies.put(ng, frequencies.getOrDefault(ng, 0) + 1);
        }

        // X-axis label
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisData);

        // Y-axis label
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData);

        // Create chart, Label, Bars are bigger, no gaps between Bars
        BarChart<String, Number> histogram = new BarChart<>(xAxis,yAxis);
        histogram.setTitle(yAxisData + " " + xAxisData);
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(0);
        histogram.setBarGap(0);
        histogram.setAnimated(false);

        // Data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(yAxisData + " " + xAxisData);

        //If chosen X axis is per Course
        if (xAxisData.equals("")) {

            //Alert if the inputs are not in range
            if (xAxisFilterStart < 0 || xAxisFilterEnd > CurrentGradesModel.courseCount || xAxisFilterStart >= xAxisFilterEnd) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Range");
                alert.setHeaderText("Filter range is out of bounds!");
                alert.setContentText("Valid range: 0 to " + CurrentGradesModel.courseCount);
                alert.show();
            }

            for (int i = xAxisFilterStart; i <= xAxisFilterEnd; i++) {
                String courseName = CurrentGradesModel.getCourseName(i);
                Number value = switch (yAxisData) {
                    case "Number of Courses" -> CurrentGradesModel.getCourseNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcCourseMean(i);
                    case "Mode of Grades" -> CurrentGradesModel.calcCourseMode(i);
                    case "Median of Grades" -> CurrentGradesModel.calcCourseMedian(i);
                    default -> 0;
                };

                if(value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                    series.getData().add(new XYChart.Data<>(courseName, value)); }}

        }

        //If chosen X axis is Per Student
        if (xAxisData.equals("Per Student")) {


            //Alert if the inputs are not in range
            if (xAxisFilterStart < 0 || xAxisFilterEnd > CurrentGradesModel.studentCount || xAxisFilterStart >= xAxisFilterEnd) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Range");
                alert.setHeaderText("Filter range is out of bounds!");
                alert.setContentText("Valid range: 0 to " + CurrentGradesModel.studentCount);
                alert.show();
            }

            for (int i = xAxisFilterStart; i <= xAxisFilterEnd; i++) {
                String studentNumber = "Student " + i;

                Number value = switch (yAxisData) {
                    case "Number of NG" -> CurrentGradesModel.getStudentNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcStudentMean(i);
                    case "Mode of Grades" -> CurrentGradesModel.calcStudentMode(i);
                    case "Median of Grades" -> CurrentGradesModel.calcStudentMedian(i);
                    default -> 0;
                };

                if (value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                    series.getData().add(new XYChart.Data<>(studentNumber, value));
                }
            }
        }

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