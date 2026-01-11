package GUI.tab;

import GUI.chart.BarChartGenerator;
import GUI.style.UIStyling;
import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tools.ChartControlFactory;

public class BarChartTab {

    private ComboBox<String> xAxisCombo;
    private ComboBox<String> yAxisCombo;
    private Slider xMinSlider;
    private Slider xMaxSlider;
    private Slider yMinSlider;
    private Slider yMaxSlider;
    private Label xMinLabel;
    private Label xMaxLabel;
    private Label yMinLabel;
    private Label yMaxLabel;
    private BorderPane root;
    private BarChartGenerator generator;

    public BorderPane createBarChartTab() {
        generator = new BarChartGenerator();
        root = new BorderPane();

        ScrollPane scrollPane = new ScrollPane(createControls());
        scrollPane.setFitToWidth(true);
        root.setLeft(scrollPane);

        updateSliderRanges();
        updateChart();

        return root;
    }

    private VBox createControls() {
        VBox box = new VBox();
        UIStyling.styleControlPanel(box);

        // set x axis combobox
        Label xAxisLabel = new Label("X-Axis:");
        UIStyling.styleHeadingLabel(xAxisLabel);
        xAxisCombo = new ComboBox<>();
        UIStyling.styleComboBox(xAxisCombo);
        xAxisCombo.getItems().addAll("Per Course", "Per Student");
        xAxisCombo.setValue("Per Course");
        //when there is a change on datasets, update the sliders and the chart
        xAxisCombo.setOnAction(e -> {
            updateSliderRanges();
            updateChart();
        });

        // set y axis combobox
        Label yAxisLabel = new Label("Y-Axis:");
        UIStyling.styleHeadingLabel(yAxisLabel);
        yAxisCombo = new ComboBox<>();
        UIStyling.styleComboBox(yAxisCombo);
        yAxisCombo.getItems().addAll("Number of NG", "Mean of Grades", "Mode of Grades", "Median of Grades");
        yAxisCombo.setValue("Number of NG");
        //when there is a change on datasets, update the sliders and the chart
        yAxisCombo.setOnAction(e -> {
            updateSliderRanges();
            updateChart();
        });

        // create labels
        xMinLabel = new Label();
        xMaxLabel = new Label();
        yMinLabel = new Label();
        yMaxLabel = new Label();

        // create sliders
        xMinSlider = new Slider();
        UIStyling.styleSlider(xMinSlider);
        xMaxSlider = new Slider();
        UIStyling.styleSlider(xMaxSlider);
        yMinSlider = new Slider();
        UIStyling.styleSlider(yMinSlider);
        yMaxSlider = new Slider();
        UIStyling.styleSlider(yMaxSlider);

        // set up the sliders using the method from chart control factory
        setupSlider(xMinSlider, xMinLabel, true);
        setupSlider(xMaxSlider, xMaxLabel, false);
        setupSlider(yMinSlider, yMinLabel, true);
        setupSlider(yMaxSlider, yMaxLabel, false);

        Label xRangeLabel = new Label("X Range:");
        UIStyling.styleHeadingLabel(xRangeLabel);
        Label yRangeLabel = new Label("Y Range:");
        UIStyling.styleHeadingLabel(yRangeLabel);
        
        box.getChildren().addAll(
                xAxisLabel, xAxisCombo,
                yAxisLabel, yAxisCombo,
                xRangeLabel, xMinLabel, xMinSlider, xMaxLabel, xMaxSlider,
                yRangeLabel, yMinLabel, yMinSlider, yMaxLabel, yMaxSlider
        );

        return box;
    }

    // identical wrapper like in scatter tab
    private void setupSlider(Slider slider, Label label, boolean isMin) {
        ChartControlFactory.setupSlider(
                slider, label, isMin,
                xMinSlider, xMaxSlider,
                yMinSlider, yMaxSlider,
                () -> updateChart()
        );
    }

    private void updateSliderRanges() {

        // for x axis
        int xMax;
        if ("Per Course".equals(xAxisCombo.getValue())) {
            xMax = CurrentGradesModel.courseCount;
        } else {
            xMax = CurrentGradesModel.studentCount;
        }

        //Setting the maximums according to the chosen dataset
        xMinSlider.setMin(0);
        xMinSlider.setMax(xMax);
        xMaxSlider.setMin(0);
        xMaxSlider.setMax(xMax);
        xMaxSlider.setValue(xMax);
        xMinSlider.setValue(0);

        // for y axis
        int yMax;

        if ("Number of NG".equals(yAxisCombo.getValue())) {
            // different number of possible max ng for student and course
            if ("Per Course".equals(xAxisCombo.getValue())) {
                yMax = 1522;   // max ng for course
            } else {
                yMax = 36;     // max ng for student
            }
        }
        else {
            //for all the calc methods for mean median mode, it is always max 10
            yMax = 10;
        }

        //Setting the maximums according to the chosen dataset
        yMinSlider.setMin(0);
        yMinSlider.setMax(yMax);
        yMaxSlider.setMin(0);
        yMaxSlider.setMax(yMax);
        yMaxSlider.setValue(yMax);
        yMinSlider.setValue(0);

        //set the maximum value between two filters for the x axis to be 100 so the graph doesn't get crowded
        xMinSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (xMaxSlider.getValue() > newVal.doubleValue() + 100) {
                xMaxSlider.setValue(newVal.doubleValue() + 100);
            }
        });

        xMaxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (xMinSlider.getValue() < newVal.doubleValue() - 100) {
                xMinSlider.setValue(newVal.doubleValue() - 100);
            }
        });

        // update labels
        xMinLabel.setText("X Min: " + (int)xMinSlider.getValue());
        xMaxLabel.setText("X Max: " + (int)xMaxSlider.getValue());
        yMinLabel.setText("Y Min: " + (int)yMinSlider.getValue());
        yMaxLabel.setText("Y Max: " + (int)yMaxSlider.getValue());
    }

    //method to update and regenerate the chart when there is a change on datasets or filters
    private void updateChart() {

        int xStart = (int) Math.min(xMinSlider.getValue(), xMaxSlider.getValue());
        int xEnd   = (int) Math.max(xMinSlider.getValue(), xMaxSlider.getValue());
        int yStart = (int) Math.min(yMinSlider.getValue(), yMaxSlider.getValue());
        int yEnd   = (int) Math.max(yMinSlider.getValue(), yMaxSlider.getValue());

        BarChart<String, Number> chart = generator.createChart(
                xAxisCombo.getValue(),
                yAxisCombo.getValue(),
                xStart, xEnd,
                yStart, yEnd
        );

        root.setCenter(chart);
    }
}