package tools;

import datamodels.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

/**
 * "Factory" class for creating shared chart control components
 * It uses callbacks to handle chart-specific behavior
 */
public class ChartControlFactory {
    
    /**
     * We create the feature filter controls (feature combo box + value combo box)
     * @param onFeatureChange Callback when feature selection changes (e.g. update value options)
     * @param onValueChange Callback when filter value changes (e.g. update chart)
     * @return FeatureFilterControls containing the controls
     */
    public static FeatureFilterControls createFeatureFilterControls(
            Runnable onFeatureChange, 
            Runnable onValueChange) {
        
        VBox controls = new VBox(10);
        
        // We create the feature selection combo box with "No Feature" as the default option
        Label featureFilterLabel = new Label("Feature Filter:");
        ComboBox<String> featureFilterComboBox = new ComboBox<>();
        featureFilterComboBox.getItems().add("No Feature");
        featureFilterComboBox.getItems().addAll(StudentInfoModel.featureNames);
        featureFilterComboBox.setValue("No Feature");
        
        // We create the value selection combo box - hidden initially until a feature is selected
        ComboBox<String> valueComboBox = new ComboBox<>();
        valueComboBox.setVisible(false);
        valueComboBox.setManaged(false);
        valueComboBox.setOnAction(e -> onValueChange.run());
        
        // Here we update value options and trigger chart update when feature selection changes
        featureFilterComboBox.setOnAction(e -> {
            updateFeatureFilterValueOptions(featureFilterComboBox, valueComboBox);
            onFeatureChange.run();
        });
        
        controls.getChildren().addAll(featureFilterLabel, featureFilterComboBox, valueComboBox);
        
        return new FeatureFilterControls(controls, featureFilterComboBox, valueComboBox);
    }
    
