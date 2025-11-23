package GUI;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;


public class BarChartGenerator {

    public BarChart<String, Number> createChart() {

        //Create the x-axis
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Course Names");

        //Create the y-axis
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of NG");

        //Create the Bar Chart with the predefined axes
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Number of NG's per Course");
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(0);
        barChart.setBarGap(0);
        barChart.setAnimated(false);

        //Create the dataset to put into the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("NG per Course");

        //Fill in the dataset with a for loop, getting the number of ng data from data models
        for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
            series.getData().add(new XYChart.Data<>(CurrentGradesModel.getCourseName(i)+ " ", CurrentGradesModel.getCourseNG(i)));
        }

        //Add the dataset into the chart
        barChart.getData().add(series);
        return barChart;
    }
}
