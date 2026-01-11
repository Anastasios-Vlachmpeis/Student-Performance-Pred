package GUI.chart;

import datamodels.CurrentGradesModel;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tools.ChartDataUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScatterPlotGenerator {

    /**
     * Creation of the scatterplot to analyze course correlations
     * Returns a BorderPane with the chart and a custom legend
     */
    public BorderPane createChart(double xAxisFilterStart, double xAxisFilterEnd,
        double yAxisFilterStart, double yAxisFilterEnd, String filterFeature, String filterValue,
        String xCourse, String yCourse, boolean showYEqualsX) {
        
        // Students are filtered based on the feature filter
        // (That's kinda how filters work)
        int[] filteredStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);
        
        // The actual chart
        ScatterChart<Number, Number> chart = createCourseCorrelationChart(xCourse, yCourse, xAxisFilterStart, xAxisFilterEnd,
                                           yAxisFilterStart, yAxisFilterEnd, filteredStudentIds, showYEqualsX);
        
        // Wrap in a BorderPane so that the legend can go to the bottom
        BorderPane container = new BorderPane();
        container.setCenter(chart);
        
        return container;
    }
    
    /**
     * The scatterplot that lets you compare grades between courses
     * Returns just the ScatterChart (also used by JointPlotGenerator)
     */
    public ScatterChart<Number, Number> createCourseCorrelationChart(String xCourse, String yCourse, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, int[] filteredStudentIds, boolean showYEqualsX) {
        int xCourseId = ChartDataUtils.findCourseId(xCourse);
        int yCourseId = ChartDataUtils.findCourseId(yCourse);
        
        // Return empty chart if course not found
        if (xCourseId == -1 || yCourseId == -1) {
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel(xCourse);
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(yCourse);
            return new ScatterChart<>(xAxis, yAxis);
        }
        
        //Axes setup 
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xCourse);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yCourse);
        
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(yCourse + " vs " + xCourse);
        scatterChart.setLegendVisible(false);
        
        if (showYEqualsX) {
            XYChart.Series<Number, Number> yEqualsXSeries = new XYChart.Series<>();
            yEqualsXSeries.setName("Y = X Line");
            
            // Generation of the Y=X line. Bound by filters and clamped to a [0, 10] grade range
            double minRange = Math.max(0, Math.min(xFilterStart, yFilterStart));
            double maxRange = Math.min(10, Math.max(xFilterEnd, yFilterEnd));
            
            int numPoints = 200;
            for (int i = 0; i <= numPoints; i++) {
                double value = minRange + (maxRange - minRange) * i / numPoints;
                XYChart.Data<Number, Number> data = new XYChart.Data<>(value, value);
                yEqualsXSeries.getData().add(data);
                // Points are styled as a thin black line 
                // rendered first so that it's below the actual data points
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: black; -fx-background-radius: 0.5px; -fx-pref-width: 1px; -fx-pref-height: 1px;");
                    }
                });
            }
            
            scatterChart.getData().add(yEqualsXSeries);
        }
        
        // Collection of valid data points first
        ArrayList<XYChart.Data<Number, Number>> dataPoints = new ArrayList<>();
        for (int studentId : filteredStudentIds) {
            try {
                double xGrade = CurrentGradesModel.getGrade(studentId, xCourseId);
                double yGrade = CurrentGradesModel.getGrade(studentId, yCourseId);
                
                // Only add the point if both of the grades are valid and within the filter ranges
                if (xGrade != -1 && yGrade != -1 &&
                    xGrade >= xFilterStart && xGrade <= xFilterEnd &&
                    yGrade >= yFilterStart && yGrade <= yFilterEnd) {
                    dataPoints.add(new XYChart.Data<>(xGrade, yGrade));
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        // Overlap count by rounding coordinates
        // 0.1 tolerance for grade precision
        Map<String, Integer> overlapCounts = new HashMap<>();
        double tolerance = 0.1;
        
        for (XYChart.Data<Number, Number> point : dataPoints) {
            double x = point.getXValue().doubleValue();
            double y = point.getYValue().doubleValue();
            
            // Round to nearest tolerance to group nearby points
            double roundedX = Math.round(x / tolerance) * tolerance;
            double roundedY = Math.round(y / tolerance) * tolerance;
            String key = roundedX + "," + roundedY;
            
            overlapCounts.put(key, overlapCounts.getOrDefault(key, 0) + 1);
        }
        
        // Find max overlap for normalization
        int maxOverlap = overlapCounts.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        // Create overlap count "buckets" for legend
        // We'll create buckets: 1, 2-3, 4-5, 6-10, 11-20, 21+
        ArrayList<OverlapBucket> buckets = new ArrayList<>();
        buckets.add(new OverlapBucket(1, 1, "1 student"));
        buckets.add(new OverlapBucket(2, 3, "2-3 students"));
        buckets.add(new OverlapBucket(4, 5, "4-5 students"));
        buckets.add(new OverlapBucket(6, 10, "6-10 students"));
        buckets.add(new OverlapBucket(11, 20, "11-20 students"));
        buckets.add(new OverlapBucket(21, Integer.MAX_VALUE, "21+ students"));
        
        // Single series for all points
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Student Grades");
        
        // Color each point based on the overlap count
        for (XYChart.Data<Number, Number> point : dataPoints) {
            double x = point.getXValue().doubleValue();
            double y = point.getYValue().doubleValue();
            
            // Get overlap count for this point
            double roundedX = Math.round(x / tolerance) * tolerance;
            double roundedY = Math.round(y / tolerance) * tolerance;
            String key = roundedX + "," + roundedY;
            int overlapCount = overlapCounts.getOrDefault(key, 1);
            
            // Colour calculation
            // Normalization by max overlap and brightness inversion
            double normalizedOverlap = (double) Math.min(overlapCount, maxOverlap) / maxOverlap;
            double brightness = 1.0 - normalizedOverlap;
            brightness = Math.max(0.2, Math.min(1.0, brightness));
            
            // Convert to RGB using #52b5aa as the base color
            int r = (int) (brightness * 82);
            int g = (int) (brightness * 181);
            int b = (int) (brightness * 170);
            
            String colorStyle = String.format("-fx-background-color: rgb(%d, %d, %d);", r, g, b);
            
            // Colour is applied after node creation
            point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(colorStyle);
                }
            });
            
            series.getData().add(point);
        }
        
        scatterChart.getData().add(series);
        ChartDataUtils.addTooltipsToSeries(series, xCourse, yCourse, null);
        
        // Create custom legend
        createOverlapLegend(scatterChart, buckets, maxOverlap);
        
        return scatterChart;
    }
    
    /**
     * Helper class for overlap count buckets
     */
    private static class OverlapBucket {
        int min;
        int max;
        String label;
        
        OverlapBucket(int min, int max, String label) {
            this.min = min;
            this.max = max;
            this.label = label;
        }
    }
    
    /**
     * Custom legend showing overlap count ranges with colored squares
     */
    private static void createOverlapLegend(ScatterChart<Number, Number> chart, ArrayList<OverlapBucket> buckets, int maxOverlap) {
        // Wait for chart to render, then add legend
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                
                // Create legend at bottom
                VBox customLegend = new VBox(5);
                customLegend.setStyle("-fx-padding: 10px; -fx-background-color: white;");
                
                Label legendTitle = new Label("Students per point:");
                legendTitle.setStyle("-fx-font-weight: bold;");
                customLegend.getChildren().add(legendTitle);
                
                HBox legendRow = new HBox(10);
                legendRow.setStyle("-fx-alignment: center-left;");
                
                for (OverlapBucket bucket : buckets) {
                    // Calculate color for bucket (use midpoint)
                    int midOverlap = bucket.max == Integer.MAX_VALUE ? Math.max(bucket.min, maxOverlap) : 
                                    Math.min(bucket.max, Math.max(bucket.min, (bucket.min + bucket.max) / 2));
                    double normalizedOverlap = (double) Math.min(midOverlap, maxOverlap) / maxOverlap;
                    double brightness = 1.0 - normalizedOverlap;
                    brightness = Math.max(0.2, Math.min(1.0, brightness));
                    
                    int r = (int) (brightness * 82);
                    int g = (int) (brightness * 181);
                    int b = (int) (brightness * 170);
                    
                    // Colored square
                    Label colorSquare = new Label();
                    colorSquare.setPrefSize(20, 20);
                    colorSquare.setStyle(String.format("-fx-background-color: rgb(%d, %d, %d); -fx-border-color: black; -fx-border-width: 1px;", r, g, b));
                    
                    // Label
                    Label label = new Label(bucket.label);
                    label.setStyle("-fx-font-size: 12px;");
                    
                    HBox legendItem = new HBox(5);
                    legendItem.getChildren().addAll(colorSquare, label);
                    legendRow.getChildren().add(legendItem);
                }
                
                customLegend.getChildren().add(legendRow);
                
                // Add legend to chart's parent if it's a BorderPane
                Node chartParent = chart.getParent();
                if (chartParent instanceof BorderPane) {
                    ((BorderPane) chartParent).setBottom(customLegend);
                }
            }
        });
    }
}

