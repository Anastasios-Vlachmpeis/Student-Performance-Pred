package tools;

import datamodels.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

/**
 * Factory class for creating shared chart control components.
 * Uses callbacks to handle chart-specific behavior.
 * 
 * Methods that can be used by other chart types:
 * - createFeatureFilterControls(Runnable onFeatureChange, Runnable onValueChange) -- Creates feature filter controls with combo boxes for feature selection and filter value
 * - createPredictionControls(Runnable onChange) -- Creates prediction controls with course and feature combo boxes plus show actual grades checkbox
 * - createCourseCorrelationControls(Runnable onChange) -- Creates course correlation controls with X and Y course combo boxes plus show Y=X line checkbox
 * - updateYAxisOptions(ComboBox<String> yAxisComboBox, ComboBox<String> xAxisComboBox, boolean courseCorrelationMode) -- Updates Y-axis combo box options based on X-axis selection and course correlation mode
 * - setupSlider(Slider slider, Label label, boolean isMin, Slider xAxisMinSlider, Slider xAxisMaxSlider, Slider yAxisMinSlider, Slider yAxisMaxSlider, Runnable onUpdate) -- Sets up slider with tick marks and listener that updates label and enforces min/max constraints
 * - updateSliderLabel(Slider slider, Label label) -- Updates slider label with formatted value
 * - setSliderRange(Slider minSlider, Slider maxSlider, double min, double max) -- Sets the range for min and max sliders
 * - updateSliderRanges(String xAxisData, String yAxisData, boolean courseCorrelationMode, Slider xAxisMinSlider, Slider xAxisMaxSlider, Slider yAxisMinSlider, Slider yAxisMaxSlider, Label xAxisMinLabel, Label xAxisMaxLabel, Label yAxisMinLabel, Label yAxisMaxLabel) -- Updates slider ranges based on axis data and course correlation mode
 * - updatePredictionControls(ComboBox<String> yAxisComboBox, VBox predictionControls) -- Updates prediction controls visibility based on Y-axis selection
 * - updateCourseCorrelationControls(boolean showControls, CheckBox courseCorrelationModeCheckBox, VBox courseCorrelationControls, ComboBox<String> xAxisComboBox, ComboBox<String> yAxisComboBox, ComboBox<String> xCourseComboBox, ComboBox<String> yCourseComboBox, Runnable onUpdateChart, Runnable onUpdateYAxisOptions, Runnable onUpdatePredictionControls, Runnable onUpdateSliderRanges) -- Updates course correlation controls and axis options when correlation mode changes
 */
public class ChartControlFactory {
    
    /**
     * Creates feature filter controls (feature combo box + value combo box)
     * @param onFeatureChange Callback when feature selection changes
     * @param onValueChange Callback when filter value changes
     * @return FeatureFilterControls containing the controls
     */
    public static FeatureFilterControls createFeatureFilterControls(
            Runnable onFeatureChange, 
            Runnable onValueChange) {
        
        VBox controls = new VBox(10);
        
        Label featureFilterLabel = new Label("Feature Filter:");
        ComboBox<String> featureFilterComboBox = new ComboBox<>();
        featureFilterComboBox.getItems().add("No Feature");
        featureFilterComboBox.getItems().addAll(StudentInfoModel.featureNames);
        featureFilterComboBox.setValue("No Feature");
        
        ComboBox<String> valueComboBox = new ComboBox<>();
        valueComboBox.setVisible(false);
        valueComboBox.setManaged(false);
        valueComboBox.setOnAction(e -> onValueChange.run());
        
        featureFilterComboBox.setOnAction(e -> {
            updateFeatureFilterValueOptions(featureFilterComboBox, valueComboBox);
            onFeatureChange.run();
        });
        
        controls.getChildren().addAll(featureFilterLabel, featureFilterComboBox, valueComboBox);
        
        return new FeatureFilterControls(controls, featureFilterComboBox, valueComboBox);
    }
    
