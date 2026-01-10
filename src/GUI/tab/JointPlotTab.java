package GUI.tab;

import GUI.chart.JointPlotGenerator;
import GUI.style.UIStyling;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tools.ChartControlFactory;

/**
 * This class creates the joint plot tab UI,
 * with the control panel and the chart
 */
public class JointPlotTab {

    // JointPlot fields
    private Slider jointXAxisMinSlider;
    private Slider jointXAxisMaxSlider;
    private Slider jointYAxisMinSlider;
    private Slider jointYAxisMaxSlider;
    private Label jointXAxisMinLabel;
    private Label jointXAxisMaxLabel;
    private Label jointYAxisMinLabel;
    private Label jointYAxisMaxLabel;
    private BorderPane jointRoot;
    private JointPlotGenerator jointGenerator;
    private ComboBox<String> jointFeatureFilterComboBox;
    private ComboBox<String> jointFeatureFilterValueComboBox;
    private VBox jointFeatureFilterControls;
    private ComboBox<String> jointXCourseComboBox;
    private ComboBox<String> jointYCourseComboBox;
    private CheckBox jointShowYEqualsXLineCheckBox;
    private VBox jointCourseCorrelationControls;


    /** 
     * We create and return the joint plot tab UI with control panel and chart 
     */
    public BorderPane createJointPlotTab() {
        jointGenerator = new JointPlotGenerator();
        jointRoot = new BorderPane();
        jointRoot.setLeft(new ScrollPane(createJointControlPanel()));
        // updateJointChart() is called at the end of createJointControlPanel() via updateJointSliderRanges()
        return jointRoot;
    }

    /** We create the control panel with all UI controls (filters, course selection, sliders) */
    private VBox createJointControlPanel() {
        // Create the main panel container with spacing and padding
        VBox panel = new VBox();
        UIStyling.styleControlPanel(panel);

        // We create the feature filter controls (feature selection + value selection)
        jointFeatureFilterControls = createJointFeatureFilterControls();
        
        // We create the course correlation controls (X course, Y course, Y=X line checkbox)
        Label correlationLabel = new Label("Course Correlation:");
        UIStyling.styleHeadingLabel(correlationLabel);
        jointCourseCorrelationControls = createJointCourseCorrelationControls();

        // We create the X-axis filter sliders (min and max) with labels
        Label xFilterLabel = new Label("X-Axis Filter (Grade Range):");
        UIStyling.styleHeadingLabel(xFilterLabel);
        jointXAxisMinSlider = new Slider();
        UIStyling.styleSlider(jointXAxisMinSlider);
        jointXAxisMaxSlider = new Slider();
        UIStyling.styleSlider(jointXAxisMaxSlider);
        jointXAxisMinLabel = new Label();
        jointXAxisMaxLabel = new Label();
        setupJointSlider(jointXAxisMinSlider, jointXAxisMinLabel, true);  // true = min slider
        setupJointSlider(jointXAxisMaxSlider, jointXAxisMaxLabel, false); // false = max slider

        // We create the Y-axis filter sliders (min and max) with labels
        Label yFilterLabel = new Label("Y-Axis Filter (Grade Range):");
        UIStyling.styleHeadingLabel(yFilterLabel);
        jointYAxisMinSlider = new Slider();
        UIStyling.styleSlider(jointYAxisMinSlider);
        jointYAxisMaxSlider = new Slider();
        UIStyling.styleSlider(jointYAxisMaxSlider);
        jointYAxisMinLabel = new Label();
        jointYAxisMaxLabel = new Label();
        setupJointSlider(jointYAxisMinSlider, jointYAxisMinLabel, true);  // true = min slider
        setupJointSlider(jointYAxisMaxSlider, jointYAxisMaxLabel, false); // false = max slider
        
        // We initialize slider ranges and generate the initial chart
        updateJointSliderRanges();
        updateJointChart();

        // And let's not forget to add all the controls to the panel in order,
        // with separators between sections, so it looks nice and organized
        panel.getChildren().addAll(
                jointFeatureFilterControls,
                new Separator(Orientation.HORIZONTAL),
                correlationLabel,
                jointCourseCorrelationControls,
                new Separator(Orientation.HORIZONTAL),
                xFilterLabel, jointXAxisMinLabel, jointXAxisMinSlider, 
                jointXAxisMaxLabel, jointXAxisMaxSlider,
                new Separator(Orientation.HORIZONTAL),
                yFilterLabel, jointYAxisMinLabel, jointYAxisMinSlider, 
                jointYAxisMaxLabel, jointYAxisMaxSlider
        );

        // finish scroll wrapping
        return panel;
    }

