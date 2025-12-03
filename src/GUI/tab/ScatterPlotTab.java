package GUI.tab;

import GUI.chart.ScatterPlotGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tools.ChartControlFactory;


/**
 * This class creates the scatter plot tab UI,
 * with the control panel and the chart
 */
public class ScatterPlotTab {

    // ScatterPlot fields
    private ComboBox<String> scatterXAxisComboBox;
    private ComboBox<String> scatterYAxisComboBox;
    private Slider scatterXAxisMinSlider;
    private Slider scatterXAxisMaxSlider;
    private Slider scatterYAxisMinSlider;
    private Slider scatterYAxisMaxSlider;
    private Label scatterXAxisMinLabel;
    private Label scatterXAxisMaxLabel;
    private Label scatterYAxisMinLabel;
    private Label scatterYAxisMaxLabel;
    private BorderPane scatterRoot;
    private ScatterPlotGenerator scatterGenerator;
    private ComboBox<String> scatterCourseComboBox;
    private ComboBox<String> scatterFeatureComboBox;
    private CheckBox scatterShowActualGradesCheckBox;
    private VBox scatterPredictionControls;
    private ComboBox<String> scatterFeatureFilterComboBox;
    private ComboBox<String> scatterFeatureFilterValueComboBox;
    private VBox scatterFeatureFilterControls;
    private CheckBox scatterCourseCorrelationModeCheckBox;
    private ComboBox<String> scatterXCourseComboBox;
    private ComboBox<String> scatterYCourseComboBox;
    private CheckBox scatterShowYEqualsXLineCheckBox;
    private VBox scatterCourseCorrelationControls;



    /** 
     * We create and return the scatter plot tab UI with control panel and chart 
     */
    public BorderPane createScatterPlotTab() {
        scatterGenerator = new ScatterPlotGenerator();
        scatterRoot = new BorderPane();
        // scrollable wrapping
        scatterRoot.setLeft(new ScrollPane(createScatterControlPanel()));
        updateScatterChart();
        return scatterRoot;
    }