    /**
     * Updates the feature filter value options based on selected feature
     */
    private static void updateFeatureFilterValueOptions(
            ComboBox<String> featureComboBox, 
            ComboBox<String> valueComboBox) {
        
        String selectedFeature = featureComboBox.getValue();
        valueComboBox.getItems().clear();
        
        if (selectedFeature == null || selectedFeature.equals("No Feature")) {
            valueComboBox.setVisible(false);
            valueComboBox.setManaged(false);
            return;
        }
        
        int featureIndex = ChartDataUtils.findFeatureId(selectedFeature);
        
        if (featureIndex == -1) {
            valueComboBox.setVisible(false);
            valueComboBox.setManaged(false);
            return;
        }
        
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        if (allStudentIds.length > 0) {
            try {
                Feature sampleFeature = StudentInfoModel.getFeature(allStudentIds[0], featureIndex);
                
                // Populate value options based on feature type
                if (sampleFeature instanceof CategoricalFeature) {
                    String[] categories = CategoricalFeature.getRange(featureIndex);
                    valueComboBox.getItems().addAll(categories);
                    if (categories.length > 0) {
                        valueComboBox.setValue(categories[0]);
                    }
                } else if (sampleFeature instanceof NumericalFeature) {
                    double min = NumericalFeature.getRangeMin(featureIndex);
                    double max = NumericalFeature.getRangeMax(featureIndex);
                    double median = (min + max) / 2.0;
                    valueComboBox.getItems().addAll(
                        "Above " + String.format("%.2f", median),
                        "Below " + String.format("%.2f", median),
                        "All"
                    );
                    valueComboBox.setValue("All");
                }
                
                valueComboBox.setVisible(true);
                valueComboBox.setManaged(true);
            } catch (Exception e) {
                valueComboBox.setVisible(false);
                valueComboBox.setManaged(false);
            }
        }
    }
    
    /**
     * Creates prediction controls (course + feature combo boxes + show actual checkbox)
     */
    public static PredictionControls createPredictionControls(Runnable onChange) {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        
        Label courseLabel = new Label("Course:");
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll(CurrentGradesModel.getCourses());
        if (!courseComboBox.getItems().isEmpty()) {
            courseComboBox.setValue(courseComboBox.getItems().get(0));
        }
        courseComboBox.setOnAction(e -> onChange.run());
        
        Label featureLabel = new Label("Feature:");
        ComboBox<String> featureComboBox = new ComboBox<>();
        featureComboBox.getItems().addAll(StudentInfoModel.featureNames);
        if (!featureComboBox.getItems().isEmpty()) {
            featureComboBox.setValue(featureComboBox.getItems().get(0));
        }
        featureComboBox.setOnAction(e -> onChange.run());
        
        CheckBox showActualGradesCheckBox = new CheckBox("Show Actual Grades");
        showActualGradesCheckBox.setOnAction(e -> onChange.run());
        
        controls.getChildren().addAll(courseLabel, courseComboBox, featureLabel, featureComboBox, showActualGradesCheckBox);
        controls.setVisible(false);
        controls.setManaged(false);
        
        return new PredictionControls(controls, courseComboBox, featureComboBox, showActualGradesCheckBox);
    }
    
