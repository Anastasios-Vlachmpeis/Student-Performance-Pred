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


    // We create and return the joint plot tab UI with control panel and chart 
    public BorderPane createJointPlotTab() {
        jointGenerator = new JointPlotGenerator();
        jointRoot = new BorderPane();
        jointRoot.setLeft(new ScrollPane(createJointControlPanel()));
        // updateJointChart() is called at end of createJointControlPanel() via updateJointSliderRanges()
        return jointRoot;
    }

    //The control panel with all UI controls;
    //filters, course selection, sliders
    private VBox createJointControlPanel() {
        // Main panel container
        VBox panel = new VBox();
        UIStyling.styleControlPanel(panel);

        // Feature filter controls
        jointFeatureFilterControls = createJointFeatureFilterControls();
        
        // Course correlation controls ;
        // X course, Y course, Y=X line checkbox
        Label correlationLabel = new Label("Course Correlation:");
        UIStyling.styleHeadingLabel(correlationLabel);
        jointCourseCorrelationControls = createJointCourseCorrelationControls();

        // X-axis filter sliders
        Label xFilterLabel = new Label("X-Axis Filter (Grade Range):");
        UIStyling.styleHeadingLabel(xFilterLabel);
        jointXAxisMinSlider = new Slider();
        UIStyling.styleSlider(jointXAxisMinSlider);
        jointXAxisMaxSlider = new Slider();
        UIStyling.styleSlider(jointXAxisMaxSlider);
        jointXAxisMinLabel = new Label();
        jointXAxisMaxLabel = new Label();
        setupJointSlider(jointXAxisMinSlider, jointXAxisMinLabel, true);
        setupJointSlider(jointXAxisMaxSlider, jointXAxisMaxLabel, false);

        // Y-axis filter sliders
        Label yFilterLabel = new Label("Y-Axis Filter (Grade Range):");
        UIStyling.styleHeadingLabel(yFilterLabel);
        jointYAxisMinSlider = new Slider();
        UIStyling.styleSlider(jointYAxisMinSlider);
        jointYAxisMaxSlider = new Slider();
        UIStyling.styleSlider(jointYAxisMaxSlider);
        jointYAxisMinLabel = new Label();
        jointYAxisMaxLabel = new Label();
        setupJointSlider(jointYAxisMinSlider, jointYAxisMinLabel, true);
        setupJointSlider(jointYAxisMaxSlider, jointYAxisMaxLabel, false);
        
        // Initialization of slider ranges and generation of the chart
        updateJointSliderRanges();
        updateJointChart();

        // Add controls to panel with separators
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

        return panel;
    }

    /**
     * Slider setup using the factory method
     * It is directly linked to different chart updates
     */
    private void setupJointSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(slider, label, isMin,
                jointXAxisMinSlider, jointXAxisMaxSlider,
                jointYAxisMinSlider, jointYAxisMaxSlider,
                () -> updateJointChart());
    }

    // Course correlation controls ;
    // X course, Y course, Y=X checkbox
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

    // Feature filter controls ;
    // feature combo box and value combo box
    private VBox createJointFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateJointChart(),
                () -> updateJointChart()
        );
        jointFeatureFilterComboBox = controls.featureComboBox;
        jointFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }

    // Slider ranges' update for course correlation
    private void updateJointSliderRanges() {
        // Course correlation always uses a grade range of 0-10
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
        
        // Label updates
        jointXAxisMinLabel.setText(String.format("Min: %.1f", jointXAxisMinSlider.getValue()));
        jointXAxisMaxLabel.setText(String.format("Max: %.1f", jointXAxisMaxSlider.getValue()));
        jointYAxisMinLabel.setText(String.format("Min: %.1f", jointYAxisMinSlider.getValue()));
        jointYAxisMaxLabel.setText(String.format("Max: %.1f", jointYAxisMaxSlider.getValue()));
    }

    // Updates the joint plot chart based on selections, filters, and options
    private void updateJointChart() {
        if (jointXAxisMinSlider == null || jointXAxisMaxSlider == null || jointYAxisMinSlider == null || jointYAxisMaxSlider == null || jointGenerator == null || jointRoot == null) {
            return;
        }

        double xMin = jointXAxisMinSlider.getValue();
        double xMax = jointXAxisMaxSlider.getValue();
        double yMin = jointYAxisMinSlider.getValue();
        double yMax = jointYAxisMaxSlider.getValue();

        // Extract feature filter, convert "No Feature" to null
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
