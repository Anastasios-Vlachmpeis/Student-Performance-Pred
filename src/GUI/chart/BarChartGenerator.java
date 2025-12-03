package GUI.chart;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;

public class BarChartGenerator {

    public BarChart<String, Number> createChart(
            String xAxisData,
            String yAxisData,
            int xAxisFilterStart,
            int xAxisFilterEnd,
            double yFilterMin,
            double yFilterMax) {

        // create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisData);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData);

        // create the chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(yAxisData + " per " + xAxisData);

        XYChart.Series<String, Number> dataset = new XYChart.Series<>();
        dataset.setName(yAxisData);

        int[] studentIds = CurrentGradesModel.getAllStudentIds();

        // when x is Per Course
        if (xAxisData.equals("Per Course")) {

            //considering the x data between the filters
            for (int i = xAxisFilterStart; i <= xAxisFilterEnd - 1; i++) {
                String courseName = CurrentGradesModel.getCourseName(i);

                //different datasets for the chosen y axis
                Number value = switch (yAxisData) {
                    case "Number of NG" -> CurrentGradesModel.getCourseNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcCourseMean(i);
                    case "Mode of Grades" -> CurrentGradesModel.calcCourseMode(i);
                    case "Median of Grades" -> CurrentGradesModel.calcCourseMedian(i);
                    default -> 0;
                };

                //making the datasets so only the ones between y filters can get in
                if (value.doubleValue() >= yFilterMin && value.doubleValue() <= yFilterMax) {
                    dataset.getData().add(new XYChart.Data<>(courseName, value));
                }
            }
        }


        // when x is Per Student
        if (xAxisData.equals("Per Student")) {

            //considering the x data between the filters
            for (int i = xAxisFilterStart; i <= xAxisFilterEnd - 1; i++) {
                String studentName = "Student " + studentIds[i];

                //different datasets for the chosen y axis
                Number value = switch (yAxisData) {
                    case "Number of NG" -> CurrentGradesModel.getStudentNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcStudentMean(studentIds[i]);
                    case "Mode of Grades" -> CurrentGradesModel.calcStudentMode(studentIds[i]);
                    case "Median of Grades" -> CurrentGradesModel.calcStudentMedian(studentIds[i]);
                    default -> 0;
                };

                //making the datasets so only the ones between y filters can get in
                if (value.doubleValue() >= yFilterMin && value.doubleValue() <= yFilterMax) {
                    dataset.getData().add(new XYChart.Data<>(studentName, value));
                }
            }
        }

        barChart.getData().add(dataset);
        return barChart;
    }


}