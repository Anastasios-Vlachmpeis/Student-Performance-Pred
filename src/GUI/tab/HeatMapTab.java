package GUI.tab;

import datamodels.CurrentGradesModel;
import datamodels.GraduateGradesModel;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tools.Color;
import tools.PearsonCorrelation;

import java.util.ArrayList;
import java.util.List;

public class HeatMapTab {
    final CategoryAxis xAxis = new CategoryAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final XYChart<String, String> sc = new ScatterChart<>(xAxis, yAxis);

    public BorderPane createPearsonCorrelationCurrentCourses() {
        xAxis.setLabel("Current Courses");
        yAxis.setLabel("Current Courses");
        sc.setTitle("Pearson Correlation between current courses based on current grades");

        // compute pearson correlation for every course pairs
        double[][] correlationMatrix = new double[CurrentGradesModel.courseCount][CurrentGradesModel.courseCount];
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                correlationMatrix[i][j] = PearsonCorrelation.betweenCurrentCourses(i, j);
            }
        }

        // range of the pearson coefficient is [-1, 1]
        // so I am creating 10 buckets of size 0.2 like such
        // [-1, -0.8], (-0.8, -0.6], (-0.6, -0.4], ..., (0.4, 0.6], (0.6, 0.8], (0.8, 1]
        XYChart.Series[] correlationBuckets = new XYChart.Series[10];
        // fill up the array
        for (int i = 0; i < correlationBuckets.length; i++) {
            correlationBuckets[i] = new XYChart.Series<String, String>();
            correlationBuckets[i].setName("r≤" + String.format("%.2f", -1 + (0.2 + i * 0.2)));
        }

        // go through all the correlations and put them into respective bucket
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                double pearsonCorrelation = correlationMatrix[i][j];
                int bucket = (int) Math.max(0, Math.floor((pearsonCorrelation + 1.0) / 0.2)-1);
                correlationBuckets[bucket].getData().add(new XYChart.Data<String, String>(CurrentGradesModel.getCourseName(i), CurrentGradesModel.getCourseName(j)));
            }
        }

        // add buckets to the chart in the form of series (1 series corresponds to 1 bucket)
        for (int i = 0; i < correlationBuckets.length; i++) {
            XYChart.Series series = correlationBuckets[i];
            if (series.getData().isEmpty()) {
                continue;
            }
            sc.getData().add(series);
        }

        // CSS blackmagic, redefine markers on scatter chart and color them based on heatmap color distribution
        List<Integer> usedSeriesNumbers = new ArrayList<>();
        for (int seriesNumber = 0; seriesNumber < sc.getData().size(); seriesNumber++) {
            boolean flagIsFirst = true;
            for (Node seriesNode : sc.lookupAll(".series" + seriesNumber)) {
                seriesNode.setStyle("-fx-background-color: " + Color.heatmapRGBinCSS(-1, 1, -0.7 + seriesNumber * 0.2) + "; -fx-shape: \"M 10 10  v10.0 h 10.0  v-10  Z\";-fx-padding: 6px;");
                if (flagIsFirst) {
                    usedSeriesNumbers.add(seriesNumber);
                    flagIsFirst = false;
                }
            }
        }
        
        // Create custom legend similar to scatterplot/jointplot
        sc.setLegendVisible(false);
        BorderPane container = new BorderPane(sc);
        createHeatmapLegend(container, usedSeriesNumbers);
        return container;
    }

    public BorderPane createPearsonCorrelationGraduateCourses() {
        xAxis.setLabel("Graduate Courses");
        yAxis.setLabel("Graduate Courses");
        sc.setTitle("Pearson Correlation between graduate courses based on graduate grades");

        // compute pearson correlation for every course pairs
        double[][] correlationMatrix = new double[CurrentGradesModel.courseCount][CurrentGradesModel.courseCount];
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                correlationMatrix[i][j] = PearsonCorrelation.betweenGraduateCourses(i, j);
            }
        }

        // range of the pearson coefficient is [-1, 1]
        // so I am creating 10 buckets of size 0.2 like such
        // [-1, -0.8], (-0.8, -0.6], (-0.6, -0.4], ..., (0.4, 0.6], (0.6, 0.8], (0.8, 1]
        XYChart.Series[] correlationBuckets = new XYChart.Series[10];
        // fill up the array
        for (int i = 0; i < correlationBuckets.length; i++) {
            correlationBuckets[i] = new XYChart.Series<String, String>();
            correlationBuckets[i].setName("r≤" + String.format("%.2f", -1 + (0.2 + i * 0.2)));
        }

        // go through all the correlations and put them into respective bucket
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                double pearsonCorrelation = correlationMatrix[i][j];
                int bucket = (int) Math.max(0, Math.floor((pearsonCorrelation + 1.0) / 0.2)-1);
                correlationBuckets[bucket].getData().add(new XYChart.Data<String, String>(GraduateGradesModel.getCourseName(i), GraduateGradesModel.getCourseName(j)));
            }
        }

        // add buckets to the chart in the form of series (1 series corresponds to 1 bucket)
        for (int i = 0; i < correlationBuckets.length; i++) {
            XYChart.Series series = correlationBuckets[i];
            if (series.getData().isEmpty()) {
                continue;
            }
            sc.getData().add(series);
        }

        // CSS blackmagic, redefine markers on scatter chart and color them based on heatmap color distribution
        List<Integer> usedSeriesNumbers = new ArrayList<>();
        for (int seriesNumber = 0; seriesNumber < sc.getData().size(); seriesNumber++) {
            boolean flagIsFirst = true;
            for (Node seriesNode : sc.lookupAll(".series" + seriesNumber)) {
                seriesNode.setStyle("-fx-background-color: " + Color.heatmapRGBinCSS(-1, 1, -0.7 + seriesNumber * 0.2) + "; -fx-shape: \"M 10 10  v10.0 h 10.0  v-10  Z\";-fx-padding: 6px;");
                if (flagIsFirst) {
                    usedSeriesNumbers.add(seriesNumber);
                    flagIsFirst = false;
                }
            }
        }
        
        // Create custom legend similar to scatterplot/jointplot
        sc.setLegendVisible(false);
        BorderPane container = new BorderPane(sc);
        createHeatmapLegend(container, usedSeriesNumbers);
        return container;
    }
    
    /**
     * Creates a custom legend for the heatmap similar to scatterplot/jointplot legend.
     */
    private void createHeatmapLegend(BorderPane container, List<Integer> activeBucketIndices) {
        sc.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                VBox customLegend = new VBox(5);
                customLegend.setStyle("-fx-padding: 10px; -fx-background-color: white;");

                Label legendTitle = new Label("Correlation Range:");
                legendTitle.setStyle("-fx-font-weight: bold;");
                customLegend.getChildren().add(legendTitle);

                HBox legendRow = new HBox(10);
                legendRow.setStyle("-fx-alignment: center-left;");

                // Iterate over the indices we actually plotted
                for (int realIndex : activeBucketIndices) {
                    // Calculate Label based on real index
                    String bucketLabel = "r≤" + String.format("%.2f", -1 + (0.2 + realIndex * 0.2));
                    
                    // Calculate Color based on real index (Match the logic in CSS block above)
                    double colorValue = -0.9 + (realIndex * 0.2);
                    String color = Color.heatmapRGBinCSS(-1, 1, colorValue);

                    Label colorSquare = new Label();
                    colorSquare.setPrefSize(20, 20);
                    colorSquare.setStyle(String.format("-fx-background-color: %s; -fx-border-color: black; -fx-border-width: 1px;", color));

                    Label label = new Label(bucketLabel);
                    label.setStyle("-fx-font-size: 12px;");

                    HBox legendItem = new HBox(5);
                    legendItem.getChildren().addAll(colorSquare, label);
                    legendRow.getChildren().add(legendItem);
                }

                customLegend.getChildren().add(legendRow);
                container.setBottom(customLegend);
            }
        });
    }
}