    /** 
     * We create the control panel with all the necessary UI controls (filters, axes, sliders) 
     */
    private VBox createScatterControlPanel() {
        // Create the main panel container with spacing and padding
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(300);

        // We create the feature filter controls (feature selection + value selection)
        scatterFeatureFilterControls = createScatterFeatureFilterControls();
        
        // We create the course correlation mode checkbox - when enabled, switches axes to course selection
        scatterCourseCorrelationModeCheckBox = new CheckBox("Course Correlation Mode");
        scatterCourseCorrelationModeCheckBox.setSelected(false);
        // We update all dependent controls when correlation mode changes
        scatterCourseCorrelationModeCheckBox.setOnAction(e -> {
            updateScatterCourseCorrelationControls();
            updateScatterYAxisOptions();
            updateScatterSliderRanges();
            updateScatterChart();
        });

        // We create the course correlation controls (X course, Y course, Y=X line checkbox)
        scatterCourseCorrelationControls = createScatterCourseCorrelationControls();

        Label xAxisLabel = new Label("X-Axis:");
        scatterXAxisComboBox = new ComboBox<>();
        scatterXAxisComboBox.getItems().addAll("Per Student", "Per Course");
        scatterXAxisComboBox.setValue("Per Student");
        scatterXAxisComboBox.setOnAction(e -> {
            if (!scatterCourseCorrelationModeCheckBox.isSelected()) {
                updateScatterYAxisOptions();
            }
            updateScatterPredictionControls();
            updateScatterSliderRanges();
            updateScatterChart();
        });

        Label yAxisLabel = new Label("Y-Axis:");
        scatterYAxisComboBox = new ComboBox<>();
        updateScatterYAxisOptions();
        scatterYAxisComboBox.setValue("Mean");
        scatterYAxisComboBox.setOnAction(e -> {
            updateScatterPredictionControls();
            updateScatterSliderRanges();
            updateScatterChart();
        });

        scatterPredictionControls = createScatterPredictionControls();

        // We create the X-axis filter sliders (min and max) with labels
        Label xFilterLabel = new Label("X-Axis Filter:");
        scatterXAxisMinSlider = new Slider();
        scatterXAxisMaxSlider = new Slider();
        scatterXAxisMinLabel = new Label();
        scatterXAxisMaxLabel = new Label();
        setupScatterSlider(scatterXAxisMinSlider, scatterXAxisMinLabel, true);  // true = min slider
        setupScatterSlider(scatterXAxisMaxSlider, scatterXAxisMaxLabel, false); // false = max slider

        // We create the Y-axis filter sliders (min and max) with labels
        Label yFilterLabel = new Label("Y-Axis Filter:");
        scatterYAxisMinSlider = new Slider();
        scatterYAxisMaxSlider = new Slider();
        scatterYAxisMinLabel = new Label();
        scatterYAxisMaxLabel = new Label();
        setupScatterSlider(scatterYAxisMinSlider, scatterYAxisMinLabel, true);  // true = min slider
        setupScatterSlider(scatterYAxisMaxSlider, scatterYAxisMaxLabel, false); // false = max slider
        
        // We initialize slider ranges (chart update happens in createScatterPlotTab)
        updateScatterSliderRanges();

        // And we add all the controls to the panel in order,
        // with separators between sections, to make it look nice and organized
        panel.getChildren().addAll(
                scatterFeatureFilterControls,
                new Separator(Orientation.HORIZONTAL),
                scatterCourseCorrelationModeCheckBox,
                scatterCourseCorrelationControls,
                new Separator(Orientation.HORIZONTAL),
                xAxisLabel, scatterXAxisComboBox,
                yAxisLabel, scatterYAxisComboBox,
                scatterPredictionControls,
                new Separator(Orientation.HORIZONTAL),
                xFilterLabel, scatterXAxisMinLabel, scatterXAxisMinSlider, scatterXAxisMaxLabel, scatterXAxisMaxSlider,
                new Separator(Orientation.HORIZONTAL),
                yFilterLabel, scatterYAxisMinLabel, scatterYAxisMinSlider, scatterYAxisMaxLabel, scatterYAxisMaxSlider
        );

        return panel;
    }

