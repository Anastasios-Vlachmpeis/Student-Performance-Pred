package GUI;

import GUI.tab.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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



    public void start(Stage primaryStage) {
        root = new BorderPane();

        // Create TabPane
        TabPane tabPane = new TabPane();

//        // Tab 1: Original Chart Generator
//        Tab chartGeneratorTab = new Tab("Chart Generator");
//        chartGeneratorTab.setClosable(false);
//        BorderPane chartGeneratorPane = createChartGeneratorTab();
//        chartGeneratorTab.setContent(chartGeneratorPane);
//        tabPane.getTabs().add(chartGeneratorTab);

        // Tab 0: Bar Chart
        Tab barTab = new Tab("Bar Chart");
        barTab.setClosable(false);
        BorderPane barPane = new BarChartTab().createBarChartTab();
        barTab.setContent(barPane);
        tabPane.getTabs().add(barTab);
        // Tab 1: Scatter Plot
        Tab scatterPlotTab = new Tab("Scatter Plot");
        scatterPlotTab.setClosable(false);
        BorderPane scatterPlotPane = new ScatterPlotTab().createScatterPlotTab();
        scatterPlotTab.setContent(scatterPlotPane);
        tabPane.getTabs().add(scatterPlotTab);

        // Tab 2: Joint Plot
        Tab jointPlotTab = new Tab("Joint Plot");
        jointPlotTab.setClosable(false);
        BorderPane jointPlotPane = new JointPlotTab().createJointPlotTab();
        jointPlotTab.setContent(jointPlotPane);
        tabPane.getTabs().add(jointPlotTab);

        // Tab 3: Faux Heat Map
        Tab fauxHeatMapTab = new Tab("Pearson Correlation Current");
        fauxHeatMapTab.setClosable(false);
        Pane fauxHeatMapCurrentGrades = new FauxHeatMapTab().createPearsonCorrelationCurrentCourses();
        fauxHeatMapTab.setContent(fauxHeatMapCurrentGrades);
        tabPane.getTabs().add(fauxHeatMapTab);
        // Tab 3.5: Faux Heat Map
        Tab fauxHeatMapTab2 = new Tab("Pearson Correlation Graduate");
        fauxHeatMapTab2.setClosable(false);
        Pane fauxHeatMapGraduateGrades = new FauxHeatMapTab().createPearsonCorrelationGraduateCourses();
        fauxHeatMapTab2.setContent(fauxHeatMapGraduateGrades);
        tabPane.getTabs().add(fauxHeatMapTab2);
        // Tab 4: Pie Chart
        Tab PieChartTab = new Tab("Pie Chart");
        PieChartTab.setClosable(false);
        BorderPane PieChartPane = new PieChartTab().createPieChart();
        PieChartTab.setContent(PieChartPane);
        tabPane.getTabs().add(PieChartTab);

        // Tab 5: Histogram
        Tab histogramTab = new Tab("Histogram");
        histogramTab.setClosable(false);
        BorderPane histogramPane = new HistogramTab().createHistogramTab();
        histogramTab.setContent(histogramPane);
        tabPane.getTabs().add(histogramTab);

        // Tab 6: Swarm plot (not working)
        Tab swarmPlotTab = new Tab("Swarm plot (alpha)");
        swarmPlotTab.setClosable(false);
        BorderPane swarmPlotPane = new SwarmPlotTab().createTab();
        swarmPlotTab.setContent(swarmPlotPane);
        tabPane.getTabs().add(swarmPlotTab);

        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1000, 840);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graph Generator");
        primaryStage.show();
    }

//    private BorderPane createChartGeneratorTab() {
//        BorderPane pane = new BorderPane();
//        VBox dataPanel = new VBox(10);
//        dataPanel.setPadding(new Insets(15));
//        dataPanel.setPrefWidth(200);
//
//        Label chartLabel = new Label("Choose the type of chart:");
//        chartList = new ListView<>();
//        chartList.getItems().addAll("BarChart", "Histogram");
//        chartList.getSelectionModel().selectFirst();
//
//        Label yAxisLabel = new Label("Choose the dataset of y axis:");
//        yAxisList = new ListView<>();
//        yAxisList.getItems().addAll("Number of NG", "Mean of Grades", "Mode of Grades", "Median of Grades");
//        yAxisList.getSelectionModel().selectFirst();
//        yAxisInputStart = new TextField();
//        yAxisInputStart.setPromptText("Put your Y Axis filter start here");
//        yAxisInputEnd = new TextField();
//        yAxisInputEnd.setPromptText("Put your Y Axis filter end here");
//
//        Label xAxisLabel = new Label("Choose the dataset of x axis:");
//        xAxisList = new ListView<>();
//        xAxisList.getItems().addAll("Per Course", "Per Student");
//        xAxisList.getSelectionModel().selectFirst();
//        xAxisInputStart = new TextField();
//        xAxisInputStart.setPromptText("Put your X Axis filter start here");
//        xAxisInputEnd = new TextField();
//        xAxisInputEnd.setPromptText("Put your X Axis filter end here");
//
//        generateButton = new Button("Generate");
//        generateButton.setMaxWidth(Double.MAX_VALUE);
//
//        dataPanel.getChildren().addAll(chartLabel, chartList, yAxisLabel, yAxisList, yAxisInputStart, yAxisInputEnd, xAxisLabel, xAxisList, xAxisInputStart, xAxisInputEnd, generateButton);
//
//        pane.setLeft(dataPanel);
//        generateButton.setOnAction(e -> generateChart());
//
//        return pane;
//    }
//
//    private void generateChart() {
//        String selectedChart = chartList.getSelectionModel().getSelectedItem();
//        String selectedXAxis = xAxisList.getSelectionModel().getSelectedItem();
//        String selectedYAxis = yAxisList.getSelectionModel().getSelectedItem();
//        int xAxisFilterStart = Integer.parseInt(xAxisInputStart.getText());
//        int xAxisFilterEnd = Integer.parseInt(xAxisInputEnd.getText());
//        int yAxisFilterStart = Integer.parseInt(yAxisInputStart.getText());
//        int yAxisFilterEnd = Integer.parseInt(yAxisInputEnd.getText());
//        Chart chart = null;
//
//        switch (selectedChart) {
//            case "BarChart" -> chart = new BarChartGenerator().createChart(selectedXAxis, selectedYAxis, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd);
//            //case "Histogram" -> chart = new HistogramGenerator().createChart(selectedXAxis, selectedYAxis);
//            //case "JointPlot" -> chart = new JointPlotGenerator().createChart(selectedXAxis, selectedYAxis);
//            //case "ScarPlot" -> chart = new ScarPlotGenerator().createChart(selectedXAxis, selectedYAxis);
//            //case "ScatterPlot" -> chart = new ScatterPlotGenerator().createChart(selectedXAxis, selectedYAxis);
//        }
//
//        // Update the chart in the first tab
//        TabPane tabPane = (TabPane) root.getCenter();
//        Tab chartTab = tabPane.getTabs().get(0);
//        BorderPane tabContent = (BorderPane) chartTab.getContent();
//        tabContent.setCenter(chart);
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
