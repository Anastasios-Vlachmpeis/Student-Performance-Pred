package GUI.tab;

import GUI.chart.ScatterPlotGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tools.ChartControlFactory;

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



    public BorderPane createScatterPlotTab() {
        scatterGenerator = new ScatterPlotGenerator();
        scatterRoot = new BorderPane();
        // scrollable wrapping
        scatterRoot.setLeft(new ScrollPane(createScatterControlPanel()));
        updateScatterChart();
        return scatterRoot;
    }

    private VBox createScatterControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(300);

        scatterFeatureFilterControls = createScatterFeatureFilterControls();
        scatterCourseCorrelationModeCheckBox = new CheckBox("Course Correlation Mode");
        scatterCourseCorrelationModeCheckBox.setSelected(false);
        scatterCourseCorrelationModeCheckBox.setOnAction(e -> {
            updateScatterCourseCorrelationControls();
            updateScatterYAxisOptions();
            updateScatterSliderRanges();
            updateScatterChart();
        });

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

        Label xFilterLabel = new Label("X-Axis Filter:");
        scatterXAxisMinSlider = new Slider();
        scatterXAxisMaxSlider = new Slider();
        scatterXAxisMinLabel = new Label();
        scatterXAxisMaxLabel = new Label();
        setupScatterSlider(scatterXAxisMinSlider, scatterXAxisMinLabel, true);
        setupScatterSlider(scatterXAxisMaxSlider, scatterXAxisMaxLabel, false);

        Label yFilterLabel = new Label("Y-Axis Filter:");
        scatterYAxisMinSlider = new Slider();
        scatterYAxisMaxSlider = new Slider();
        scatterYAxisMinLabel = new Label();
        scatterYAxisMaxLabel = new Label();
        setupScatterSlider(scatterYAxisMinSlider, scatterYAxisMinLabel, true);
        setupScatterSlider(scatterYAxisMaxSlider, scatterYAxisMaxLabel, false);
        updateScatterSliderRanges();

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

    private void setupScatterSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(slider, label, isMin,
                scatterXAxisMinSlider, scatterXAxisMaxSlider,
                scatterYAxisMinSlider, scatterYAxisMaxSlider,
                () -> updateScatterChart());
    }

    private void updateScatterYAxisOptions() {
        ChartControlFactory.updateYAxisOptions(
                scatterYAxisComboBox,
                scatterXAxisComboBox,
                scatterCourseCorrelationModeCheckBox.isSelected()
        );
    }

    private VBox createScatterCourseCorrelationControls() {
        ChartControlFactory.CourseCorrelationControls controls = ChartControlFactory.createCourseCorrelationControls(
                () -> updateScatterChart()
        );
        scatterXCourseComboBox = controls.xCourseComboBox;
        scatterYCourseComboBox = controls.yCourseComboBox;
        scatterShowYEqualsXLineCheckBox = controls.showYEqualsXLineCheckBox;
        return controls.container;
    }

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

    private VBox createScatterFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateScatterChart(),
                () -> updateScatterChart()
        );
        scatterFeatureFilterComboBox = controls.featureComboBox;
        scatterFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }


    private VBox createScatterPredictionControls() {
        ChartControlFactory.PredictionControls controls = ChartControlFactory.createPredictionControls(
                () -> updateScatterChart()
        );
        scatterCourseComboBox = controls.courseComboBox;
        scatterFeatureComboBox = controls.featureComboBox;
        scatterShowActualGradesCheckBox = controls.showActualGradesCheckBox;
        return controls.container;
    }

    private void updateScatterPredictionControls() {
        ChartControlFactory.updatePredictionControls(scatterYAxisComboBox, scatterPredictionControls);
    }

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

    private void updateScatterChart() {
        String xAxisData = scatterXAxisComboBox.getValue();
        String yAxisData = scatterYAxisComboBox.getValue();

        double xMin = scatterXAxisMinSlider.getValue();
        double xMax = scatterXAxisMaxSlider.getValue();
        double yMin = scatterYAxisMinSlider.getValue();
        double yMax = scatterYAxisMaxSlider.getValue();

        String selectedCourse = null;
        String selectedFeature = null;
        boolean showActualGrades = false;
        if ("Predicted Grade".equals(yAxisData)) {
            selectedCourse = scatterCourseComboBox.getValue();
            selectedFeature = scatterFeatureComboBox.getValue();
            showActualGrades = scatterShowActualGradesCheckBox.isSelected();
        }

        String filterFeature = scatterFeatureFilterComboBox.getValue();
        String filterValue = scatterFeatureFilterValueComboBox.getValue();
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
        }

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
