package GUI.tab;

import GUI.old.JointPlotGenerator;
import javafx.geometry.Insets;
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
    private ComboBox<String> jointXAxisComboBox;
    private ComboBox<String> jointYAxisComboBox;
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
    private ComboBox<String> jointCourseComboBox;
    private ComboBox<String> jointFeatureComboBox;
    private CheckBox jointShowActualGradesCheckBox;
    private VBox jointPredictionControls;
    private ComboBox<String> jointFeatureFilterComboBox;
    private ComboBox<String> jointFeatureFilterValueComboBox;
    private VBox jointFeatureFilterControls;
    private CheckBox jointCourseCorrelationModeCheckBox;
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

    /** We create the control panel with all UI controls (filters, axes, sliders) */
    private VBox createJointControlPanel() {
        // Create the main panel container with spacing and padding
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(300);

        // We create the feature filter controls (feature selection + value selection)
        jointFeatureFilterControls = createJointFeatureFilterControls();
        
        // We create the course correlation mode checkbox - when enabled, switches axes to course selection
        jointCourseCorrelationModeCheckBox = new CheckBox("Course Correlation Mode");
        jointCourseCorrelationModeCheckBox.setSelected(false);
        // We update all dependent controls when correlation mode changes
        jointCourseCorrelationModeCheckBox.setOnAction(e -> {
            updateJointCourseCorrelationControls();
            updateJointYAxisOptions();
            updateJointSliderRanges();
            updateJointChart();
        });

        // We create the course correlation controls (X course, Y course, Y=X line checkbox)
        jointCourseCorrelationControls = createJointCourseCorrelationControls();

        // We create the X-axis selection combo box: "Per Student" or "Per Course" (or course name in correlation mode)
        Label xAxisLabel = new Label("X-Axis:");
        jointXAxisComboBox = new ComboBox<>();
        jointXAxisComboBox.getItems().addAll("Per Student", "Per Course");
        jointXAxisComboBox.setValue("Per Student");
        // We update Y-axis options only if not in correlation mode (correlation mode handles it separately)
        jointXAxisComboBox.setOnAction(e -> {
            if (!jointCourseCorrelationModeCheckBox.isSelected()) {
                updateJointYAxisOptions();
            }
            updateJointPredictionControls();
            updateJointSliderRanges();
            updateJointChart();
        });

        // Here we create the Y-axis selection combo box: metric (Mean, Mode, etc.) or course name (in correlation mode)
        Label yAxisLabel = new Label("Y-Axis:");
        jointYAxisComboBox = new ComboBox<>();
        updateJointYAxisOptions(); // We populate options based on X-axis selection
        jointYAxisComboBox.setValue("Mean");
        // We update prediction controls visibility and slider ranges, then refresh chart
        jointYAxisComboBox.setOnAction(e -> {
            updateJointPredictionControls();
            updateJointSliderRanges();
            updateJointChart();
        });

        // We create the prediction controls (course, feature, show actual checkbox) - initially hidden
        jointPredictionControls = createJointPredictionControls();

        // We create the X-axis filter sliders (min and max) with labels
        Label xFilterLabel = new Label("X-Axis Filter:");
        jointXAxisMinSlider = new Slider();
        jointXAxisMaxSlider = new Slider();
        jointXAxisMinLabel = new Label();
        jointXAxisMaxLabel = new Label();
        setupJointSlider(jointXAxisMinSlider, jointXAxisMinLabel, true);  // true = min slider
        setupJointSlider(jointXAxisMaxSlider, jointXAxisMaxLabel, false); // false = max slider

        // We create the Y-axis filter sliders (min and max) with labels
        Label yFilterLabel = new Label("Y-Axis Filter:");
        jointYAxisMinSlider = new Slider();
        jointYAxisMaxSlider = new Slider();
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
                jointCourseCorrelationModeCheckBox,
                jointCourseCorrelationControls,
                new Separator(Orientation.HORIZONTAL),
                xAxisLabel, jointXAxisComboBox,
                yAxisLabel, jointYAxisComboBox,
                jointPredictionControls,
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
     * This method updates the Y-axis combo box options based on 
     * the X-axis selection and course correlation mode
     */
    private void updateJointYAxisOptions() {
        ChartControlFactory.updateYAxisOptions(
                jointYAxisComboBox,
                jointXAxisComboBox,
                jointCourseCorrelationModeCheckBox.isSelected()
        );
    }

    /** 
     * We create course correlation controls (X course, Y course, show Y=X checkbox) 
     */
    private VBox createJointCourseCorrelationControls() {
        ChartControlFactory.CourseCorrelationControls controls = ChartControlFactory.createCourseCorrelationControls(
                () -> updateJointChart()
        );
        jointXCourseComboBox = controls.xCourseComboBox;
        jointYCourseComboBox = controls.yCourseComboBox;
        jointShowYEqualsXLineCheckBox = controls.showYEqualsXLineCheckBox;
        return controls.container;
    }

    /** 
     * Update course correlation controls visibility and axis options
     * when the correlation mode changes 
     * */
    private void updateJointCourseCorrelationControls() {
        ChartControlFactory.updateCourseCorrelationControls(
                jointCourseCorrelationModeCheckBox.isSelected(),
                jointCourseCorrelationModeCheckBox,
                jointCourseCorrelationControls,
                jointXAxisComboBox,
                jointYAxisComboBox,
                jointXCourseComboBox,
                jointYCourseComboBox,
                () -> updateJointChart(),
                () -> updateJointYAxisOptions(),
                () -> updateJointPredictionControls(),
                () -> updateJointSliderRanges()
        );
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


    /** 
     * We create the prediction controls 
     * (course combo box, feature combo box, show actual grades checkbox)
     */
    private VBox createJointPredictionControls() {
        ChartControlFactory.PredictionControls controls = ChartControlFactory.createPredictionControls(
                () -> updateJointChart()
        );
        jointCourseComboBox = controls.courseComboBox;
        jointFeatureComboBox = controls.featureComboBox;
        jointShowActualGradesCheckBox = controls.showActualGradesCheckBox;
        return controls.container;
    }

    /** Update prediction controls visibility based on Y-axis selection */
    private void updateJointPredictionControls() {
        ChartControlFactory.updatePredictionControls(jointYAxisComboBox, jointPredictionControls);
    }

    /** Update slider ranges based on axis data selections and course correlation mode */
    private void updateJointSliderRanges() {
        ChartControlFactory.updateSliderRanges(
                jointXAxisComboBox.getValue(),
                jointYAxisComboBox.getValue() == null ? "Mean" : jointYAxisComboBox.getValue(),
                jointCourseCorrelationModeCheckBox.isSelected(),
                jointXAxisMinSlider, jointXAxisMaxSlider,
                jointYAxisMinSlider, jointYAxisMaxSlider,
                jointXAxisMinLabel, jointXAxisMaxLabel,
                jointYAxisMinLabel, jointYAxisMaxLabel
        );
    }

    /** 
     * This method updates the joint plot chart based on 
     * the current axis selections, filters, and options 
     */
    private void updateJointChart() {
        if (jointXAxisComboBox == null || jointYAxisComboBox == null || jointXAxisMinSlider == null || jointXAxisMaxSlider == null ||jointYAxisMinSlider == null || jointYAxisMaxSlider == null || jointGenerator == null || jointRoot == null) {
            return;
        }

        String xAxisData = jointXAxisComboBox.getValue();
        String yAxisData = jointYAxisComboBox.getValue();

        if (xAxisData == null || yAxisData == null) {
            return;
        }

        double xMin = jointXAxisMinSlider.getValue();
        double xMax = jointXAxisMaxSlider.getValue();
        double yMin = jointYAxisMinSlider.getValue();
        double yMax = jointYAxisMaxSlider.getValue();

        // We extract prediction parameters only if Y-axis is "Predicted Grade"
        String selectedCourse = null;
        String selectedFeature = null;
        boolean showActualGrades = false;
        if ("Predicted Grade".equals(yAxisData)) {
            selectedCourse = jointCourseComboBox != null ? jointCourseComboBox.getValue() : null;
            selectedFeature = jointFeatureComboBox != null ? jointFeatureComboBox.getValue() : null;
            showActualGrades = jointShowActualGradesCheckBox != null && jointShowActualGradesCheckBox.isSelected();
        }

        // Then, we extract feature filter and convert "No Feature" to null
        String filterFeature = jointFeatureFilterComboBox != null ? jointFeatureFilterComboBox.getValue() : null;
        String filterValue = jointFeatureFilterValueComboBox != null ? jointFeatureFilterValueComboBox.getValue() : null;
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
        }

        // And we also extract the course correlation mode parameters
        boolean courseCorrelationMode = jointCourseCorrelationModeCheckBox != null && jointCourseCorrelationModeCheckBox.isSelected();
        String xCourse = courseCorrelationMode && jointXAxisComboBox.getValue() != null ? jointXAxisComboBox.getValue() : null;
        String yCourse = courseCorrelationMode && jointYAxisComboBox.getValue() != null ? jointYAxisComboBox.getValue() : null;
        boolean showYEqualsX = courseCorrelationMode && jointShowYEqualsXLineCheckBox != null && jointShowYEqualsXLineCheckBox.isSelected();

        BorderPane chart = jointGenerator.createChart(
                xAxisData, yAxisData, xMin, xMax, yMin, yMax,
                selectedCourse, selectedFeature, showActualGrades,
                filterFeature, filterValue,
                courseCorrelationMode, xCourse, yCourse, showYEqualsX
        );

        jointRoot.setCenter(chart);
    }



}
