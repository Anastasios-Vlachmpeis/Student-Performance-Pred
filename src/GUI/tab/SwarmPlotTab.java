package GUI.tab;

import GUI.chart.SwarmPlotGenerator;
import GUI.style.UIStyling;
import datamodels.CurrentGradesModel;
import datamodels.CategoricalFeature;
import datamodels.StudentInfoModel;
import javafx.scene.chart.Chart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SwarmPlotTab {
    private BorderPane root;
    private ComboBox<String> courseComboBox;
    private ComboBox<String> featureComboBox;
    private SwarmPlotGenerator generator;

    public BorderPane createTab() {
        this.root = new BorderPane();
        this.generator = new SwarmPlotGenerator();

        VBox controlPanel = new VBox();
        UIStyling.styleControlPanel(controlPanel);

        Label titleLabel = new Label("Grade Distribution by Feature");
        UIStyling.styleTitleLabel(titleLabel);

        // Course selection
        Label courseLabel = new Label("Select Course:");
        UIStyling.styleHeadingLabel(courseLabel);
        courseComboBox = new ComboBox<>();
        UIStyling.styleComboBox(courseComboBox);
        String[] courses = CurrentGradesModel.getCourses();
        courseComboBox.getItems().addAll(courses);
        if (courses.length > 0) {
            courseComboBox.setValue(courses[0]);
        }
        // Auto-update when course selection changes
        courseComboBox.setOnAction(e -> updateVisualization());

        // Feature selection (only categorical features)
        Label featureLabel = new Label("Group by Feature:");
        UIStyling.styleHeadingLabel(featureLabel);
        featureComboBox = new ComboBox<>();
        UIStyling.styleComboBox(featureComboBox);
        
        // Get categorical feature names
        String[] featureNames = StudentInfoModel.featureNames;
        Integer[] categoricalIds = CategoricalFeature.getAllowedIds();
        for (int id : categoricalIds) {
            if (id < featureNames.length) {
                featureComboBox.getItems().add(featureNames[id]);
            }
        }
        if (featureComboBox.getItems().size() > 0) {
            featureComboBox.setValue(featureComboBox.getItems().get(0));
        }
        // Auto-update when feature selection changes
        featureComboBox.setOnAction(e -> updateVisualization());

        controlPanel.getChildren().addAll(
            titleLabel,
            courseLabel,
            courseComboBox,
            featureLabel,
            featureComboBox
        );

        root.setLeft(controlPanel);
        
        // Generate initial visualization
        updateVisualization();

        return root;
    }

    private void updateVisualization() {
        String selectedCourse = courseComboBox.getValue();
        String selectedFeature = featureComboBox.getValue();
        
        if (selectedCourse == null || selectedFeature == null) {
            root.setCenter(new Label("Please select a course and feature."));
            return;
        }

        // Find course ID
        int courseId = -1;
        String[] courses = CurrentGradesModel.getCourses();
        for (int i = 0; i < courses.length; i++) {
            if (courses[i].equals(selectedCourse)) {
                courseId = i;
                break;
            }
        }

        if (courseId == -1) {
            root.setCenter(new Label("Course not found."));
            return;
        }

        Chart chart = generator.createChart(courseId, selectedFeature);
        root.setCenter(chart);
    }
}