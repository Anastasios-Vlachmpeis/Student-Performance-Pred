package GUI.old;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;

public class BarChartGeneratorold {

    public BarChart<String, Number> createChart(String xAxisData, String yAxisData, int xAxisFilterStart, int xAxisFilterEnd, int yAxisFilterStart, int yAxisFilterEnd) {

        // Create the x-axis
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisData);

        // Create the y-axis
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData);

        // Create the bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(yAxisData + " " + xAxisData);
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(0);
        barChart.setBarGap(1);
        barChart.setAnimated(false);

        XYChart.Series<String, Number> dataset = new XYChart.Series<>();
        dataset.setName(yAxisData + " " + xAxisData);

        // Create the dataset according to users choice

        //If chosen X axis is per Course
        if (xAxisData.equals("Per Course")) {

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
                    case "Number of NG" -> CurrentGradesModel.getCourseNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcCourseMean(i);
                    case "Mode of Grades" -> CurrentGradesModel.calcCourseMode(i);
                    case "Median of Grades" -> CurrentGradesModel.calcCourseMedian(i);
                    default -> 0;
                };

                if(value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                    dataset.getData().add(new XYChart.Data<>(courseName, value)); }}

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

                if(value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                    dataset.getData().add(new XYChart.Data<>(studentNumber, value)); }}

        }
        barChart.getData().add(dataset);
        return barChart;
    }
}