package GUI.tab;

import GUI.chart.SwarmPlotGenerator;
import javafx.scene.chart.Chart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class SwarmPlotTab {
    private BorderPane root;
    private ListView<String> yAxisList;
    private TextField yAxisInputStart;
    private TextField yAxisInputEnd;
    private ListView<String> xAxisList;
    private TextField xAxisInputStart;
    private TextField xAxisInputEnd;

    public BorderPane createTab() {
        this.root = new BorderPane();


        VBox dataPanel = new VBox(10);
        dataPanel.setPadding(new Insets(15));
        dataPanel.setPrefWidth(200);

//        Label chartLabel = new Label("Choose the type of chart:");
//        chartList = new ListView<>();
//        chartList.getItems().addAll("BarChart", "Histogram", "JointPlot", "SwarmPlot", "ScatterPlot");
//        chartList.getSelectionModel().selectFirst();

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

        Button generateButton = new Button("Generate");
        generateButton.setMaxWidth(Double.MAX_VALUE);


        dataPanel.getChildren().addAll(yAxisLabel, yAxisList, yAxisInputStart, yAxisInputEnd, xAxisLabel, xAxisList, xAxisInputStart, xAxisInputEnd, generateButton);


        root.setLeft(dataPanel);


        generateButton.setOnAction(e -> generateChart());


        return root;
    }
    private void generateChart() {
        String selectedXAxis = xAxisList.getSelectionModel().getSelectedItem();
        String selectedYAxis = yAxisList.getSelectionModel().getSelectedItem();
        int xAxisFilterStart = Integer.parseInt(xAxisInputStart.getText());
        int xAxisFilterEnd = Integer.parseInt(xAxisInputEnd.getText());
        int yAxisFilterStart = Integer.parseInt(yAxisInputStart.getText());
        int yAxisFilterEnd = Integer.parseInt(yAxisInputEnd.getText());

        Chart chart = new SwarmPlotGenerator().createChart(
                selectedXAxis,
                selectedYAxis,
                xAxisFilterStart,
                xAxisFilterEnd,
                yAxisFilterStart,
                yAxisFilterEnd
        );

        root.setCenter(chart);
    }

}