    /**
     * Creates course correlation controls (X course + Y course + show Y=X checkbox)
     */
    public static CourseCorrelationControls createCourseCorrelationControls(Runnable onChange) {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        
        String[] courses = CurrentGradesModel.getCourses();
        
        Label xCourseLabel = new Label("X Course:");
        ComboBox<String> xCourseComboBox = new ComboBox<>();
        xCourseComboBox.getItems().addAll(courses);
        if (!xCourseComboBox.getItems().isEmpty()) {
            xCourseComboBox.setValue(xCourseComboBox.getItems().get(0));
        }
        
        Label yCourseLabel = new Label("Y Course:");
        ComboBox<String> yCourseComboBox = new ComboBox<>();
        yCourseComboBox.getItems().addAll(courses);
        if (yCourseComboBox.getItems().size() > 1) {
            yCourseComboBox.setValue(yCourseComboBox.getItems().get(1));
        } else if (!yCourseComboBox.getItems().isEmpty()) {
            yCourseComboBox.setValue(yCourseComboBox.getItems().get(0));
        }
        
        CheckBox showYEqualsXLineCheckBox = new CheckBox("Show Y=X Line");
        showYEqualsXLineCheckBox.setOnAction(e -> onChange.run());
        
        controls.getChildren().addAll(xCourseLabel, xCourseComboBox, yCourseLabel, yCourseComboBox, showYEqualsXLineCheckBox);
        controls.setVisible(false);
        controls.setManaged(false);
        
        return new CourseCorrelationControls(controls, xCourseComboBox, yCourseComboBox, showYEqualsXLineCheckBox);
    }
    
    /**
     * Updates Y-axis options based on X-axis selection and course correlation mode
     */
    public static void updateYAxisOptions(
            ComboBox<String> yAxisComboBox,
            ComboBox<String> xAxisComboBox,
            boolean courseCorrelationMode) {
        
        String currentSelection = yAxisComboBox.getValue();
        yAxisComboBox.getItems().clear();
        
        // If course correlation mode, show courses as Y-axis options
        if (courseCorrelationMode) {
            yAxisComboBox.getItems().addAll(CurrentGradesModel.getCourses());
            if (!yAxisComboBox.getItems().contains(currentSelection)) {
                yAxisComboBox.setValue(yAxisComboBox.getItems().get(0));
            } else {
                yAxisComboBox.setValue(currentSelection);
            }
            return;
        }
        
        // Add base Y-axis options
        yAxisComboBox.getItems().addAll("Mean", "Mode", "Median", "Number of NGs");
        if (xAxisComboBox.getValue().equals("Per Student")) {
            yAxisComboBox.getItems().add("Predicted Grade");
        } else if (xAxisComboBox.getValue().equals("Per Course")) {
            yAxisComboBox.getItems().addAll("Number of Passing Students", "Number of Non-Passing Students", "Number of Cum-Laude Students");
        }
        
        yAxisComboBox.setValue(yAxisComboBox.getItems().contains(currentSelection) ? currentSelection : yAxisComboBox.getItems().get(0));
    }
    
