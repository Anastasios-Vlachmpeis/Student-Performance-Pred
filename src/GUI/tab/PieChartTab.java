package GUI.tab;

import datamodels.CurrentGradesModel;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import solutions.Phase1Step2Methods;

public class PieChartTab {

    private ComboBox<String> datasetCombo;
    private PieChart pieChart;
    private Label infoLabel;

    public BorderPane createPieChart() {
        BorderPane root = new BorderPane();

        // dataset selection
        datasetCombo = new ComboBox<>();
        datasetCombo.getItems().addAll(
                "Cum Laude percentage",
                "Graduation percentage"
        );
        datasetCombo.setValue("Cum Laude percentage");

        pieChart = new PieChart();

        infoLabel = new Label();
        infoLabel.setPadding(new Insets(5));

        VBox topBox = new VBox(10, datasetCombo);
        topBox.setPadding(new Insets(10));
        root.setTop(topBox);
        root.setCenter(pieChart);
        root.setBottom(infoLabel);

        //update and regenerate the pie chart whenever there is a change on dataset combo
        datasetCombo.setOnAction(e -> updatePieChart());

        // initialize
        updatePieChart();

        return root;
    }

    private void updatePieChart() {
        pieChart.getData().clear();
        String type = datasetCombo.getValue();

        if ("Cum Laude percentage".equals(type)) {
            displayCumLaudeChart();
        } else if ("Graduation percentage".equals(type)) {
            displayGraduationChart();
        }
    }


    //displays the cum laude chart by getting help from the helper methods
    private void displayCumLaudeChart() {
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        int totalStudents = studentIds.length;

        int cumLaudeCount = calculateCumLaudeCount(studentIds);
        int nonCumLaude = totalStudents - cumLaudeCount;

        pieChart.setTitle("CumLaude - NonCumLaude Students");

        pieChart.getData().add(new PieChart.Data("CumLaude", cumLaudeCount));
        pieChart.getData().add(new PieChart.Data("NonCumLaude", nonCumLaude));


        //change the info label so that it shows the right percentage
        infoLabel.setText(String.format(
                "Cum Laude: %d (%.1f%%), Non Cum Laude: %d (%.1f%%)",
                cumLaudeCount, 100.0 * cumLaudeCount / totalStudents,
                nonCumLaude, 100.0 * nonCumLaude / totalStudents
        ));
    }

    //to display the students that are expected to graduate again by getting help from helper methods
    private void displayGraduationChart() {
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        int totalStudents = studentIds.length;

        int graduates = calculatePredictedGraduates();
        int nonGraduates = totalStudents - graduates;

        pieChart.setTitle("Expected Number of Graduate-NonGraduate Students");

        pieChart.getData().add(new PieChart.Data("Expected Graduates", graduates));
        pieChart.getData().add(new PieChart.Data("Expected NonGraduates", nonGraduates));

        infoLabel.setText(String.format(
                "Expected Graduates: %d (%.1f%%), Expected NonGraduates: %d (%.1f%%)",
                graduates, 100.0 * graduates / totalStudents,
                nonGraduates, 100.0 * nonGraduates / totalStudents
        ));
    }



    //helper method to calculate cum laude count
    private int calculateCumLaudeCount(int[] studentIds) {
        int count = 0;
        for (int studentId : studentIds) {
            if (CurrentGradesModel.calcStudentMean(studentId) > 8) count++;
        }
        return count;
    }

    //helper method to calculate expected graduates
    private int calculatePredictedGraduates() {
        // simulate the monte carlo simulation, 1000 iteration for optimal value/time
        // resits are 0 as a constant because of some problem!, still gives the right value
        double predictedGraduates = Phase1Step2Methods
                .predictGraduateAmountMonteCarloSimulation(1000, 0);
        return (int) Math.round(predictedGraduates);
    }
}