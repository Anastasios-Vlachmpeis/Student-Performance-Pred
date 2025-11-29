package scatterplot;

import datamodels.CurrentGradesModel;
import datamodels.StudentInfoModel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScatterPlotDashboard extends Application {
    
    private ComboBox<String> xAxisComboBox;
    private ComboBox<String> yAxisComboBox;
    private Slider xAxisMinSlider;
    private Slider xAxisMaxSlider;
    private Slider yAxisMinSlider;
    private Slider yAxisMaxSlider;
    private Label xAxisMinLabel;
    private Label xAxisMaxLabel;
    private Label yAxisMinLabel;
    private Label yAxisMaxLabel;
    private BorderPane root;
    private ScatterPlotGenerator generator;
    private ComboBox<String> courseComboBox;
    private ComboBox<String> featureComboBox;
    private CheckBox showActualGradesCheckBox;
    private VBox predictionControls;
    
    @Override
    public void start(Stage primaryStage) {
        generator = new ScatterPlotGenerator();
        root = new BorderPane();
        
        // Create control panel
        VBox controlPanel = createControlPanel();
        root.setLeft(controlPanel);
        
        // Create initial chart
        updateChart();
        
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Scatter Plot Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(300);
        
        // X-Axis Selection
        Label xAxisLabel = new Label("X-Axis:");
        xAxisComboBox = new ComboBox<>();
        xAxisComboBox.getItems().addAll("Per Student", "Per Course");
        xAxisComboBox.setValue("Per Student");
        xAxisComboBox.setOnAction(e -> {
            updateYAxisOptions();
            updatePredictionControls();
            updateSliderRanges();
            updateChart();
        });
        
        // Y-Axis Selection
        Label yAxisLabel = new Label("Y-Axis:");
        yAxisComboBox = new ComboBox<>();
        updateYAxisOptions();
        yAxisComboBox.setValue("Mean");
        yAxisComboBox.setOnAction(e -> {
            updatePredictionControls();
            updateSliderRanges();
            updateChart();
        });
        
        // Prediction controls (course and feature selectors)
        predictionControls = createPredictionControls();
        
        // X-Axis Filter Sliders
        Label xFilterLabel = new Label("X-Axis Filter:");
        xAxisMinSlider = new Slider();
        xAxisMaxSlider = new Slider();
        xAxisMinLabel = new Label();
        xAxisMaxLabel = new Label();
        setupSlider(xAxisMinSlider, xAxisMinLabel, true);
        setupSlider(xAxisMaxSlider, xAxisMaxLabel, false);
        
        // Y-Axis Filter Sliders
        Label yFilterLabel = new Label("Y-Axis Filter:");
        yAxisMinSlider = new Slider();
        yAxisMaxSlider = new Slider();
        yAxisMinLabel = new Label();
        yAxisMaxLabel = new Label();
        setupSlider(yAxisMinSlider, yAxisMinLabel, true);
        setupSlider(yAxisMaxSlider, yAxisMaxLabel, false);
        
        // Initial slider range setup
        updateSliderRanges();
        
        panel.getChildren().addAll(
            xAxisLabel, xAxisComboBox,
            yAxisLabel, yAxisComboBox,
            predictionControls,
            new Separator(Orientation.HORIZONTAL),
            xFilterLabel, xAxisMinLabel, xAxisMinSlider, xAxisMaxLabel, xAxisMaxSlider,
            new Separator(Orientation.HORIZONTAL),
            yFilterLabel, yAxisMinLabel, yAxisMinSlider, yAxisMaxLabel, yAxisMaxSlider
        );
        
        return panel;
    }
    
    private void setupSlider(Slider slider, Label label, boolean isMin) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(5);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSliderLabel(slider, label);
            if (isMin) {
                // Ensure min <= max
                if (slider == xAxisMinSlider && newVal.doubleValue() > xAxisMaxSlider.getValue()) {
                    xAxisMaxSlider.setValue(newVal.doubleValue());
                } else if (slider == yAxisMinSlider && newVal.doubleValue() > yAxisMaxSlider.getValue()) {
                    yAxisMaxSlider.setValue(newVal.doubleValue());
                }
            } else {
                // Ensure max >= min
                if (slider == xAxisMaxSlider && newVal.doubleValue() < xAxisMinSlider.getValue()) {
                    xAxisMinSlider.setValue(newVal.doubleValue());
                } else if (slider == yAxisMaxSlider && newVal.doubleValue() < yAxisMinSlider.getValue()) {
                    yAxisMinSlider.setValue(newVal.doubleValue());
                }
            }
            updateChart();
        });
    }
    
    private void updateSliderLabel(Slider slider, Label label) {
        label.setText(String.format("%.2f", slider.getValue()));
    }
    
    private void updateYAxisOptions() {
        String currentSelection = yAxisComboBox.getValue();
        yAxisComboBox.getItems().clear();
        
        yAxisComboBox.getItems().addAll("Mean", "Mode", "Median", "Number of NGs");
        
        // Add "Predicted Grade" option (only for Per Student X-axis)
        if (xAxisComboBox.getValue().equals("Per Student")) {
            yAxisComboBox.getItems().add("Predicted Grade");
        }
        
        // Add course-only options if X-axis is "Per Course"
        if (xAxisComboBox.getValue().equals("Per Course")) {
            yAxisComboBox.getItems().addAll(
                "Number of Passing Students",
                "Number of Non-Passing Students",
                "Number of Cum-Laude Students"
            );
        }
        
        // Restore selection if it's still valid, otherwise select first item
        if (yAxisComboBox.getItems().contains(currentSelection)) {
            yAxisComboBox.setValue(currentSelection);
        } else {
            yAxisComboBox.setValue(yAxisComboBox.getItems().get(0));
        }
    }
    
    private VBox createPredictionControls() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        
        // Course selector
        Label courseLabel = new Label("Course:");
        courseComboBox = new ComboBox<>();
        String[] courses = CurrentGradesModel.getCourses();
        for (int i = 0; i < courses.length; i++) {
            courseComboBox.getItems().add(courses[i]);
        }
        if (courseComboBox.getItems().size() > 0) {
            courseComboBox.setValue(courseComboBox.getItems().get(0));
        }
        courseComboBox.setOnAction(e -> updateChart());
        
        // Feature selector
        Label featureLabel = new Label("Feature:");
        featureComboBox = new ComboBox<>();
        String[] featureNames = StudentInfoModel.featureNames;
        for (String featureName : featureNames) {
            featureComboBox.getItems().add(featureName);
        }
        if (featureComboBox.getItems().size() > 0) {
            featureComboBox.setValue(featureComboBox.getItems().get(0));
        }
        featureComboBox.setOnAction(e -> updateChart());
        
        // Checkbox to show actual grades alongside predicted
        showActualGradesCheckBox = new CheckBox("Show Actual Grades");
        showActualGradesCheckBox.setSelected(false);
        showActualGradesCheckBox.setOnAction(e -> updateChart());
        
        controls.getChildren().addAll(courseLabel, courseComboBox, featureLabel, featureComboBox, showActualGradesCheckBox);
        controls.setVisible(false);
        controls.setManaged(false);
        
        return controls;
    }
    
    private void updatePredictionControls() {
        boolean showControls = yAxisComboBox.getValue() != null && 
                              yAxisComboBox.getValue().equals("Predicted Grade");
        predictionControls.setVisible(showControls);
        predictionControls.setManaged(showControls);
    }
    
    private void updateSliderRanges() {
        String xAxisData = xAxisComboBox.getValue();
        String yAxisData = yAxisComboBox.getValue();
        
        // Update X-axis slider ranges
        if (xAxisData.equals("Per Student")) {
            xAxisMinSlider.setMin(0);
            xAxisMinSlider.setMax(1522);
            xAxisMinSlider.setValue(0);
            xAxisMaxSlider.setMin(0);
            xAxisMaxSlider.setMax(1522);
            xAxisMaxSlider.setValue(1522);
        } else { // Per Course
            xAxisMinSlider.setMin(0);
            xAxisMinSlider.setMax(36);
            xAxisMinSlider.setValue(0);
            xAxisMaxSlider.setMin(0);
            xAxisMaxSlider.setMax(36);
            xAxisMaxSlider.setValue(36);
        }
        
        // Update Y-axis slider ranges
        double yMin = 0;
        double yMax = 10;
        
        if (yAxisData.equals("Number of NGs")) {
            if (xAxisData.equals("Per Student")) {
                yMax = 36; // Students can have 0-36 NGs
            } else { // Per Course
                yMax = 1522; // Courses can have 0-1522 NGs
            }
        } else if (yAxisData.equals("Number of Passing Students") || 
                   yAxisData.equals("Number of Non-Passing Students") ||
                   yAxisData.equals("Number of Cum-Laude Students")) {
            yMax = 1522; // Course-specific metrics
        } else if (yAxisData.equals("Predicted Grade")) {
            yMax = 10; // Predicted grades are 0-10
        }
        // Mean, Mode, Median default to 0-10
        
        yAxisMinSlider.setMin(yMin);
        yAxisMinSlider.setMax(yMax);
        yAxisMinSlider.setValue(yMin);
        yAxisMaxSlider.setMin(yMin);
        yAxisMaxSlider.setMax(yMax);
        yAxisMaxSlider.setValue(yMax);
        
        // Update labels
        updateSliderLabel(xAxisMinSlider, xAxisMinLabel);
        updateSliderLabel(xAxisMaxSlider, xAxisMaxLabel);
        updateSliderLabel(yAxisMinSlider, yAxisMinLabel);
        updateSliderLabel(yAxisMaxSlider, yAxisMaxLabel);
    }
    
    private void updateChart() {
        String xAxisData = xAxisComboBox.getValue();
        String yAxisData = yAxisComboBox.getValue();
        
        double xMin = xAxisMinSlider.getValue();
        double xMax = xAxisMaxSlider.getValue();
        double yMin = yAxisMinSlider.getValue();
        double yMax = yAxisMaxSlider.getValue();
        
        // Get course and feature info if needed for predictions
        String selectedCourse = null;
        String selectedFeature = null;
        boolean showActualGrades = false;
        if (yAxisData != null && yAxisData.equals("Predicted Grade")) {
            selectedCourse = courseComboBox.getValue();
            selectedFeature = featureComboBox.getValue();
            showActualGrades = showActualGradesCheckBox.isSelected();
        }
        
        ScatterChart<Number, Number> chart = generator.createChart(
            xAxisData, yAxisData, xMin, xMax, yMin, yMax, selectedCourse, selectedFeature, showActualGrades
        );
        
        root.setCenter(chart);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

