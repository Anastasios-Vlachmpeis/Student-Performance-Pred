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
    private ComboBox<String> scatterFeatureFilterComboBox;
    private ComboBox<String> scatterFeatureFilterValueComboBox;
    private VBox scatterFeatureFilterControls;
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
     * We create the control panel with all the necessary UI controls (filters, course selection, sliders) 
     */
    private VBox createScatterControlPanel() {
        // Create the main panel container with spacing and padding
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(300);

        // We create the feature filter controls (feature selection + value selection)
        scatterFeatureFilterControls = createScatterFeatureFilterControls();
        
        // We create the course correlation controls (X course, Y course, Y=X line checkbox)
        Label correlationLabel = new Label("Course Correlation:");
        scatterCourseCorrelationControls = createScatterCourseCorrelationControls();

        // We create the X-axis filter sliders (min and max) with labels
        Label xFilterLabel = new Label("X-Axis Filter (Grade Range):");
        scatterXAxisMinSlider = new Slider();
        scatterXAxisMaxSlider = new Slider();
        scatterXAxisMinLabel = new Label();
        scatterXAxisMaxLabel = new Label();
        setupScatterSlider(scatterXAxisMinSlider, scatterXAxisMinLabel, true);  // true = min slider
        setupScatterSlider(scatterXAxisMaxSlider, scatterXAxisMaxLabel, false); // false = max slider

        // We create the Y-axis filter sliders (min and max) with labels
        Label yFilterLabel = new Label("Y-Axis Filter (Grade Range):");
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
                scatterFeatureFilterControls, new Separator(Orientation.HORIZONTAL),
                correlationLabel, scatterCourseCorrelationControls,
                new Separator(Orientation.HORIZONTAL), xFilterLabel, scatterXAxisMinLabel,
                scatterXAxisMinSlider, scatterXAxisMaxLabel, scatterXAxisMaxSlider,
                new Separator(Orientation.HORIZONTAL), yFilterLabel, scatterYAxisMinLabel,
                scatterYAxisMinSlider, scatterYAxisMaxLabel, scatterYAxisMaxSlider
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
     * Create the course correlation controls (X course, Y course, show Y=X checkbox) 
     */
    private VBox createScatterCourseCorrelationControls() {
        ChartControlFactory.CourseCorrelationControls controls = ChartControlFactory.createCourseCorrelationControls(
                () -> {
                    updateScatterSliderRanges();
                    updateScatterChart();
                });
        scatterXCourseComboBox = controls.xCourseComboBox;
        scatterYCourseComboBox = controls.yCourseComboBox;
        scatterShowYEqualsXLineCheckBox = controls.showYEqualsXLineCheckBox;
        return controls.container;
    }

    /** 
     * Create the feature filter controls (feature combo box and value combo box) 
     */
    private VBox createScatterFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateScatterChart(), () -> updateScatterChart());
        scatterFeatureFilterComboBox = controls.featureComboBox;
        scatterFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }


    /** 
     * Update the slider ranges for course correlation mode (grade range 0-10)
     */
    private void updateScatterSliderRanges() {
        // For course correlation, we always use grade range 0-10
        scatterXAxisMinSlider.setMin(0);
        scatterXAxisMinSlider.setMax(10);
        scatterXAxisMinSlider.setValue(0);
        scatterXAxisMaxSlider.setMin(0);
        scatterXAxisMaxSlider.setMax(10);
        scatterXAxisMaxSlider.setValue(10);
        
        scatterYAxisMinSlider.setMin(0);
        scatterYAxisMinSlider.setMax(10);
        scatterYAxisMinSlider.setValue(0);
        scatterYAxisMaxSlider.setMin(0);
        scatterYAxisMaxSlider.setMax(10);
        scatterYAxisMaxSlider.setValue(10);
        
        // Update labels
        scatterXAxisMinLabel.setText(String.format("Min: %.1f", scatterXAxisMinSlider.getValue()));
        scatterXAxisMaxLabel.setText(String.format("Max: %.1f", scatterXAxisMaxSlider.getValue()));
        scatterYAxisMinLabel.setText(String.format("Min: %.1f", scatterYAxisMinSlider.getValue()));
        scatterYAxisMaxLabel.setText(String.format("Max: %.1f", scatterYAxisMaxSlider.getValue()));
    }

    /** 
     * Update the scatter plot chart based on course correlation selections, filters, and options 
     */
    private void updateScatterChart() {
        double xMin = scatterXAxisMinSlider.getValue();
        double xMax = scatterXAxisMaxSlider.getValue();
        double yMin = scatterYAxisMinSlider.getValue();
        double yMax = scatterYAxisMaxSlider.getValue();

        // Here we extract feature filter and convert "No Feature" to null
        String filterFeature = scatterFeatureFilterComboBox.getValue();
        String filterValue = scatterFeatureFilterValueComboBox.getValue();
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
        }

        // Extract course correlation parameters
        String xCourse = scatterXCourseComboBox.getValue();
        String yCourse = scatterYCourseComboBox.getValue();
        boolean showYEqualsX = scatterShowYEqualsXLineCheckBox != null && scatterShowYEqualsXLineCheckBox.isSelected();

        BorderPane chartContainer = scatterGenerator.createChart(
                xMin, xMax, yMin, yMax, filterFeature, filterValue,
                xCourse, yCourse, showYEqualsX);

        scatterRoot.setCenter(chartContainer);
    }


}
