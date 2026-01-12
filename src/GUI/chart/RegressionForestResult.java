package GUI.chart;

import javafx.scene.chart.BarChart;

public class RegressionForestResult {
    private final BarChart<String, Number> chart;
    private final double predictedGrade;

    public RegressionForestResult(BarChart<String, Number> chart, double predictedGrade) {
        this.chart = chart;
        this.predictedGrade = predictedGrade;
    }

    public BarChart<String, Number> getChart() {
        return chart;
    }

    public double getPredictedGrade() {
        return predictedGrade;
    }
}