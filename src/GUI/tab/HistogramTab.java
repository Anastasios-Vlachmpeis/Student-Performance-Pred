package GUI.tab;

import GUI.chart.HistogramGenerator;
import GUI.style.UIStyling;
import datamodels.CurrentGradesModel;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tools.ChartControlFactory;

public class HistogramTab {

    // Drop-down Menu for choosing the X and Y axis
    private ComboBox<String> histXAxisComboBox;
    private ComboBox<String> histYAxisComboBox;

    // Sliders for filtering the ranges
    private Slider histXAxisMinSlider;
    private Slider histXAxisMaxSlider;
    private Slider histYAxisMinSlider;
    private Slider histYAxisMaxSlider;

    // Labels that show the values near sliders
    private Label histXAxisMinLabel;
    private Label histXAxisMaxLabel;
    private Label histYAxisMinLabel;
    private Label histYAxisMaxLabel;

    // layout of  HistogramTab
    private BorderPane histRoot;
    // Creates the histogram based on chosen X and Y axis
    private HistogramGenerator histGenerator;

    // Featur filter drop-down
    private VBox histFeatureFilterControls;
    private ComboBox<String> histFeatureFilterComboBox;
    private ComboBox<String> histFeatureFilterValueComboBox;


    /**
     *Creates the UI for the Histogram
     */
    public BorderPane createHistogramTab() {
        histGenerator = new HistogramGenerator();
        histRoot = new BorderPane();

        ScrollPane scrollPane = new ScrollPane(createHistogramControlPanel());
        scrollPane.setFitToWidth(true);
        histRoot.setLeft(scrollPane);

        return histRoot;
    }

    /**
     * Builds the left (menus, sliders, filters)
     */
    private VBox createHistogramControlPanel() {
        VBox panel = new VBox();
        UIStyling.styleControlPanel(panel);

        // Feature filter UI
        histFeatureFilterControls = createHistogramFeatureFilterControls();

        // X-axis dropdown
        Label xAxisLabel = new Label("X-Axis:");
        UIStyling.styleHeadingLabel(xAxisLabel);
        histXAxisComboBox = new ComboBox<>();
        UIStyling.styleComboBox(histXAxisComboBox);
        histXAxisComboBox.getItems().addAll(
                "Mean of Grades", "Median of Grades",
                "Mode of Grades", "Number of NG",
                "Number of Passing Students",
                "Number of graded Courses",
                "Number of failed Courses");
        histXAxisComboBox.setValue("Mean of Grades");
        // When X-axis choices change, update sliders and chart
        histXAxisComboBox.setOnAction(e -> {
            updateHistogramSliderRanges();
            updateHistogramChart();
        });

        // Y-axis dropdown
        Label yAxisLabel = new Label("Y-Axis (frequency):");
        UIStyling.styleHeadingLabel(yAxisLabel);
        histYAxisComboBox = new ComboBox<>();
        UIStyling.styleComboBox(histYAxisComboBox);
        histYAxisComboBox.getItems().addAll(  "Number of Courses", "Number of Students");
        histYAxisComboBox.setValue("Number of Courses");
        // When Y-axis choices change, update sliders and chart
        histYAxisComboBox.setOnAction(e -> {
            updateHistogramSliderRanges();
            updateHistogramChart();
        });

        // X-axis sliders
        Label xFilterLabel = new Label("X-Axis range:");
        UIStyling.styleHeadingLabel(xFilterLabel);
        histXAxisMinSlider = new Slider();
        UIStyling.styleSlider(histXAxisMinSlider);
        histXAxisMaxSlider = new Slider();
        UIStyling.styleSlider(histXAxisMaxSlider);
        histXAxisMinLabel = new Label();
        histXAxisMaxLabel= new Label();
        // Attaches slider logic
        setupHistogramSlider(histXAxisMinSlider, histXAxisMinLabel, true);
        setupHistogramSlider(histXAxisMaxSlider, histXAxisMaxLabel, false);

        // Y-axis sliders
        Label yFilterLabel = new Label("Y-Axis range:");
        UIStyling.styleHeadingLabel(yFilterLabel);
        histYAxisMinSlider = new Slider();
        UIStyling.styleSlider(histYAxisMinSlider);
        histYAxisMaxSlider = new Slider();
        UIStyling.styleSlider(histYAxisMaxSlider);
        histYAxisMinLabel = new Label();
        histYAxisMaxLabel= new Label();
        setupHistogramSlider(histYAxisMinSlider, histYAxisMinLabel, true);
        setupHistogramSlider(histYAxisMaxSlider, histYAxisMaxLabel, false);

        // Draws first chart and enable slider limits
        updateHistogramSliderRanges();
        updateHistogramChart();

        // Adds everything to the left UI
        panel.getChildren().addAll(
                histFeatureFilterControls,
                new Separator(Orientation.HORIZONTAL),
                yAxisLabel, histYAxisComboBox,
                xAxisLabel, histXAxisComboBox,
                new Separator(Orientation.HORIZONTAL),
                yFilterLabel,
                histYAxisMinLabel, histYAxisMinSlider,
                histYAxisMaxLabel, histYAxisMaxSlider,
                new Separator(Orientation.HORIZONTAL),
                xFilterLabel,
                histXAxisMinLabel, histXAxisMinSlider,
                histXAxisMaxLabel, histXAxisMaxSlider
        );
        return panel;
    }

