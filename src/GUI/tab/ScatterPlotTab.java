package GUI.tab;

import GUI.chart.ScatterPlotGenerator;
import GUI.style.UIStyling;
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

    // We create and return the scatter plot tab UI with control panel and chart
    public BorderPane createScatterPlotTab() {
        scatterGenerator = new ScatterPlotGenerator();
        scatterRoot = new BorderPane();
        // scrollable wrapping
        scatterRoot.setLeft(new ScrollPane(createScatterControlPanel()));
        updateScatterChart();
        return scatterRoot;
    }

    /** 
     * We create the control panel with all the necessary UI controls ;
     * filters, course selection, sliders
     */
    private VBox createScatterControlPanel() {
        // Main panel container
        VBox panel = new VBox();
        UIStyling.styleControlPanel(panel);

        // Feature filter controls
        scatterFeatureFilterControls = createScatterFeatureFilterControls();
        
        // Course correlation controls ;
        // X course, Y course, Y=X line checkbox
        Label correlationLabel = new Label("Course Correlation:");
        UIStyling.styleHeadingLabel(correlationLabel);
        scatterCourseCorrelationControls = createScatterCourseCorrelationControls();

        // X-axis filter sliders
        Label xFilterLabel = new Label("X-Axis Filter (Grade Range):");
        UIStyling.styleHeadingLabel(xFilterLabel);
        scatterXAxisMinSlider = new Slider();
        UIStyling.styleSlider(scatterXAxisMinSlider);
        scatterXAxisMaxSlider = new Slider();
        UIStyling.styleSlider(scatterXAxisMaxSlider);
        scatterXAxisMinLabel = new Label();
        scatterXAxisMaxLabel = new Label();
        setupScatterSlider(scatterXAxisMinSlider, scatterXAxisMinLabel, true);
        setupScatterSlider(scatterXAxisMaxSlider, scatterXAxisMaxLabel, false);

        // Y-axis filter sliders
        Label yFilterLabel = new Label("Y-Axis Filter (Grade Range):");
        UIStyling.styleHeadingLabel(yFilterLabel);
        scatterYAxisMinSlider = new Slider();
        UIStyling.styleSlider(scatterYAxisMinSlider);
        scatterYAxisMaxSlider = new Slider();
        UIStyling.styleSlider(scatterYAxisMaxSlider);
        scatterYAxisMinLabel = new Label();
        scatterYAxisMaxLabel = new Label();
        setupScatterSlider(scatterYAxisMinSlider, scatterYAxisMinLabel, true);
        setupScatterSlider(scatterYAxisMaxSlider, scatterYAxisMaxLabel, false);
        
        // Initialize slider ranges
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

    // Slider setup using the factory method
    // It is directly linked to different chart updates
    private void setupScatterSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(slider, label, isMin,
                scatterXAxisMinSlider, scatterXAxisMaxSlider,
                scatterYAxisMinSlider, scatterYAxisMaxSlider,
                () -> updateScatterChart());
    }

    // Course correlation controls ;
    // X course, Y course, Y=X checkbox
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

    // Feature filter controls ;
    // feature combo box and value combo box
    private VBox createScatterFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateScatterChart(), () -> updateScatterChart() // callbacks for when the feature or value changes
            );
        scatterFeatureFilterComboBox = controls.featureComboBox;
        scatterFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }

    // Slider ranges' update for course correlation
    private void updateScatterSliderRanges() {
        // Course correlation always uses a grade range of 0-10
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
     * Updates the scatter plot chart based on selections, filters, and options.
     */
    private void updateScatterChart() {
        double xMin = scatterXAxisMinSlider.getValue();
        double xMax = scatterXAxisMaxSlider.getValue();
        double yMin = scatterYAxisMinSlider.getValue();
        double yMax = scatterYAxisMaxSlider.getValue();

        // Extract feature filter, convert "No Feature" to null
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
