package GUI.tab;

import GUI.old.JointPlotGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tools.ChartControlFactory;

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


    public BorderPane createJointPlotTab() {
        jointGenerator = new JointPlotGenerator();
        jointRoot = new BorderPane();
        jointRoot.setLeft(new ScrollPane(createJointControlPanel()));
        // updateJointChart() is called at the end of createJointControlPanel() via updateJointSliderRanges()
        return jointRoot;
    }

    private VBox createJointControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(300);

        jointFeatureFilterControls = createJointFeatureFilterControls();
        jointCourseCorrelationModeCheckBox = new CheckBox("Course Correlation Mode");
        jointCourseCorrelationModeCheckBox.setSelected(false);
        jointCourseCorrelationModeCheckBox.setOnAction(e -> {
            updateJointCourseCorrelationControls();
            updateJointYAxisOptions();
            updateJointSliderRanges();
            updateJointChart();
        });

        jointCourseCorrelationControls = createJointCourseCorrelationControls();

        Label xAxisLabel = new Label("X-Axis:");
        jointXAxisComboBox = new ComboBox<>();
        jointXAxisComboBox.getItems().addAll("Per Student", "Per Course");
        jointXAxisComboBox.setValue("Per Student");
        jointXAxisComboBox.setOnAction(e -> {
            if (!jointCourseCorrelationModeCheckBox.isSelected()) {
                updateJointYAxisOptions();
            }
            updateJointPredictionControls();
            updateJointSliderRanges();
            updateJointChart();
        });

        Label yAxisLabel = new Label("Y-Axis:");
        jointYAxisComboBox = new ComboBox<>();
        jointYAxisComboBox.getSelectionModel().selectFirst();
        updateJointYAxisOptions();
        jointYAxisComboBox.setValue("Mean");
        jointYAxisComboBox.setOnAction(e -> {
            updateJointPredictionControls();
            updateJointSliderRanges();
            updateJointChart();
        });

        jointPredictionControls = createJointPredictionControls();

        Label xFilterLabel = new Label("X-Axis Filter:");
        jointXAxisMinSlider = new Slider();
        jointXAxisMaxSlider = new Slider();
        jointXAxisMinLabel = new Label();
        jointXAxisMaxLabel = new Label();
        setupJointSlider(jointXAxisMinSlider, jointXAxisMinLabel, true);
        setupJointSlider(jointXAxisMaxSlider, jointXAxisMaxLabel, false);

        Label yFilterLabel = new Label("Y-Axis Filter:");
        jointYAxisMinSlider = new Slider();
        jointYAxisMaxSlider = new Slider();
        jointYAxisMinLabel = new Label();
        jointYAxisMaxLabel = new Label();
        setupJointSlider(jointYAxisMinSlider, jointYAxisMinLabel, true);
        setupJointSlider(jointYAxisMaxSlider, jointYAxisMaxLabel, false);
        updateJointSliderRanges();
        updateJointChart();

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
                xFilterLabel, jointXAxisMinLabel, jointXAxisMinSlider, jointXAxisMaxLabel, jointXAxisMaxSlider,
                new Separator(Orientation.HORIZONTAL),
                yFilterLabel, jointYAxisMinLabel, jointYAxisMinSlider, jointYAxisMaxLabel, jointYAxisMaxSlider
        );

        // finish scroll wrapping
        return panel;
    }

    private void setupJointSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(slider, label, isMin,
                jointXAxisMinSlider, jointXAxisMaxSlider,
                jointYAxisMinSlider, jointYAxisMaxSlider,
                () -> updateJointChart());
    }

    private void updateJointYAxisOptions() {
        ChartControlFactory.updateYAxisOptions(
                jointYAxisComboBox,
                jointXAxisComboBox,
                jointCourseCorrelationModeCheckBox.isSelected()
        );
    }

    private VBox createJointCourseCorrelationControls() {
        ChartControlFactory.CourseCorrelationControls controls = ChartControlFactory.createCourseCorrelationControls(
                () -> updateJointChart()
        );
        jointXCourseComboBox = controls.xCourseComboBox;
        jointYCourseComboBox = controls.yCourseComboBox;
        jointShowYEqualsXLineCheckBox = controls.showYEqualsXLineCheckBox;
        return controls.container;
    }

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

    private VBox createJointFeatureFilterControls() {
        ChartControlFactory.FeatureFilterControls controls = ChartControlFactory.createFeatureFilterControls(
                () -> updateJointChart(),
                () -> updateJointChart()
        );
        jointFeatureFilterComboBox = controls.featureComboBox;
        jointFeatureFilterValueComboBox = controls.valueComboBox;
        return controls.container;
    }


    private VBox createJointPredictionControls() {
        ChartControlFactory.PredictionControls controls = ChartControlFactory.createPredictionControls(
                () -> updateJointChart()
        );
        jointCourseComboBox = controls.courseComboBox;
        jointFeatureComboBox = controls.featureComboBox;
        jointShowActualGradesCheckBox = controls.showActualGradesCheckBox;
        return controls.container;
    }

    private void updateJointPredictionControls() {
        ChartControlFactory.updatePredictionControls(jointYAxisComboBox, jointPredictionControls);
    }

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

        String selectedCourse = null;
        String selectedFeature = null;
        boolean showActualGrades = false;
        if ("Predicted Grade".equals(yAxisData)) {
            selectedCourse = jointCourseComboBox != null ? jointCourseComboBox.getValue() : null;
            selectedFeature = jointFeatureComboBox != null ? jointFeatureComboBox.getValue() : null;
            showActualGrades = jointShowActualGradesCheckBox != null && jointShowActualGradesCheckBox.isSelected();
        }

        String filterFeature = jointFeatureFilterComboBox != null ? jointFeatureFilterComboBox.getValue() : null;
        String filterValue = jointFeatureFilterValueComboBox != null ? jointFeatureFilterValueComboBox.getValue() : null;
        if ("No Feature".equals(filterFeature)) {
            filterFeature = null;
        }

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

        // scrollable pane wrapping
        ScrollPane scrollPane = new ScrollPane(chart);

        jointRoot.setCenter(chart);
    }



}