    /**
     * Sets up a slider with tick marks and a listener that updates label and enforces min/max constraints
     */
    public static void setupSlider(Slider slider, Label label, boolean isMin, 
                                   Slider xAxisMinSlider, Slider xAxisMaxSlider,
                                   Slider yAxisMinSlider, Slider yAxisMaxSlider,
                                   Runnable onUpdate) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(5);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSliderLabel(slider, label);
            if (isMin) {
                if (slider == xAxisMinSlider && newVal.doubleValue() > xAxisMaxSlider.getValue()) {
                    xAxisMaxSlider.setValue(newVal.doubleValue());
                } else if (slider == yAxisMinSlider && newVal.doubleValue() > yAxisMaxSlider.getValue()) {
                    yAxisMaxSlider.setValue(newVal.doubleValue());
                }
            } else {
                if (slider == xAxisMaxSlider && newVal.doubleValue() < xAxisMinSlider.getValue()) {
                    xAxisMinSlider.setValue(newVal.doubleValue());
                } else if (slider == yAxisMaxSlider && newVal.doubleValue() < yAxisMinSlider.getValue()) {
                    yAxisMinSlider.setValue(newVal.doubleValue());
                }
            }
            onUpdate.run();
        });
    }
    
    /**
     * Updates slider label with formatted value
     */
    public static void updateSliderLabel(Slider slider, Label label) {
        label.setText(String.format("%.2f", slider.getValue()));
    }
    
    /**
     * Sets the range for min and max sliders
     */
    public static void setSliderRange(Slider minSlider, Slider maxSlider, double min, double max) {
        minSlider.setMin(min);
        minSlider.setMax(max);
        minSlider.setValue(min);
        maxSlider.setMin(min);
        maxSlider.setMax(max);
        maxSlider.setValue(max);
    }
    
    /**
     * Updates slider ranges based on axis data and course correlation mode
     */
    public static void updateSliderRanges(String xAxisData, String yAxisData, boolean courseCorrelationMode,
                                          Slider xAxisMinSlider, Slider xAxisMaxSlider,
                                          Slider yAxisMinSlider, Slider yAxisMaxSlider,
                                          Label xAxisMinLabel, Label xAxisMaxLabel,
                                          Label yAxisMinLabel, Label yAxisMaxLabel) {
        if (courseCorrelationMode) {
            setSliderRange(xAxisMinSlider, xAxisMaxSlider, 0, 10);
            setSliderRange(yAxisMinSlider, yAxisMaxSlider, 0, 10);
            updateSliderLabel(xAxisMinSlider, xAxisMinLabel);
            updateSliderLabel(xAxisMaxSlider, xAxisMaxLabel);
            updateSliderLabel(yAxisMinSlider, yAxisMinLabel);
            updateSliderLabel(yAxisMaxSlider, yAxisMaxLabel);
            return;
        }
        
        if (xAxisData.equals("Per Student")) {
            setSliderRange(xAxisMinSlider, xAxisMaxSlider, 0, 1522);
        } else {
            setSliderRange(xAxisMinSlider, xAxisMaxSlider, 0, 35);
        }
        
        double yMax = 10;
        if (yAxisData.equals("Number of NGs")) {
            yMax = xAxisData.equals("Per Student") ? 36 : 1522;
        } else if (yAxisData.equals("Number of Passing Students") ||
                   yAxisData.equals("Number of Non-Passing Students") ||
                   yAxisData.equals("Number of Cum-Laude Students")) {
            yMax = 1522;
        }
        
        setSliderRange(yAxisMinSlider, yAxisMaxSlider, 0, yMax);
        updateSliderLabel(xAxisMinSlider, xAxisMinLabel);
        updateSliderLabel(xAxisMaxSlider, xAxisMaxLabel);
        updateSliderLabel(yAxisMinSlider, yAxisMinLabel);
        updateSliderLabel(yAxisMaxSlider, yAxisMaxLabel);
    }
    
    /**
     * Updates prediction controls visibility based on Y-axis selection
     */
    public static void updatePredictionControls(ComboBox<String> yAxisComboBox, VBox predictionControls) {
        boolean showControls = "Predicted Grade".equals(yAxisComboBox.getValue());
        predictionControls.setVisible(showControls);
        predictionControls.setManaged(showControls);
    }
    
    /**
     * Updates course correlation controls and axis options when correlation mode changes
     */
    public static void updateCourseCorrelationControls(boolean showControls,
                                                      CheckBox courseCorrelationModeCheckBox,
                                                      VBox courseCorrelationControls,
                                                      ComboBox<String> xAxisComboBox,
                                                      ComboBox<String> yAxisComboBox,
                                                      ComboBox<String> xCourseComboBox,
                                                      ComboBox<String> yCourseComboBox,
                                                      Runnable onUpdateChart,
                                                      Runnable onUpdateYAxisOptions,
                                                      Runnable onUpdatePredictionControls,
                                                      Runnable onUpdateSliderRanges) {
        courseCorrelationControls.setVisible(showControls);
        courseCorrelationControls.setManaged(showControls);
        
        if (showControls) {
            String[] courses = CurrentGradesModel.getCourses();
            String currentXSelection = xAxisComboBox.getValue();
            xAxisComboBox.getItems().clear();
            xAxisComboBox.getItems().addAll(courses);
            if (!xAxisComboBox.getItems().isEmpty()) {
                String xValue = xCourseComboBox.getValue() != null && xAxisComboBox.getItems().contains(xCourseComboBox.getValue())
                    ? xCourseComboBox.getValue()
                    : (!xAxisComboBox.getItems().contains(currentXSelection) ? xAxisComboBox.getItems().get(0) : currentXSelection);
                xAxisComboBox.setValue(xValue);
                xCourseComboBox.setValue(xValue);
            }
            
            xAxisComboBox.setOnAction(e -> {
                if (xAxisComboBox.getValue() != null) {
                    xCourseComboBox.setValue(xAxisComboBox.getValue());
                }
                onUpdateChart.run();
            });
            
            xCourseComboBox.setOnAction(e -> {
                if (xCourseComboBox.getValue() != null) {
                    xAxisComboBox.setValue(xCourseComboBox.getValue());
                }
                onUpdateChart.run();
            });
            
            yAxisComboBox.setOnAction(e -> {
                if (yAxisComboBox.getValue() != null) {
                    yCourseComboBox.setValue(yAxisComboBox.getValue());
                }
                onUpdateChart.run();
            });
            
            yCourseComboBox.setOnAction(e -> {
                if (yCourseComboBox.getValue() != null) {
                    yAxisComboBox.setValue(yCourseComboBox.getValue());
                }
                onUpdateChart.run();
            });
        } else {
            String currentXSelection = xAxisComboBox.getValue();
            xAxisComboBox.getItems().clear();
            xAxisComboBox.getItems().addAll("Per Student", "Per Course");
            xAxisComboBox.setValue(xAxisComboBox.getItems().contains(currentXSelection) ? currentXSelection : "Per Student");
            
            xAxisComboBox.setOnAction(e -> {
                if (!courseCorrelationModeCheckBox.isSelected()) {
                    onUpdateYAxisOptions.run();
                }
                onUpdatePredictionControls.run();
                onUpdateSliderRanges.run();
                onUpdateChart.run();
            });
            
            yAxisComboBox.setOnAction(e -> {
                onUpdatePredictionControls.run();
                onUpdateSliderRanges.run();
                onUpdateChart.run();
            });
        }
        
        onUpdateYAxisOptions.run();
    }
    
    // Helper classes to hold the created controls
    public static class FeatureFilterControls {
        public final VBox container;
        public final ComboBox<String> featureComboBox;
        public final ComboBox<String> valueComboBox;
        
        public FeatureFilterControls(VBox container, ComboBox<String> featureComboBox, ComboBox<String> valueComboBox) {
            this.container = container;
            this.featureComboBox = featureComboBox;
            this.valueComboBox = valueComboBox;
        }
    }
    
    public static class PredictionControls {
        public final VBox container;
        public final ComboBox<String> courseComboBox;
        public final ComboBox<String> featureComboBox;
        public final CheckBox showActualGradesCheckBox;
        
        public PredictionControls(VBox container, ComboBox<String> courseComboBox, 
                                 ComboBox<String> featureComboBox, CheckBox showActualGradesCheckBox) {
            this.container = container;
            this.courseComboBox = courseComboBox;
            this.featureComboBox = featureComboBox;
            this.showActualGradesCheckBox = showActualGradesCheckBox;
        }
    }
    
    public static class CourseCorrelationControls {
        public final VBox container;
        public final ComboBox<String> xCourseComboBox;
        public final ComboBox<String> yCourseComboBox;
        public final CheckBox showYEqualsXLineCheckBox;
        
        public CourseCorrelationControls(VBox container, ComboBox<String> xCourseComboBox,
                                       ComboBox<String> yCourseComboBox, CheckBox showYEqualsXLineCheckBox) {
            this.container = container;
            this.xCourseComboBox = xCourseComboBox;
            this.yCourseComboBox = yCourseComboBox;
            this.showYEqualsXLineCheckBox = showYEqualsXLineCheckBox;
        }
    }
}

