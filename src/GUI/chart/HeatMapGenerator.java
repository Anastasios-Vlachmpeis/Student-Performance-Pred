package GUI.chart;

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

public class HeatMapGenerator {
    
    /**
     * Creates a Pearson correlation heatmap for current courses.
     * @return A BorderPane containing the heatmap chart and legend
     */
    public BorderPane createCurrentCoursesHeatMap() {
        CategoryAxis xAxis = new CategoryAxis();
        CategoryAxis yAxis = new CategoryAxis();
        ScatterChart<String, String> sc = new ScatterChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Current Courses");
        yAxis.setLabel("Current Courses");
        sc.setTitle("Pearson Correlation between current courses based on current grades");

        // Compute pearson correlation for every course pair
        double[][] correlationMatrix = computeCorrelationMatrix(true);

        // Create buckets and populate chart
        List<Integer> usedBucketIndices = populateChart(sc, correlationMatrix, true);

        // Style the chart nodes
        styleChartNodes(sc, usedBucketIndices);

        // Create custom legend
        sc.setLegendVisible(false);
        BorderPane container = new BorderPane(sc);
        createHeatmapLegend(container, sc, usedBucketIndices);
        
        return container;
    }
    
    /**
     * Creates a Pearson correlation heatmap for graduate courses.
     * @return A BorderPane containing the heatmap chart and legend
     */
    public BorderPane createGraduateCoursesHeatMap() {
        CategoryAxis xAxis = new CategoryAxis();
        CategoryAxis yAxis = new CategoryAxis();
        ScatterChart<String, String> sc = new ScatterChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Graduate Courses");
        yAxis.setLabel("Graduate Courses");
        sc.setTitle("Pearson Correlation between graduate courses based on graduate grades");

        // Compute pearson correlation for every course pair
        double[][] correlationMatrix = computeCorrelationMatrix(false);

        // Create buckets and populate chart
        List<Integer> usedBucketIndices = populateChart(sc, correlationMatrix, false);

        // Style the chart nodes
        styleChartNodes(sc, usedBucketIndices);

        // Create custom legend
        sc.setLegendVisible(false);
        BorderPane container = new BorderPane(sc);
        createHeatmapLegend(container, sc, usedBucketIndices);
        
        return container;
    }
    
    /**
     * Computes the correlation matrix for all course pairs.
     * @param useCurrentGrades If true, uses CurrentGradesModel; otherwise uses GraduateGradesModel
     * @return A 2D array containing correlation values
     */
    private double[][] computeCorrelationMatrix(boolean useCurrentGrades) {
        double[][] correlationMatrix = new double[CurrentGradesModel.courseCount][CurrentGradesModel.courseCount];
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                if (useCurrentGrades) {
                    correlationMatrix[i][j] = PearsonCorrelation.betweenCurrentCourses(i, j);
                } else {
                    correlationMatrix[i][j] = PearsonCorrelation.betweenGraduateCourses(i, j);
                }
            }
        }
        return correlationMatrix;
    }
    
    /**
     * Populates the chart with correlation data organized into buckets.
     * Range of Pearson coefficient is [-1, 1], creating 10 buckets of size 0.2:
     * [-1, -0.8], (-0.8, -0.6], (-0.6, -0.4], ..., (0.4, 0.6], (0.6, 0.8], (0.8, 1]
     * 
     * @param sc The ScatterChart to populate
     * @param correlationMatrix The correlation matrix
     * @param useCurrentGrades If true, uses CurrentGradesModel; otherwise uses GraduateGradesModel
     * @return List of series numbers that were actually used (non-empty buckets)
     */
    @SuppressWarnings("unchecked")
    private List<Integer> populateChart(ScatterChart<String, String> sc, double[][] correlationMatrix, boolean useCurrentGrades) {
        XYChart.Series<String, String>[] correlationBuckets = new XYChart.Series[10];
        
        // Initialize buckets
        for (int i = 0; i < correlationBuckets.length; i++) {
            correlationBuckets[i] = new XYChart.Series<String, String>();
            correlationBuckets[i].setName("r≤" + String.format("%.2f", -1 + (0.2 + i * 0.2)));
        }

        // Distribute correlations into buckets
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                double pearsonCorrelation = correlationMatrix[i][j];
                int bucket = (int) Math.max(0, Math.floor((pearsonCorrelation + 1.0) / 0.2) - 1);
                
                String courseNameI = useCurrentGrades 
                    ? CurrentGradesModel.getCourseName(i) 
                    : GraduateGradesModel.getCourseName(i);
                String courseNameJ = useCurrentGrades 
                    ? CurrentGradesModel.getCourseName(j) 
                    : GraduateGradesModel.getCourseName(j);
                
                correlationBuckets[bucket].getData().add(
                    new XYChart.Data<String, String>(courseNameI, courseNameJ)
                );
            }
        }

        // Add non-empty buckets to chart and track used bucket indices
        List<Integer> usedBucketIndices = new ArrayList<>();
        for (int i = 0; i < correlationBuckets.length; i++) {
            XYChart.Series<String, String> series = correlationBuckets[i];
            if (!series.getData().isEmpty()) {
                sc.getData().add(series);
                usedBucketIndices.add(i); // Track the original bucket index
            }
        }
        
        return usedBucketIndices;
    }
    
    /**
     * Styles the chart nodes based on their correlation bucket.
     * 
     * @param sc The ScatterChart to style
     * @param usedBucketIndices List of bucket indices that were used
     */
    private void styleChartNodes(ScatterChart<String, String> sc, List<Integer> usedBucketIndices) {
        for (int seriesIndex = 0; seriesIndex < usedBucketIndices.size(); seriesIndex++) {
            int bucketIndex = usedBucketIndices.get(seriesIndex);
            int seriesNumber = seriesIndex; // Series number matches the order they were added
            for (Node seriesNode : sc.lookupAll(".series" + seriesNumber)) {
                seriesNode.setStyle("-fx-background-color: " + 
                    Color.heatmapRGBinCSS(-1, 1, -0.7 + bucketIndex * 0.2) + 
                    "; -fx-shape: \"M 10 10  v10.0 h 10.0  v-10  Z\";-fx-padding: 6px;");
            }
        }
    }
    
    /**
     * Creates a custom legend for the heatmap similar to scatterplot/jointplot legend.
     * @param container The BorderPane container to add the legend to
     * @param sc The ScatterChart
     * @param activeBucketIndices List of bucket indices that were actually plotted
     */
    private void createHeatmapLegend(BorderPane container, ScatterChart<String, String> sc, List<Integer> activeBucketIndices) {
        sc.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                VBox customLegend = new VBox(5);
                customLegend.setStyle("-fx-padding: 10px; -fx-background-color: white;");

                Label legendTitle = new Label("Correlation Range:");
                legendTitle.setStyle("-fx-font-weight: bold;");
                customLegend.getChildren().add(legendTitle);

                HBox legendRow = new HBox(10);
                legendRow.setStyle("-fx-alignment: center-left;");

                // Iterate over the bucket indices we actually plotted
                for (int bucketIndex : activeBucketIndices) {
                    // Calculate Label based on bucket index
                    String bucketLabel = "r≤" + String.format("%.2f", -1 + (0.2 + bucketIndex * 0.2));
                    
                    // Calculate Color based on bucket index (Match the logic in CSS block above)
                    double colorValue = -0.9 + (bucketIndex * 0.2);
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