package GUI;
import datamodels.CurrentGradesModel;
import javafx.scene.chart.*;

public class ScatterPlotGenerator {

    private ScatterChart<Number, Number> chart;

    public Chart createChart() {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Course index");
        yAxis.setLabel("Number of NG");

        chart = new ScatterChart<>(xAxis, yAxis);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("NG per course");



        for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
            series.getData().add(
                    new XYChart.Data<>(i, CurrentGradesModel.getCourseNG(i))
            );
        }
        chart.getData().add(series);
        return chart;


    }
}