    /**
     * Set up a slider with factory method, linking it to chart updates 
     */
    private void setupJointSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(slider, label, isMin,
                jointXAxisMinSlider, jointXAxisMaxSlider,
                jointYAxisMinSlider, jointYAxisMaxSlider,
                () -> updateJointChart());
    }

    /** 
     * We create course correlation controls (X course, Y course, show Y=X checkbox) 
     */
    private VBox createJointCourseCorrelationControls() {
        ChartControlFactory.CourseCorrelationControls controls = ChartControlFactory.createCourseCorrelationControls(
                () -> {
                    updateJointSliderRanges();
                    updateJointChart();
                }
        );
        jointXCourseComboBox = controls.xCourseComboBox;
        jointYCourseComboBox = controls.yCourseComboBox;
        jointShowYEqualsXLineCheckBox = controls.showYEqualsXLineCheckBox;
        return controls.container;
    }

    /** 
     * We create feature filter controls (feature combo box and value combo box) 
     */
    private VBox createJointFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateJointChart(),
                () -> updateJointChart()
        );
        jointFeatureFilterComboBox = controls.featureComboBox;
        jointFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }


    /** Update slider ranges for course correlation mode (grade range 0-10) */
    private void updateJointSliderRanges() {
        // For course correlation, we always use grade range 0-10
        jointXAxisMinSlider.setMin(0);
        jointXAxisMinSlider.setMax(10);
        jointXAxisMinSlider.setValue(0);
        jointXAxisMaxSlider.setMin(0);
        jointXAxisMaxSlider.setMax(10);
        jointXAxisMaxSlider.setValue(10);
        
        jointYAxisMinSlider.setMin(0);
        jointYAxisMinSlider.setMax(10);
        jointYAxisMinSlider.setValue(0);
        jointYAxisMaxSlider.setMin(0);
        jointYAxisMaxSlider.setMax(10);
        jointYAxisMaxSlider.setValue(10);
        
        // Update labels
        jointXAxisMinLabel.setText(String.format("Min: %.1f", jointXAxisMinSlider.getValue()));
        jointXAxisMaxLabel.setText(String.format("Max: %.1f", jointXAxisMaxSlider.getValue()));
        jointYAxisMinLabel.setText(String.format("Min: %.1f", jointYAxisMinSlider.getValue()));
        jointYAxisMaxLabel.setText(String.format("Max: %.1f", jointYAxisMaxSlider.getValue()));
    }

    /** 
     * This method updates the joint plot chart based on 
     * course correlation selections, filters, and options 
     */
    private void updateJointChart() {
        if (jointXAxisMinSlider == null || jointXAxisMaxSlider == null || jointYAxisMinSlider == null || jointYAxisMaxSlider == null || jointGenerator == null || jointRoot == null) {
            return;
        }

        double xMin = jointXAxisMinSlider.getValue();
        double xMax = jointXAxisMaxSlider.getValue();
        double yMin = jointYAxisMinSlider.getValue();
        double yMax = jointYAxisMaxSlider.getValue();

        // Extract feature filter and convert "No Feature" to null
        String filterFeature = jointFeatureFilterComboBox != null ? jointFeatureFilterComboBox.getValue() : null;
        String filterValue = jointFeatureFilterValueComboBox != null ? jointFeatureFilterValueComboBox.getValue() : null;
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
        }

        // Extract course correlation parameters
        String xCourse = jointXCourseComboBox != null ? jointXCourseComboBox.getValue() : null;
        String yCourse = jointYCourseComboBox != null ? jointYCourseComboBox.getValue() : null;
        boolean showYEqualsX = jointShowYEqualsXLineCheckBox != null && jointShowYEqualsXLineCheckBox.isSelected();

        BorderPane chart = jointGenerator.createChart(
                xMin, xMax, yMin, yMax,
                filterFeature, filterValue,
                xCourse, yCourse, showYEqualsX
        );

        jointRoot.setCenter(chart);
    }



}
