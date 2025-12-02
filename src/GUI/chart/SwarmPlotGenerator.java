package GUI.chart;

import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import datamodels.CurrentGradesModel;
import javafx.scene.control.Alert;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import javafx.geometry.Point2D;
import tools.Util;


public class SwarmPlotGenerator {

    // Random generator to create small horizontal offsets for the swarm plot
    private Random random = new Random();

    public ScatterChart<String, Number> createChart(
            String xAxisData, String yAxisData, int xAxisFilterStart, int xAxisFilterEnd,
            int yAxisFilterStart, int yAxisFilterEnd)  {
        // create the x-axis as a category axis as courses/students are categorical
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisData); // labels the x-axis
        // create the Y-axis as a number-axis for numerical values
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData); // labels the y-axis

        // create the scatter-chart object with the axes (as there is no built-in swarm-plot function)
        ScatterChart<String, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(yAxisData + " " + xAxisData); // sets title
        // series holds all the data points
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(yAxisData + " Distribution"); // name for the legend

        // X-axis is the courses
        if (xAxisData.equals("Per Course")) {
            xAxisFilterStart = Util.cap(0, CurrentGradesModel.courseCount, xAxisFilterStart);
            xAxisFilterEnd = Util.cap(0, CurrentGradesModel.studentCount, xAxisFilterEnd);
            // alert if the filters are set out of range
            if (xAxisFilterStart < 0 || xAxisFilterEnd > CurrentGradesModel.courseCount || xAxisFilterStart >= xAxisFilterEnd) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Range");
                alert.setHeaderText("Filter range is out of bounds!");
                alert.setContentText("Valid range: 0 to " + CurrentGradesModel.courseCount);
                alert.show();
            }
            // loops through the courses in the filter range
            for (int i = xAxisFilterStart; i <= xAxisFilterEnd; i++) {
                String courseName = CurrentGradesModel.getCourseName(i);
                // Next loop gets all individual grades for the course
                for (Number value : CurrentGradesModel.getAllGradesCourse(i)) {
                    // apply y-axis filter
                    if (value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                        // Add data-point to the series
                        series.getData().add(new XYChart.Data<>(courseName, value));
                    }
                }
            }
        }
        // X-axis is the students
        if (xAxisData.equals("Per Student")) {
            // alert if the filters are set out of range
            if (xAxisFilterStart < 0 || xAxisFilterEnd > CurrentGradesModel.courseCount || xAxisFilterStart >= xAxisFilterEnd) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Range");
                alert.setHeaderText("Filter range is out of bounds!");
                alert.setContentText("Valid range: 0 to " + CurrentGradesModel.courseCount);
                alert.show();
            }
            // loop through the students in the filter range
            for (int i = xAxisFilterStart; i <= xAxisFilterEnd; i++) {
                String studentName = "Student " + i;
                // loop through all the individual grades of the students
                for (Number value : CurrentGradesModel.getAllGradesStudent(i)) {
                    // apply y axis filter
                    if (value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                        // add the data point to the series
                        series.getData().add(new XYChart.Data<>(studentName, value));
                    }
                }
            }
        }

        scatterChart.getData().add(series); // add the series to the chart
        // this applies horizontal jitter to each data point
        // makes overlapping points spread out visually like a swarm plot
        applyBeeswarmLayout(series, 10);
        // return the chart with the data and jitter applied
        return scatterChart;
    }
    private void applyBeeswarmLayout(XYChart.Series<String, Number> series, double radius) {
        Map<String, List<XYChart.Data<String, Number>>> grouped = new HashMap<>();

        for (XYChart.Data<String, Number> d : series.getData()) {
            grouped.computeIfAbsent(d.getXValue(), k -> new ArrayList<>()).add(d);
        }
        for (String category : grouped.keySet()) {

            List<XYChart.Data<String, Number>> points = grouped.get(category);

            points.sort(Comparator.comparingDouble(p -> p.getYValue().doubleValue()));

            List<Point2D> placed = new ArrayList<>();

            for (XYChart.Data<String, Number> p : points) {

                p.nodeProperty().addListener((obs, oldNode, node) -> {
                    if (node == null) return;

                    double y = p.getYValue().doubleValue();
                    double xOffset = 0.0;
                    double step = radius * 0.6;

                    while (true) {
                        boolean collision = false;

                        for (Point2D pos : placed) {
                            if (Math.hypot(pos.getX() - xOffset, pos.getY() - y) < radius) {
                                collision = true;
                                break;
                            }
                        }

                        if (!collision) break;

                        xOffset = (xOffset >= 0 ? -step : step) * (1 + Math.abs(xOffset / step));

                    }
                    placed.add(new Point2D(xOffset, y));
                    node.setTranslateX(xOffset);
                });

            }

        }



    }
}
