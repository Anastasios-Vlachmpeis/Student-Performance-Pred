package GUI.chart;

import datamodels.CurrentGradesModel;
import GUI.style.UIStyling;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

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
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> dataset = new XYChart.Series<>();
        dataset.setName(yAxisData);

        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        java.util.ArrayList<XYChart.Data<String, Number>> barData = new java.util.ArrayList<>();
        double maxValue = 0.0;

       // when x is Per Course
        if (xAxisData.equals("Per Course")) {
            
            // Collect course indices that pass the filters
            java.util.ArrayList<Integer> courseIndices = new java.util.ArrayList<>();
            for (int i = xAxisFilterStart; i <= xAxisFilterEnd - 1; i++) {
                Number value = switch (yAxisData) {
                    case "Number of NG" -> CurrentGradesModel.getCourseNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcCourseMean(i);
                    case "Mode of Grades" -> CurrentGradesModel.calcCourseMode(i);
                    case "Median of Grades" -> CurrentGradesModel.calcCourseMedian(i);
                    default -> 0;
                };
                
                if (value.doubleValue() >= yFilterMin && value.doubleValue() <= yFilterMax) {
                    courseIndices.add(i);
                }
            }
            
            // Sort by number of NG's in descending order
            courseIndices.sort((a, b) -> Integer.compare(
                CurrentGradesModel.getAllValidGradesCourse(a).size(),
                CurrentGradesModel.getAllValidGradesCourse(b).size()
            ));
            
            // Add sorted courses to dataset
            for (int i : courseIndices) {
                String courseName = CurrentGradesModel.getCourseName(i);
                Number value = switch (yAxisData) {
                    case "Number of NG" -> CurrentGradesModel.getCourseNG(i);
                    case "Mean of Grades" -> CurrentGradesModel.calcCourseMean(i);
                    case "Mode of Grades" -> CurrentGradesModel.calcCourseMode(i);
                    case "Median of Grades" -> CurrentGradesModel.calcCourseMedian(i);
                    default -> 0;
                };
                barData.add(new XYChart.Data<>(courseName, value));
                maxValue = Math.max(maxValue, value.doubleValue());
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
                    barData.add(new XYChart.Data<>(studentName, value));
                    maxValue = Math.max(maxValue, value.doubleValue());
                }
            }
        }

        // Store maxValue as final for use in lambda
        final double finalMaxValue = maxValue > 0 ? maxValue : 1.0;

        // Apply coloring based on value
        for (XYChart.Data<String, Number> data : barData) {
            double value = data.getYValue().doubleValue();
            
            // Calculate color using centralized method
            String colorStyle = UIStyling.calculateBarColorStyle(value, finalMaxValue);
            
            // Apply color styling when node is created
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(colorStyle);
                }
            });
            
            dataset.getData().add(data);
        }

        barChart.getData().add(dataset);
        
        // Style bars after chart is rendered
        barChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                for (XYChart.Data<String, Number> data : dataset.getData()) {
                    double value = data.getYValue().doubleValue();
                    
                    // Calculate color using centralized method
                    String colorStyle = UIStyling.calculateBarColorStyle(value, finalMaxValue);
                    
                    if (data.getNode() != null) {
                        data.getNode().setStyle(colorStyle);
                    }
                }
            }
        });
        
        return barChart;
    }


}