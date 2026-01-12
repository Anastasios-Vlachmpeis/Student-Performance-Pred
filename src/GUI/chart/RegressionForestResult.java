package GUI.chart;

import javafx.scene.chart.BarChart;

public class RegressionForestResult {
    private final BarChart<String, Number> chart;
    private final double predictedGrade;

    //constructor method to store the regression forest chart and the predicted grade
    public RegressionForestResult(BarChart<String, Number> chart, double predictedGrade) {
        this.chart = chart;
        this.predictedGrade = predictedGrade;
    }


    //getters for the chart and the predicted grade
    public BarChart<String, Number> getChart() {
        return chart;
    }

    public double getPredictedGrade() {
        return predictedGrade;
    }
}