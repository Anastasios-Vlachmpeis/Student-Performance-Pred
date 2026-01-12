package GUI.chart;

import GUI.style.UIStyling;
import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import jdk.jfr.Frequency;
import regressionTree.RegressionTreeTrainer;
import regressionTree.TreeNode;
import regressionTree.regressionForest;

import java.util.ArrayList;

public class RegressionForestGenerator {
    public RegressionForestResult createChart(int treeCount, int courseId, int studentId) {
        // create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Grades Produced by the Regression Trees");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency of Grades");

        // create the chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);

        //create the dataset
        XYChart.Series<String, Number> dataset = new XYChart.Series<>();
        java.util.ArrayList<XYChart.Data<String, Number>> barData = new java.util.ArrayList<>();

        //Set the initial values
        ArrayList<Integer> allStudents = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        int three = 0, four = 0, five = 0, six = 0, seven = 0, eight = 0, nine = 0, ten = 0;
        double predictionSum = 0;

        //have a for loop to train the asked amount of randomized regression trees
        for (int i = 1; i <= treeCount; i++) {
            ArrayList<Integer> trainingStudents =  regressionForest.getRandom(allStudents);
            TreeNode regressionTree = RegressionTreeTrainer.train(trainingStudents, courseId);
            double predictedGrade = regressionTree.predict(studentId);
            //create the frequency data
            if (Math.round(predictedGrade) == 3) {three++;}
            if (Math.round(predictedGrade) == 4) {four++;}
            if (Math.round(predictedGrade) == 5) {five++;}
            if (Math.round(predictedGrade) == 6) {six++;}
            if (Math.round(predictedGrade) == 7) {seven++;}
            if (Math.round(predictedGrade) == 8) {eight++;}
            if (Math.round(predictedGrade) == 9) {nine++;}
            if (Math.round(predictedGrade) == 10) {ten++;}
            predictionSum = predictionSum + predictedGrade;
        }

        //add the data to the dataset
        dataset.getData().add(new XYChart.Data<>("Grade 3", three));
        dataset.getData().add(new XYChart.Data<>("Grade 4", four));
        dataset.getData().add(new XYChart.Data<>("Grade 5", five));
        dataset.getData().add(new XYChart.Data<>("Grade 6", six));
        dataset.getData().add(new XYChart.Data<>("Grade 7", seven));
        dataset.getData().add(new XYChart.Data<>("Grade 8", eight));
        dataset.getData().add(new XYChart.Data<>("Grade 9", nine));
        dataset.getData().add(new XYChart.Data<>("Grade 10", ten));

        // Find max frequency for color normalization
        int maxFrequency = Math.max(Math.max(Math.max(three, four), Math.max(five, six)),
                                   Math.max(Math.max(seven, eight), Math.max(nine, ten)));
        final int finalMaxFrequency = maxFrequency > 0 ? maxFrequency : 1;

        // Apply coloring to each bar
        for (XYChart.Data<String, Number> data : dataset.getData()) {
            int frequency = data.getYValue().intValue();
            
            // Calculate color using centralized method
            String colorStyle = UIStyling.calculateBarColorStyle(frequency, finalMaxFrequency);
            
            // Apply color styling when node is created
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(colorStyle);
                }
            });
        }

        barChart.getData().add(dataset);
        
        // Style bars after chart is rendered (fallback)
        barChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                for (XYChart.Data<String, Number> data : dataset.getData()) {
                    int frequency = data.getYValue().intValue();
                    String colorStyle = UIStyling.calculateBarColorStyle(frequency, finalMaxFrequency);
                    if (data.getNode() != null) {
                        data.getNode().setStyle(colorStyle);
                    }
                }
            }
        });

        //return as result class to prevent any conflicts
        double predictedGrade = predictionSum / treeCount;
        return new RegressionForestResult(barChart, predictedGrade);
    }
}
