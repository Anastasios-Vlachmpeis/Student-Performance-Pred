package tools;

import GUI.style.UIStyling;
import datamodels.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

/**
 * Factory class for creating shared chart control components
 * Uses callbacks to handle chart-specific behavior
 * 
 * Multiple tabs (scatterplot, jointplot, histogram, barchart) need
 * the same controls - feature filters, course selection, sliders, etc. 
 * Instead of duplicating that code everywhere, we put it here. 
 * Each tab can call these methods and pass in callbacks for what should happen when things change.
 */
public class ChartControlFactory {
    
    /**
     * Creates feature filter controls (feature combo box + value combo box).
     * @param onFeatureChange Callback when feature selection changes (e.g. update value options)
     * @param onValueChange Callback when filter value changes (e.g. update chart)
     * @return FeatureFilterControls containing the controls
     */
    public static FeatureFilterControls createFeatureFilterControls(
            Runnable onFeatureChange, 
            Runnable onValueChange) {
        
        VBox controls = new VBox(10);
        
        // Feature selection combo box, "No Feature" is default
        Label featureFilterLabel = new Label("Feature Filter:");
        UIStyling.styleHeadingLabel(featureFilterLabel);
        ComboBox<String> featureFilterComboBox = new ComboBox<>();
        UIStyling.styleComboBox(featureFilterComboBox);
        featureFilterComboBox.getItems().add("No Feature");
        featureFilterComboBox.getItems().addAll(StudentInfoModel.featureNames);
        featureFilterComboBox.setValue("No Feature");
        
        // Value selection combo box - hidden until a feature is selected
        ComboBox<String> valueComboBox = new ComboBox<>();
        UIStyling.styleComboBox(valueComboBox);
        valueComboBox.setVisible(false);
        valueComboBox.setManaged(false);
        valueComboBox.setOnAction(e -> onValueChange.run());
        
        // Update value options and trigger chart update when feature changes
        featureFilterComboBox.setOnAction(e -> {
            updateFeatureFilterValueOptions(featureFilterComboBox, valueComboBox);
            onFeatureChange.run();
        });
        controls.getChildren().addAll(featureFilterLabel, featureFilterComboBox, valueComboBox);
        
        return new FeatureFilterControls(controls, featureFilterComboBox, valueComboBox);
    }
    
    //Updates feature filter value options based on the selected feature
    private static void updateFeatureFilterValueOptions(
            ComboBox<String> featureComboBox, 
            ComboBox<String> valueComboBox) {
        
        String selectedFeature = featureComboBox.getValue();
        valueComboBox.getItems().clear();
        
        // Hide value combo box if no feature is selected
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
                
                // Populate value options;
                // categories for categorical, thresholds for numerical
                if (sampleFeature instanceof CategoricalFeature) {
                    String[] categories = CategoricalFeature.getRange(featureIndex);
                    valueComboBox.getItems().addAll(categories);
                    if (categories.length > 0) {
                        valueComboBox.setValue(categories[0]);
                    }
                } else if (sampleFeature instanceof NumericalFeature) {
                    // For numerical features we calculate the median and create threshold options
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
                
                // Make value combo box visible now that it has options
                valueComboBox.setVisible(true);
                valueComboBox.setManaged(true);
            } catch (Exception e) {
                // If anything goes wrong we just hide the value combo box
                valueComboBox.setVisible(false);
                valueComboBox.setManaged(false);
            }
        }
    }
    
    // Creates course correlation controls
    // X course + Y course + show Y=X checkbox
    public static CourseCorrelationControls createCourseCorrelationControls(Runnable onChange) {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10, 0, 0, 0));
        
        String[] courses = CurrentGradesModel.getCourses();
        
        // X-axis course selection
        Label xCourseLabel = new Label("X Course:");
        UIStyling.styleHeadingLabel(xCourseLabel);
        ComboBox<String> xCourseComboBox = new ComboBox<>();
        UIStyling.styleComboBox(xCourseComboBox);
        xCourseComboBox.getItems().addAll(courses);
        if (!xCourseComboBox.getItems().isEmpty()) {
            xCourseComboBox.setValue(xCourseComboBox.getItems().get(0));
        }
        // Trigger chart update when X course changes
        xCourseComboBox.setOnAction(e -> onChange.run());
        
        // Y-axis course selection (default to second course if available)
        Label yCourseLabel = new Label("Y Course:");
        UIStyling.styleHeadingLabel(yCourseLabel);
        ComboBox<String> yCourseComboBox = new ComboBox<>();
        UIStyling.styleComboBox(yCourseComboBox);
        yCourseComboBox.getItems().addAll(courses);
        if (yCourseComboBox.getItems().size() > 1) {
            yCourseComboBox.setValue(yCourseComboBox.getItems().get(1));
        } else if (!yCourseComboBox.getItems().isEmpty()) {
            yCourseComboBox.setValue(yCourseComboBox.getItems().get(0));
        }
        // Trigger chart update when the Y course changes
        yCourseComboBox.setOnAction(e -> onChange.run());
        
        // Checkbox to show the Y=X reference line
        CheckBox showYEqualsXLineCheckBox = new CheckBox("Show Y=X Line");
        showYEqualsXLineCheckBox.setOnAction(e -> onChange.run());
        
        controls.getChildren().addAll(xCourseLabel, xCourseComboBox, yCourseLabel, yCourseComboBox, showYEqualsXLineCheckBox);
        controls.setVisible(true);
        controls.setManaged(true);
        
        return new CourseCorrelationControls(controls, xCourseComboBox, yCourseComboBox, showYEqualsXLineCheckBox);
    }
    
    //Slider with tick marks and listener that updates label and enforces min/max constraints
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
            // Enforcing of the min <= max constraint by adjusting the other slider if needed
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
    
    //Updates the slider label with the formatted value
    public static void updateSliderLabel(Slider slider, Label label) {
        label.setText(String.format("%.2f", slider.getValue()));
    }
    
    //Sets the range for min and max sliders
    public static void setSliderRange(Slider minSlider, Slider maxSlider, double min, double max) {
        minSlider.setMin(min);
        minSlider.setMax(max);
        minSlider.setValue(min);
        
        maxSlider.setMin(min);
        maxSlider.setMax(max);
        maxSlider.setValue(max);
    }
    
    //Helper class to hold feature filter controls ;
    //container, feature combo box, value combo box
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
    
    //Helper class to hold course correlation controls ;
    //container, X course combo box, Y course combo box, show Y=X checkbox
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

