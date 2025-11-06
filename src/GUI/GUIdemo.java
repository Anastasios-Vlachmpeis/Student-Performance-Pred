package GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUIdemo extends Application {

    private BorderPane root;
    private ListView<String> chartList;
    private Button generateButton;


    public void start(Stage primaryStage) {
        root = new BorderPane();


        VBox dataPanel = new VBox(10);
        dataPanel.setPadding(new Insets(15));
        dataPanel.setPrefWidth(200);

        chartList = new ListView<>();
        chartList.getItems().addAll("BarChart", "Histogram", "JointPlot", "ScarPlot", "ScatterPlot");
        chartList.getSelectionModel().selectFirst();

        generateButton = new Button("Generate");
        generateButton.setMaxWidth(Double.MAX_VALUE);


        dataPanel.getChildren().addAll(chartList, generateButton);


        root.setLeft(dataPanel);


        generateButton.setOnAction(e -> generateChart());

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graph Generator");
        primaryStage.show();
    }

    private void generateChart() {
        String selected = chartList.getSelectionModel().getSelectedItem();
        Chart chart = null;

        switch (selected) {
            case "BarChart" -> chart = new BarChartGenerator().createChart();
            case "Histogram" -> chart = new HistogramGenerator().createChart();
            case "JointPlot" -> chart = new JointPlotGenerator().createChart();
            case "ScarPlot" -> chart = new ScarPlotGenerator().createChart();
            case "ScatterPlot" -> chart = new ScatterPlotGenerator().createChart();
        }

        root.setCenter(chart);
    }

    public static void main(String[] args) {
        launch(args);
    }
}