    /** Updates feature filter value options based on selected feature */
    private static void updateFeatureFilterValueOptions(
            ComboBox<String> featureComboBox, 
            ComboBox<String> valueComboBox) {
        
        String selectedFeature = featureComboBox.getValue();
        valueComboBox.getItems().clear();
        
        // Here we hide the value combo box if no feature is selected
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
                
                // Here we populate value options based on feature type: categories for categorical, thresholds for numerical
                if (sampleFeature instanceof CategoricalFeature) {
                    String[] categories = CategoricalFeature.getRange(featureIndex);
                    valueComboBox.getItems().addAll(categories);
                    if (categories.length > 0) {
                        valueComboBox.setValue(categories[0]);
                    }
                } else if (sampleFeature instanceof NumericalFeature) {
                    // For numerical features, we calculate the median and create threshold options
                    double min = NumericalFeature.getRangeMin(featureIndex);
                    double max = NumericalFeature.getRangeMax(featureIndex);
                    double median = (min + max) / 2.0;
                    // We add options for "Above median", "Below median", and "All"
                    valueComboBox.getItems().addAll(
                        "Above " + String.format("%.2f", median),
                        "Below " + String.format("%.2f", median),
                        "All"
                    );
                    valueComboBox.setValue("All");
                }
                
                // We make the value combo box visible and managed now that it has options
                valueComboBox.setVisible(true);
                valueComboBox.setManaged(true);
            } catch (Exception e) {
                // If anything goes wrong, we hide the value combo box
                valueComboBox.setVisible(false);
                valueComboBox.setManaged(false);
            }
        }
    }
    
    /** 
     * Create the prediction controls (course + feature combo boxes + show the checkbox) 
     */
    public static PredictionControls createPredictionControls(Runnable onChange) {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        
        // We create the course selection combo box for prediction
        Label courseLabel = new Label("Course:");
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll(CurrentGradesModel.getCourses());
        if (!courseComboBox.getItems().isEmpty()) {
            courseComboBox.setValue(courseComboBox.getItems().get(0));
        }
        courseComboBox.setOnAction(e -> onChange.run());
        
        // We create the feature selection combo box for prediction (decision stump feature)
        Label featureLabel = new Label("Feature:");
        ComboBox<String> featureComboBox = new ComboBox<>();
        featureComboBox.getItems().addAll(StudentInfoModel.featureNames);
        if (!featureComboBox.getItems().isEmpty()) {
            featureComboBox.setValue(featureComboBox.getItems().get(0));
        }
        featureComboBox.setOnAction(e -> onChange.run());
        
        // We create the checkbox to show actual grades alongside predicted grades
        CheckBox showActualGradesCheckBox = new CheckBox("Show Actual Grades");
        showActualGradesCheckBox.setOnAction(e -> onChange.run());
        
        controls.getChildren().addAll(courseLabel, courseComboBox, featureLabel, featureComboBox, showActualGradesCheckBox);
        // Here we hide the controls initially - they're shown only when Y-axis is "Predicted Grade"
        controls.setVisible(false);
        controls.setManaged(false);
        
        return new PredictionControls(controls, courseComboBox, featureComboBox, showActualGradesCheckBox);
    }
    
    /** Creates course correlation controls (X course + Y course + show Y=X checkbox) */
    public static CourseCorrelationControls createCourseCorrelationControls(Runnable onChange) {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        
        String[] courses = CurrentGradesModel.getCourses();
        
        // We create the X-axis course selection combo box
        Label xCourseLabel = new Label("X Course:");
        ComboBox<String> xCourseComboBox = new ComboBox<>();
        xCourseComboBox.getItems().addAll(courses);
        if (!xCourseComboBox.getItems().isEmpty()) {
            xCourseComboBox.setValue(xCourseComboBox.getItems().get(0));
        }
        // Add event handler to trigger chart update when X course changes
        xCourseComboBox.setOnAction(e -> onChange.run());
        
        // We create the Y-axis course selection combo box (default to second course if available)
        Label yCourseLabel = new Label("Y Course:");
        ComboBox<String> yCourseComboBox = new ComboBox<>();
        yCourseComboBox.getItems().addAll(courses);
        if (yCourseComboBox.getItems().size() > 1) {
            yCourseComboBox.setValue(yCourseComboBox.getItems().get(1));
        } else if (!yCourseComboBox.getItems().isEmpty()) {
            yCourseComboBox.setValue(yCourseComboBox.getItems().get(0));
        }
        // Add event handler to trigger chart update when Y course changes
        yCourseComboBox.setOnAction(e -> onChange.run());
        
        // We create the checkbox to show Y=X reference line (rendered below data points)
        CheckBox showYEqualsXLineCheckBox = new CheckBox("Show Y=X Line");
        showYEqualsXLineCheckBox.setOnAction(e -> onChange.run());
        
        controls.getChildren().addAll(xCourseLabel, xCourseComboBox, yCourseLabel, yCourseComboBox, showYEqualsXLineCheckBox);
        // Course correlation controls are always visible now
        controls.setVisible(true);
        controls.setManaged(true);
        
        return new CourseCorrelationControls(controls, xCourseComboBox, yCourseComboBox, showYEqualsXLineCheckBox);
    }
    
    /** 
     * Update the Y-axis options based on X-axis selection and course correlation mode 
     */
    public static void updateYAxisOptions(
            ComboBox<String> yAxisComboBox,
            ComboBox<String> xAxisComboBox,
            boolean courseCorrelationMode) {
        
        String currentSelection = yAxisComboBox.getValue();
        yAxisComboBox.getItems().clear();
        
        // Here we show courses as Y-axis options if course correlation mode is enabled
        if (courseCorrelationMode) {
            yAxisComboBox.getItems().addAll(CurrentGradesModel.getCourses());
            if (!yAxisComboBox.getItems().contains(currentSelection)) {
                yAxisComboBox.setValue(yAxisComboBox.getItems().get(0));
            } else {
                yAxisComboBox.setValue(currentSelection);
            }
            return;
        }
        
        // Here we add base Y-axis options and X-axis-specific options
        yAxisComboBox.getItems().addAll("Mean", "Mode", "Median", "Number of NGs");
        if (xAxisComboBox.getValue().equals("Per Student")) {
            yAxisComboBox.getItems().add("Predicted Grade");
        } else if (xAxisComboBox.getValue().equals("Per Course")) {
            yAxisComboBox.getItems().addAll("Number of Passing Students", "Number of Non-Passing Students", "Number of Cum-Laude Students");
        }
        
        yAxisComboBox.setValue(yAxisComboBox.getItems().contains(currentSelection) ? currentSelection : yAxisComboBox.getItems().get(0));
    }
    
    /** 
     * Set up a slider with tick marks and a listener that updates label and enforces min/max constraints 
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
            // Here we enforce the min <= max constraint by adjusting the other slider if needed
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
     * Update the slider label with the formatted value 
     */
    public static void updateSliderLabel(Slider slider, Label label) {
        label.setText(String.format("%.2f", slider.getValue()));
    }
    
    /** 
     * Set the range for min and max sliders 
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
     * Update the slider ranges based on axis data and course correlation mode 
     */
    public static void updateSliderRanges(String xAxisData, String yAxisData, boolean courseCorrelationMode,
                                          Slider xAxisMinSlider, Slider xAxisMaxSlider,
                                          Slider yAxisMinSlider, Slider yAxisMaxSlider,
                                          Label xAxisMinLabel, Label xAxisMaxLabel,
                                          Label yAxisMinLabel, Label yAxisMaxLabel) {
        // Here we set both axes to grade range [0, 10] if course correlation mode is enabled
        if (courseCorrelationMode) {
            setSliderRange(xAxisMinSlider, xAxisMaxSlider, 0, 10);
            setSliderRange(yAxisMinSlider, yAxisMaxSlider, 0, 10);
            updateSliderLabel(xAxisMinSlider, xAxisMinLabel);
            updateSliderLabel(xAxisMaxSlider, xAxisMaxLabel);
            updateSliderLabel(yAxisMinSlider, yAxisMinLabel);
            updateSliderLabel(yAxisMaxSlider, yAxisMaxLabel);
            return;
        }
        
        // Here we set X-axis range based on data type
        if (xAxisData.equals("Per Student")) {
            setSliderRange(xAxisMinSlider, xAxisMaxSlider, 0, 1522);
        } else {
            setSliderRange(xAxisMinSlider, xAxisMaxSlider, 0, 35);
        }
        
        // Here we set Y-axis range based on Y-axis data type
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
     * Update the prediction controls visibility based on Y-axis selection 
     */
    public static void updatePredictionControls(ComboBox<String> yAxisComboBox, VBox predictionControls) {
        // Here we show prediction controls only when Y-axis is "Predicted Grade"
        boolean showControls = "Predicted Grade".equals(yAxisComboBox.getValue());
        predictionControls.setVisible(showControls);
        predictionControls.setManaged(showControls);
    }
    
    /** 
     * Update the course correlation controls and axis options when correlation mode changes 
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
            // Here we replace axis options with courses when correlation mode is enabled
            String[] courses = CurrentGradesModel.getCourses();
            String currentXSelection = xAxisComboBox.getValue();
            xAxisComboBox.getItems().clear();
            xAxisComboBox.getItems().addAll(courses);
            if (!xAxisComboBox.getItems().isEmpty()) {
                // We try to preserve the current selection, or use X-course value, or default to first course
                String xValue = xCourseComboBox.getValue() != null && xAxisComboBox.getItems().contains(xCourseComboBox.getValue())
                    ? xCourseComboBox.getValue()
                    : (!xAxisComboBox.getItems().contains(currentXSelection) ? xAxisComboBox.getItems().get(0) : currentXSelection);
                xAxisComboBox.setValue(xValue);
                xCourseComboBox.setValue(xValue);
            }
            
            // Here we sync X-axis and X-course combo boxes bidirectionally
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
            
            // Here we sync Y-axis and Y-course combo boxes bidirectionally
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
            // Here we restore standard axis options when correlation mode is disabled
            String currentXSelection = xAxisComboBox.getValue();
            xAxisComboBox.getItems().clear();
            xAxisComboBox.getItems().addAll("Per Student", "Per Course");
            // We preserve the current selection if valid, otherwise default to "Per Student"
            xAxisComboBox.setValue(xAxisComboBox.getItems().contains(currentXSelection) ? currentXSelection : "Per Student");
            
            // We restore the standard X-axis event handler
            xAxisComboBox.setOnAction(e -> {
                if (!courseCorrelationModeCheckBox.isSelected()) {
                    onUpdateYAxisOptions.run();
                }
                onUpdatePredictionControls.run();
                onUpdateSliderRanges.run();
                onUpdateChart.run();
            });
            
            // We restore the standard Y-axis event handler
            yAxisComboBox.setOnAction(e -> {
                onUpdatePredictionControls.run();
                onUpdateSliderRanges.run();
                onUpdateChart.run();
            });
        }
        
        onUpdateYAxisOptions.run();
    }
    
    /** 
     * Helper classes to hold the created controls and make them easy to access
     * We use this class to hold feature filter controls 
     * (container, feature combo box, value combo box)
     */
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
    
    /** 
     * We use this class to hold prediction controls (container, course combo box, feature combo box, show actual checkbox)
     */
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
    
    /** 
     * We use this class to hold course correlation controls (container, X course combo box, Y course combo box, show Y=X checkbox)
     */
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