    /** Sets up a slider with factory method, linking it to chart updates */
    private void setupScatterSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(slider, label, isMin,
                scatterXAxisMinSlider, scatterXAxisMaxSlider,
                scatterYAxisMinSlider, scatterYAxisMaxSlider,
                () -> updateScatterChart());
    }

    /**
     * Update the Y-axis combo box options based on X-axis selection and course correlation mode 
     */
    private void updateScatterYAxisOptions() {
        ChartControlFactory.updateYAxisOptions(
                scatterYAxisComboBox,
                scatterXAxisComboBox,
                scatterCourseCorrelationModeCheckBox.isSelected()
        );
    }

    /** 
     * Create the course correlation controls (X course, Y course, show Y=X checkbox) 
     */
    private VBox createScatterCourseCorrelationControls() {
        ChartControlFactory.CourseCorrelationControls controls = ChartControlFactory.createCourseCorrelationControls(
                () -> updateScatterChart()
        );
        scatterXCourseComboBox = controls.xCourseComboBox;
        scatterYCourseComboBox = controls.yCourseComboBox;
        scatterShowYEqualsXLineCheckBox = controls.showYEqualsXLineCheckBox;
        return controls.container;
    }

    /** 
     * Update the course correlation controls visibility and axis options
     * when correlation mode changes 
     */
    private void updateScatterCourseCorrelationControls() {
        ChartControlFactory.updateCourseCorrelationControls(
                scatterCourseCorrelationModeCheckBox.isSelected(),
                scatterCourseCorrelationModeCheckBox,
                scatterCourseCorrelationControls,
                scatterXAxisComboBox,
                scatterYAxisComboBox,
                scatterXCourseComboBox,
                scatterYCourseComboBox,
                () -> updateScatterChart(),
                () -> updateScatterYAxisOptions(),
                () -> updateScatterPredictionControls(),
                () -> updateScatterSliderRanges()
        );
    }

    /** 
     * Create the feature filter controls (feature combo box and value combo box) 
     */
    private VBox createScatterFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateScatterChart(),
                () -> updateScatterChart()
        );
        scatterFeatureFilterComboBox = controls.featureComboBox;
        scatterFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }


    /** 
     * Create the prediction controls (course combo box, feature combo box, show actual grades checkbox) 
     */
    private VBox createScatterPredictionControls() {
        ChartControlFactory.PredictionControls controls = ChartControlFactory.createPredictionControls(
                () -> updateScatterChart()
        );
        scatterCourseComboBox = controls.courseComboBox;
        scatterFeatureComboBox = controls.featureComboBox;
        scatterShowActualGradesCheckBox = controls.showActualGradesCheckBox;
        return controls.container;
    }

    /** 
     * Update the prediction controls visibility based on Y-axis selection 
     */
    private void updateScatterPredictionControls() {
        ChartControlFactory.updatePredictionControls(scatterYAxisComboBox, scatterPredictionControls);
    }

    /** 
     * Update the slider ranges based on axis data selections and course correlation mode 
     */
    private void updateScatterSliderRanges() {
        ChartControlFactory.updateSliderRanges(
                scatterXAxisComboBox.getValue(),
                scatterYAxisComboBox.getValue(),
                scatterCourseCorrelationModeCheckBox.isSelected(),
                scatterXAxisMinSlider, scatterXAxisMaxSlider,
                scatterYAxisMinSlider, scatterYAxisMaxSlider,
                scatterXAxisMinLabel, scatterXAxisMaxLabel,
                scatterYAxisMinLabel, scatterYAxisMaxLabel
        );
    }

    /** 
     * Update the scatter plot chart based on current axis selections, filters, and options 
     */
    private void updateScatterChart() {
        String xAxisData = scatterXAxisComboBox.getValue();
        String yAxisData = scatterYAxisComboBox.getValue();

        double xMin = scatterXAxisMinSlider.getValue();
        double xMax = scatterXAxisMaxSlider.getValue();
        double yMin = scatterYAxisMinSlider.getValue();
        double yMax = scatterYAxisMaxSlider.getValue();

        // Here we extract prediction parameters only if Y-axis is "Predicted Grade"
        String selectedCourse = null;
        String selectedFeature = null;
        boolean showActualGrades = false;
        if ("Predicted Grade".equals(yAxisData)) {
            selectedCourse = scatterCourseComboBox.getValue();
            selectedFeature = scatterFeatureComboBox.getValue();
            showActualGrades = scatterShowActualGradesCheckBox.isSelected();
        }

        // Here we extract feature filter and convert "No Feature" to null
        String filterFeature = scatterFeatureFilterComboBox.getValue();
        String filterValue = scatterFeatureFilterValueComboBox.getValue();
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
        }

        // Here we extract course correlation mode parameters
        boolean courseCorrelationMode = scatterCourseCorrelationModeCheckBox.isSelected();
        String xCourse = courseCorrelationMode ? scatterXAxisComboBox.getValue() : null;
        String yCourse = courseCorrelationMode ? scatterYAxisComboBox.getValue() : null;
        boolean showYEqualsX = courseCorrelationMode && scatterShowYEqualsXLineCheckBox.isSelected();

        ScatterChart<Number, Number> chart = scatterGenerator.createChart(
                xAxisData, yAxisData, xMin, xMax, yMin, yMax,
                selectedCourse, selectedFeature, showActualGrades,
                filterFeature, filterValue,
                courseCorrelationMode, xCourse, yCourse, showYEqualsX
        );

        scatterRoot.setCenter(chart);
    }


}
