package GUI;

import datamodels.CurrentGradesModel;
import javafx.application.Application;
import tools.ChartControlFactory;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUIdemo extends Application {

    // Original chart generator fields
    private BorderPane root;
    private ListView<String> chartList;
    private ListView<String> xAxisList;
    private ListView<String> yAxisList;
    private Button generateButton;
    private TextField xAxisInputStart;
    private TextField xAxisInputEnd;
    private TextField yAxisInputStart;
    private TextField yAxisInputEnd;

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

    public void start(Stage primaryStage) {
        root = new BorderPane();

        // Create TabPane
        TabPane tabPane = new TabPane();

        // Tab 1: Original Chart Generator
        Tab chartGeneratorTab = new Tab("Chart Generator");
        chartGeneratorTab.setClosable(false);
        BorderPane chartGeneratorPane = createChartGeneratorTab();
        chartGeneratorTab.setContent(chartGeneratorPane);
        tabPane.getTabs().add(chartGeneratorTab);

        // Tab 2: Scatter Plot
        Tab scatterPlotTab = new Tab("Scatter Plot");
        scatterPlotTab.setClosable(false);
        BorderPane scatterPlotPane = createScatterPlotTab();
        scatterPlotTab.setContent(scatterPlotPane);
        tabPane.getTabs().add(scatterPlotTab);

        // Tab 3: Joint Plot
        Tab jointPlotTab = new Tab("Joint Plot");
        jointPlotTab.setClosable(false);
        BorderPane jointPlotPane = createJointPlotTab();
        jointPlotTab.setContent(jointPlotPane);
        tabPane.getTabs().add(jointPlotTab);

        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 960);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graph Generator");
        primaryStage.show();
    }

    private BorderPane createChartGeneratorTab() {
        BorderPane pane = new BorderPane();
        VBox dataPanel = new VBox(10);
        dataPanel.setPadding(new Insets(15));
        dataPanel.setPrefWidth(200);

        Label chartLabel = new Label("Choose the type of chart:");
        chartList = new ListView<>();
        chartList.getItems().addAll("BarChart", "Histogram");
        chartList.getSelectionModel().selectFirst();

        Label yAxisLabel = new Label("Choose the dataset of y axis:");
        yAxisList = new ListView<>();
        yAxisList.getItems().addAll("Number of NG", "Mean of Grades", "Mode of Grades", "Median of Grades");
        yAxisList.getSelectionModel().selectFirst();
        yAxisInputStart = new TextField();
        yAxisInputStart.setPromptText("Put your Y Axis filter start here");
        yAxisInputEnd = new TextField();
        yAxisInputEnd.setPromptText("Put your Y Axis filter end here");

        Label xAxisLabel = new Label("Choose the dataset of x axis:");
        xAxisList = new ListView<>();
        xAxisList.getItems().addAll("Per Course", "Per Student");
        xAxisList.getSelectionModel().selectFirst();
        xAxisInputStart = new TextField();
        xAxisInputStart.setPromptText("Put your X Axis filter start here");
        xAxisInputEnd = new TextField();
        xAxisInputEnd.setPromptText("Put your X Axis filter end here");

        generateButton = new Button("Generate");
        generateButton.setMaxWidth(Double.MAX_VALUE);

        dataPanel.getChildren().addAll(chartLabel, chartList, yAxisLabel, yAxisList, yAxisInputStart, yAxisInputEnd, xAxisLabel, xAxisList, xAxisInputStart, xAxisInputEnd, generateButton);

        pane.setLeft(dataPanel);
        generateButton.setOnAction(e -> generateChart());

        return pane;
    }

    private BorderPane createScatterPlotTab() {
        scatterGenerator = new ScatterPlotGenerator();
        scatterRoot = new BorderPane();
        scatterRoot.setLeft(createScatterControlPanel());
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

    private BorderPane createJointPlotTab() {
        jointGenerator = new JointPlotGenerator();
        jointRoot = new BorderPane();
        jointRoot.setLeft(createJointControlPanel());
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
            jointYAxisComboBox.getValue(),
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

        jointRoot.setCenter(chart);
    }

    private void generateChart() {
        String selectedChart = chartList.getSelectionModel().getSelectedItem();
        String selectedXAxis = xAxisList.getSelectionModel().getSelectedItem();
        String selectedYAxis = yAxisList.getSelectionModel().getSelectedItem();
        int xAxisFilterStart = Integer.parseInt(xAxisInputStart.getText());
        int xAxisFilterEnd = Integer.parseInt(xAxisInputEnd.getText());
        int yAxisFilterStart = Integer.parseInt(yAxisInputStart.getText());
        int yAxisFilterEnd = Integer.parseInt(yAxisInputEnd.getText());
        Chart chart = null;

        switch (selectedChart) {
            case "BarChart" -> chart = new BarChartGenerator().createChart(selectedXAxis, selectedYAxis, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd);
            //case "Histogram" -> chart = new HistogramGenerator().createChart(selectedXAxis, selectedYAxis);
            //case "JointPlot" -> chart = new JointPlotGenerator().createChart(selectedXAxis, selectedYAxis);
            //case "ScarPlot" -> chart = new ScarPlotGenerator().createChart(selectedXAxis, selectedYAxis);
            //case "ScatterPlot" -> chart = new ScatterPlotGenerator().createChart(selectedXAxis, selectedYAxis);
        }

        // Update the chart in the first tab
        TabPane tabPane = (TabPane) root.getCenter();
        Tab chartTab = tabPane.getTabs().get(0);
        BorderPane tabContent = (BorderPane) chartTab.getContent();
        tabContent.setCenter(chart);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
