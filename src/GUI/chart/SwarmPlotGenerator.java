package GUI.chart;

import datamodels.CurrentGradesModel;
import datamodels.StudentInfoModel;
import datamodels.CategoricalFeature;
import datamodels.Feature;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Circle;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import java.util.*;

public class SwarmPlotGenerator {
    
    //Colors
    private static final Color[] CATEGORY_COLORS = {
        Color.rgb(127, 127, 127),
        Color.rgb(148, 103, 189),
        Color.rgb(82, 181, 170),
        Color.rgb(44, 160, 44), 
        Color.rgb(214, 39, 40)

    };
    
    private Random random = new Random();

    public ScatterChart<String, Number> createChart(int courseId, String featureName) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Feature Categories");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Grades");
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(10);
        yAxis.setTickUnit(1);

        ScatterChart<String, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle("Grade Distribution: " + CurrentGradesModel.getCourseName(courseId) + 
                           " grouped by " + featureName);
        scatterChart.setLegendVisible(false);

        //Feature ID
        int featureId = -1;
        String[] featureNames = StudentInfoModel.featureNames;
        for (int i = 0; i < featureNames.length; i++) {
            if (featureNames[i].equals(featureName)) {
                featureId = i;
                break;
            }
        }

        if (featureId == -1 || !CategoricalFeature.isIdAllowed(featureId)) {
            scatterChart.setTitle("Error: Invalid categorical feature");
            return scatterChart;
        }

        //Categories
        String[] categories = CategoricalFeature.getRange(featureId);
        
        //Students by category
        Map<String, List<Double>> categoryGrades = new HashMap<>();
        for (String category : categories) {
            categoryGrades.put(category, new ArrayList<>());
        }

        //Students with grades
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : allStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1) { // Skip NGs
                    Feature studentFeature = StudentInfoModel.getFeature(studentId, featureId);
                    if (studentFeature instanceof CategoricalFeature) {
                        String category = ((CategoricalFeature) studentFeature).getCategory();
                        categoryGrades.get(category).add(grade);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }

        //Series per category
        int colorIndex = 0;
        for (String category : categories) {
            List<Double> grades = categoryGrades.get(category);
            if (grades.isEmpty()) continue;

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(category);
            
            Color categoryColor = CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length];
            colorIndex++;
            
            for (Double grade : grades) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(category, grade);
                
                // Create the points
                Circle circle = new Circle(2.9);
                circle.setFill(categoryColor.deriveColor(0, 1, 1, 0.5));
                circle.setStroke(categoryColor.deriveColor(0, 1, 0.8, 0.8));
                circle.setStrokeWidth(0.5);
                
                data.setNode(circle);
                series.getData().add(data);
            }
            
            scatterChart.getData().add(series);
        }

        //Swarm layout
        scatterChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyBeesSwarmLayout(scatterChart, 2);
            }
        });
        return scatterChart;
    }

    private void applyBeesSwarmLayout(ScatterChart<String, Number> chart, double radius) {
        Map<String, List<XYChart.Data<String, Number>>> grouped = new HashMap<>();

        // Group data points by category
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                String category = data.getXValue();
                grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(data);
            }
        }

        // Apply swarm layout to each category
        for (String category : grouped.keySet()) {
            List<XYChart.Data<String, Number>> points = grouped.get(category);
            points.sort(Comparator.comparingDouble(p -> p.getYValue().doubleValue()));

            List<Point2D> placed = new ArrayList<>();
            for (XYChart.Data<String, Number> p : points) {
                if (p.getNode() == null) continue;
                double y = p.getYValue().doubleValue();
                //Jitter
                double jitterAmount = 0.008; //random offset
                double jitteredY = y + (random.nextDouble() - 0.5) * jitterAmount;
                
                double xOffset = 0;
                double step = radius * 0.9;
                // Position without collision
                int maxAttempts = 50; // Prevent infinite loops
                int attempts = 0;
                while (attempts < maxAttempts) {
                    boolean collision = false;
                    for (Point2D pos : placed) {
                        // Collision detection
                        if (Math.hypot(pos.getX() - xOffset, pos.getY() - jitteredY) < radius) {
                            collision = true;
                            break;
                        }
                    }
                    if (!collision) break;
                    
                    // Spread pattern
                    if (xOffset >= 0) {
                        xOffset = -(Math.abs(xOffset) + step);
                    } else {
                        xOffset = Math.abs(xOffset) + step;
                    }
                    attempts++;
                }
                
                placed.add(new Point2D(xOffset, jitteredY));
                p.getNode().setTranslateX(xOffset);
                p.getNode().setTranslateY(jitteredY - y);
            }
        }
    }
}