    /**
     * Connects sliders and labels to the ChartControlFactory logic.
     * Refreshes histogram slider value changes.
     */
    private void setupHistogramSlider (Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(
                slider, label, isMin,
                histXAxisMinSlider, histXAxisMaxSlider,
                histYAxisMinSlider, histYAxisMaxSlider,
                () -> updateHistogramChart()
        );
    }

    /**
     * Sets the min and max value of sliders based on selected X and Y axis.
     * Makes sure that the slider show the correct ranges automatically
     */
    private void updateHistogramSliderRanges() {

        // Prevents issues if the UI hasn't loaded
        if (histXAxisComboBox == null || histYAxisComboBox == null) {
            return;
        }

        String xAxis = histXAxisComboBox.getValue();
        String yAxis = histYAxisComboBox.getValue();

        // Determines the X-axis range based on what the X-axis shows
        double xMin;
        double xMax;

        // Grade-like values (0–10)
        if ("Mean of Grades".equals(xAxis)
                || "Median of Grades".equals(xAxis)
                || "Mode of Grades".equals(xAxis)) {
            xMin = 0.0;
            xMax = 10.0;
        }
        // Course-based counts
        else if ("Number of failed Courses".equals(xAxis)
                || "Number of graded Courses".equals(xAxis)) {
            xMin = 0.0;
            xMax = CurrentGradesModel.courseCount; //36
        }
        // NG counts or passing Students counts
        else if ("Number of NG".equals(xAxis)
                || "Number of Passing Students".equals(xAxis)
                || "Number of Non-passing Students".equals(xAxis)) {
             xMin = 0.0;
            xMax = CurrentGradesModel.studentCount; //1521
        }
        else {
            xMin = 0.0;
            xMax = 10.0;
        }

        // Set X sliders
        histXAxisMinSlider.setMin(xMin);
        histXAxisMinSlider.setMax(xMax);
        histYAxisMinSlider.setValue(xMin);

        histXAxisMaxSlider.setMin(xMin);
        histXAxisMaxSlider.setMax(xMax);
        histXAxisMaxSlider.setValue(xMax);

        histXAxisMinLabel.setText(String.format("%.2f", histXAxisMinSlider.getValue()));
        histXAxisMaxLabel.setText(String.format("%.2f", histXAxisMaxSlider.getValue()));

        // Y-axis slider range
        int maxFreq = "Number of Courses".equals(yAxis)
                ? CurrentGradesModel.courseCount
                : CurrentGradesModel.studentCount;

        double yMin = 0.0;
        double yMax = Math.max(1, maxFreq);

        // Set Y sliders
        histYAxisMinSlider.setMin(yMin);
        histYAxisMinSlider.setMax(yMax);
        histYAxisMinSlider.setValue(yMin);

        histYAxisMaxSlider.setMin(yMin);
        histYAxisMaxSlider.setMax(yMax);
        histYAxisMaxSlider.setValue(yMax);

        histYAxisMinLabel.setText(Integer.toString((int) histYAxisMinSlider.getValue()));
        histYAxisMaxLabel.setText(Integer.toString((int) histYAxisMaxSlider.getValue()));
    }

    /**
     * Creates the Feature filter
     */
    private VBox createHistogramFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateHistogramChart(), // Feature is changed, redrwa
                () -> updateHistogramChart()
        );
        histFeatureFilterComboBox = controls.featureComboBox;
        histFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }

    /**
     * Refreshes histogram and shows it in the centre.
     * Runs whenever a dropdown or slider is changed.
     */
    private void updateHistogramChart() {
        // safety check
        if (histRoot == null ||
            histXAxisComboBox == null || histYAxisComboBox == null ||
            histXAxisMinLabel == null || histXAxisMaxSlider == null ||
            histYAxisMinSlider == null || histYAxisMaxSlider == null ||
            histGenerator == null) {
            return;
        }

        //Get user choices
        String xAxisData = histXAxisComboBox.getValue();
        String yAxisData = histYAxisComboBox.getValue();

        if (xAxisData == null || yAxisData == null) {
            return;
        }

        // Slider values
        double xMin = histXAxisMinSlider.getValue();
        double xMax = histXAxisMaxSlider.getValue();
        double yMin = histYAxisMinSlider.getValue();
        double yMax = histYAxisMaxSlider.getValue();

        // Feature filter setting
        String filterFeature = histFeatureFilterComboBox != null ? histFeatureFilterComboBox.getValue() : null;
        String filterValue = histFeatureFilterValueComboBox != null ? histFeatureFilterValueComboBox.getValue() : null;

        // "No Feature" mean no filtering
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
            filterValue = null;
        }

        // Creates new chart using HistogramGenerator
        var chart = histGenerator.createChart(
                xAxisData, yAxisData, xMin, xMax, yMin, yMax, filterFeature, filterValue
        );

        // SHow chart in the centre
        histRoot.setCenter(chart);
    }
}
