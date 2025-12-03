package GUI.chart;

import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import datamodels.CurrentGradesModel;
import javafx.scene.control.Alert;
import javafx.scene.shape.Circle;
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
            int yAxisFilterStart, int yAxisFilterEnd) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisData);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData);

        ScatterChart<String, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(yAxisData + " " + xAxisData);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(yAxisData + " Distribution");

        // ----- PER COURSE -----
        if (xAxisData.equals("Per Course")) {
            xAxisFilterStart = Util.cap(0, CurrentGradesModel.courseCount, xAxisFilterStart);
            xAxisFilterEnd = Util.cap(0, CurrentGradesModel.courseCount - 1, xAxisFilterEnd);

            for (int i = xAxisFilterStart; i <= xAxisFilterEnd; i++) {
                String courseName = CurrentGradesModel.getCourseName(i);
                Number value = null;

                switch (yAxisData) {
                    case "Number of NG" -> value = CurrentGradesModel.getCourseNG(i);
                    case "Mean of Grades" -> value = CurrentGradesModel.calcCourseMean(i);
                    case "Mode of Grades" -> value = CurrentGradesModel.calcCourseMode(i);
                    case "Median of Grades" -> value = CurrentGradesModel.calcCourseMedian(i);
                }

                if (value != null) {
                    if (value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>(courseName, value);
                        data.setNode(new Circle(5));
                        series.getData().add(data);
                    }
                } else { // default case: show all individual grades
                    double[] allGrades = CurrentGradesModel.getAllGradesCourse(i);
                    for (double grade : allGrades) {
                        if (grade >= yAxisFilterStart && grade <= yAxisFilterEnd) {
                            XYChart.Data<String, Number> data = new XYChart.Data<>(courseName, grade);
                            data.setNode(new Circle(5));
                            series.getData().add(data);
                        }
                    }
                }
            }
        }

        //  PER STUDENT
        if (xAxisData.equals("Per Student")) {
            xAxisFilterStart = Util.cap(0, CurrentGradesModel.studentCount, xAxisFilterStart);
            xAxisFilterEnd = Util.cap(0, CurrentGradesModel.studentCount - 1, xAxisFilterEnd);

            for (int i = xAxisFilterStart; i <= xAxisFilterEnd; i++) {
                String studentName = "Student " + i;
                Number value = null;

                switch (yAxisData) {
                    case "Number of NG" -> value = CurrentGradesModel.getStudentNG(i);
                    case "Mean of Grades" -> value = CurrentGradesModel.calcStudentMean(i);
                    case "Mode of Grades" -> value = CurrentGradesModel.calcStudentMode(i);
                    case "Median of Grades" -> value = CurrentGradesModel.calcStudentMedian(i);
                }

                if (value != null) {
                    if (value.doubleValue() >= yAxisFilterStart && value.doubleValue() <= yAxisFilterEnd) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>(studentName, value);
                        data.setNode(new Circle(5));
                        series.getData().add(data);
                    }
                } else { // default case: show all individual grades
                    double[] allGrades = CurrentGradesModel.getAllGradesStudent(i);
                    for (double grade : allGrades) {
                        if (grade >= yAxisFilterStart && grade <= yAxisFilterEnd) {
                            XYChart.Data<String, Number> data = new XYChart.Data<>(studentName, grade);
                            data.setNode(new Circle(5));
                            series.getData().add(data);
                        }
                    }
                }
            }
        }

        scatterChart.getData().add(series);
        applyBeesSwarmLayout(series, 10);
        return scatterChart;
    }

    private void applyBeesSwarmLayout(XYChart.Series<String, Number> series, double radius) {
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
                    double xOffset = 0;
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
                    placed.add(new Point2D(xOffset,y));
                    node.setTranslateX(xOffset);
                });
            }
        }


    }
}