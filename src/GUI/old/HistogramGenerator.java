package GUI.old;

import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tools.ChartDataUtils;
import tools.Statistics;

import java.util.Map;
import java.util.TreeMap;

public class HistogramGenerator {

    public BarChart<String, Number> createChart(String xAxisData, String yAxisData, double xAxisFilterStart, double xAxisFilterEnd, double yAxisFilterStart, double yAxisFilterEnd, String filterFeature, String filterValue) {

        int[] filterStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        // X-axis label
        CategoryAxis xAxis = new CategoryAxis();

        // Y-axis label
        NumberAxis yAxis = new NumberAxis();

        // This part creates the chart with the distance between Categories set to five for appearance
        BarChart<String, Number> histogram = new BarChart<>(xAxis,yAxis);
        histogram.setTitle(yAxisData + " with the " + xAxisData + " (unsupported X)");
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(5);
        histogram.setBarGap(0);
        histogram.setAnimated(false);

        xAxis.setLabel(xAxisData);
        yAxis.setLabel(yAxisData);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName(yAxisData + " with the " + xAxisData);

        Map<String, Integer> frequencies = new TreeMap<>();

        double binWidth = 0.5;

        if ("Number of Courses".equals(yAxisData)) {

            int nCourses = CurrentGradesModel.courseCount;

            for (int courseIndex = 0; courseIndex < nCourses; courseIndex++) {

                double value;

                try {
                    switch (xAxisData) {
                        case "Mean of Grades" -> value = CurrentGradesModel.calcCourseMean(courseIndex);
                        case "Median of Grades" -> value = CurrentGradesModel.calcCourseMedian(courseIndex);
                        case "Mode of Grades" -> value = CurrentGradesModel.calcCourseMode(courseIndex);
                        case "Number of NG" -> value = CurrentGradesModel.getCourseNG(courseIndex);

                        case "Number of Passing Students" -> value = ChartDataUtils.getPassingStudents(courseIndex);
                        case "Number of Non-passingStudents" -> value = ChartDataUtils.getNonPassingStudents(courseIndex);
                        default -> {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    continue;
                }

                if (value < xAxisFilterStart || value > xAxisFilterEnd) {
                    continue;
                }

                int binIndex = (int) Math.floor(value / binWidth);
                double left = binIndex * binWidth;
                double right = left + binWidth;

                String label = String.format("%.1f-%.1f", left, right);
                frequencies.put(label, frequencies.getOrDefault(label, 0) + 1);
            }
        }
        else if ("Number of Students".equals(yAxisData)) {

            if (filterStudentIds == null || filterStudentIds.length == 0) {
                histogram.getData().add(series1);
                return histogram;
            }

            for (int studentId : filterStudentIds) {

                double value;

                try {
                    switch (xAxisData) {
                        case "Mean of Grades" -> value = CurrentGradesModel.calcStudentMean(studentId);
                        case "Median of Grades" -> value = CurrentGradesModel.calcStudentMedian(studentId);
                        case "Mode of Grades" -> value = CurrentGradesModel.calcStudentMode(studentId);
                        case "Number of NG" -> value = CurrentGradesModel.getStudentNG(studentId);

                        default -> {
                            continue;
                        }
                    }
                }catch (Exception e) {
                    continue;
                }

                if (value < xAxisFilterStart || value > xAxisFilterEnd) {
                    continue;
                }

                int binIndex = (int) Math.floor(value / binWidth);
                double left = binIndex * binWidth;
                double right = left + binWidth;

                String label = String.format("%.1f-%.1f", left, right);
                frequencies.put(label, frequencies.getOrDefault(label, 0) + 1);
            }
        }

         for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
             String binlabel = entry.getKey();
             int count = entry.getValue();

             if (count < yAxisFilterStart || count > yAxisFilterEnd) {
                 continue;
             }

             series1.getData().add(new XYChart.Data<>(binlabel, count));
         }
         histogram.getData().add(series1);
         return histogram;
    }
}