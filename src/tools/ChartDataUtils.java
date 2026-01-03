package tools;

import datamodels.*;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility class for data calculation and chart generation.
 * Used by ScatterPlotGenerator and JointPlotGenerator.
 */
public class ChartDataUtils {
    
    /** 
     * Calculate the Y-value for a student based on selected Y-axis data type 
     */
    public static double getStudentYValueForFiltered(int studentId, String yAxisData, String selectedCourse, String selectedFeature) {
        return switch (yAxisData) {
            case "Mean" -> {
                double mean = CurrentGradesModel.calcStudentMean(studentId);
                yield mean == -1 ? 0 : mean;
            }
            case "Mode" -> {
                double mode = CurrentGradesModel.calcStudentMode(studentId);
                yield mode == -1 ? 0 : mode;
            }
            case "Median" -> {
                double median = CurrentGradesModel.calcStudentMedian(studentId);
                yield median == -1 ? 0 : median;
            }
            case "Number of NGs" -> {
                // Here we map student ID to index because getStudentNG expects index, not ID
                int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
                int studentIndex = -1;
                for (int i = 0; i < allStudentIds.length; i++) {
                    if (allStudentIds[i] == studentId) {
                        studentIndex = i;
                        break;
                    }
                }
                yield studentIndex >= 0 ? CurrentGradesModel.getStudentNG(studentIndex) : 0;
            }
            case "Predicted Grade" -> getPredictedGrade(studentId, selectedCourse, selectedFeature);
            default -> 0;
        };
    }
    
    /** 
     * Calculate the Y-value for a course based on selected Y-axis data type 
     */
    public static double getCourseYValue(int courseId, String yAxisData) {
        return switch (yAxisData) {
            case "Mean" -> {
                double mean = CurrentGradesModel.calcCourseMean(courseId);
                yield mean == -1 ? 0 : mean;
            }
            case "Mode" -> {
                double mode = CurrentGradesModel.calcCourseMode(courseId);
                yield mode == -1 ? 0 : mode;
            }
            case "Median" -> {
                double median = CurrentGradesModel.calcCourseMedian(courseId);
                yield median == -1 ? 0 : median;
            }
            case "Number of NGs" -> CurrentGradesModel.getCourseNG(courseId);
            case "Number of Passing Students" -> getPassingStudents(courseId);
            case "Number of Non-Passing Students" -> getNonPassingStudents(courseId);
            case "Number of Cum-Laude Students" -> getCumLaudeStudents(courseId);
            default -> 0;
        };
    }
    
    /** 
     * Predict the grade for a student using a decision stump
     * based on the selected course and feature 
     */
    public static double getPredictedGrade(int studentId, String selectedCourse, String selectedFeature) {
        if (selectedCourse == null || selectedFeature == null) {
            return 0;
        }
        
        try {
            int courseId = findCourseId(selectedCourse);
            int featureId = findFeatureId(selectedFeature);
            
            if (courseId == -1 || featureId == -1) {
                return 0;
            }
            
            // Here we create a decision stump for this course/feature combination and use it to predict the grade
            DecisionStump decisionStump = createDecisionStumpForFeature(courseId, featureId);
            return decisionStump.predictGrade(studentId);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /** 
     * Find the course ID for a given course name. Returns -1 if not found. 
     */
    public static int findCourseId(String courseName) {
        if (courseName == null) {
            return -1;
        }
        String[] courses = CurrentGradesModel.getCourses();
        for (int i = 0; i < courses.length; i++) {
            if (courses[i].equals(courseName)) {
                return i;
            }
        }
        return -1;
    }
    
    /** 
     * Find the feature ID for a given feature name. Returns -1 if not found. 
     */
    public static int findFeatureId(String featureName) {
        if (featureName == null) {
            return -1;
        }
        String[] featureNames = StudentInfoModel.featureNames;
        for (int i = 0; i < featureNames.length; i++) {
            if (featureNames[i].equals(featureName)) {
                return i;
            }
        }
        return -1;
    }
    
    /** 
     * We create a decision stump for a given course and feature
     * using median split for numerical features and first category for categorical features
     */
    public static DecisionStump createDecisionStumpForFeature(int courseId, int featureId) {
        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        
        // Here we return a default decision stump if no students have grades for this course
        if (studentIds.isEmpty()) {
            double defaultMean = CurrentGradesModel.getCourseMeansMean();
            int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
            if (allStudentIds.length > 0) {
                Feature defaultFeature = StudentInfoModel.getFeature(allStudentIds[0], featureId);
                return new DecisionStump(defaultFeature, defaultMean, defaultMean);
            } else {
                Feature defaultFeature = new NumericalFeature(featureId, 0.5);
                return new DecisionStump(defaultFeature, defaultMean, defaultMean);
            }
        }
        
        Feature sampleFeature = StudentInfoModel.getFeature(studentIds.get(0), featureId);
        ArrayList<Double> aboveSplit = new ArrayList<>();
        ArrayList<Double> belowSplit = new ArrayList<>();
        
        // Here we determine the split feature: we use median for numerical features, first category for categorical
        Feature splitFeature;
        if (sampleFeature instanceof NumericalFeature) {
            // Calculate median of feature values
            ArrayList<Double> featureValues = new ArrayList<>();
            for (int studentId : studentIds) {
                Feature feature = StudentInfoModel.getFeature(studentId, featureId);
                if (feature instanceof NumericalFeature) {
                    featureValues.add(((NumericalFeature) feature).getValue());
                }
            }
            if (featureValues.isEmpty()) {
                splitFeature = new NumericalFeature(featureId, 0.5);
            } else {
                featureValues.sort(null);
                double medianValue = featureValues.size() % 2 == 0 ?
                    (featureValues.get(featureValues.size() / 2 - 1) + featureValues.get(featureValues.size() / 2)) / 2.0 :
                    featureValues.get(featureValues.size() / 2);
                splitFeature = new NumericalFeature(featureId, medianValue);
            }
        } else {
            String firstCategory = ((CategoricalFeature) sampleFeature).getCategory();
            splitFeature = new CategoricalFeature(featureId, firstCategory);
        }
        
        // Split students into above and below based on split feature
        for (int studentId : studentIds) {
            double grade = CurrentGradesModel.getGrade(studentId, courseId);
            Feature studentFeature = StudentInfoModel.getFeature(studentId, featureId);
            
            if (SplitCondition.evaluate(studentFeature, splitFeature)) {
                aboveSplit.add(grade);
            } else {
                belowSplit.add(grade);
            }
        }
        
        // Calculate mean grades for above and below splits
        double meanAbove = aboveSplit.isEmpty() ? -1 : 
            aboveSplit.stream().mapToDouble(Double::doubleValue).sum() / aboveSplit.size();
        double meanBelow = belowSplit.isEmpty() ? -1 :
            belowSplit.stream().mapToDouble(Double::doubleValue).sum() / belowSplit.size();
        
        // Handle empty splits
        if (meanAbove == -1 && meanBelow == -1) {
            double defaultMean = CurrentGradesModel.getCourseMeansMean();
            meanAbove = meanBelow = defaultMean;
        } else if (meanAbove == -1) {
            meanAbove = meanBelow;
        } else if (meanBelow == -1) {
            meanBelow = meanAbove;
        }
        
        return new DecisionStump(splitFeature, meanAbove, meanBelow);
    }
    
    /** Returns number of passing students (grade >= 6.0) for a given course */
    public static int getPassingStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade >= 6.0);
    }
    
    /** Returns number of non-passing students (grade < 6.0) for a given course */
    public static int getNonPassingStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade < 6.0);
    }
    
    /** Returns number of cum-laude students (grade > 8.0) for a given course */
    public static int getCumLaudeStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade > 8.0);
    }
    
    /** Counts students for a given course that satisfy the provided grade condition */
    public static int countStudentsByGrade(int courseId, Predicate<Double> condition) {
        int count = 0;
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1 && condition.test(grade)) {
                    count++;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return count;
    }
    
    /** 
     * Return filtered student IDs based on selected feature and filter value
     * Return all students if no filter is selected
     */
    public static int[] getFilteredStudentIds(String filterFeature, String filterValue) {
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        
        // Here we return all students if no filter is selected
        if (filterFeature == null || filterFeature.equals("No Feature")) {
            return allStudentIds;
        }
        
        int featureIndex = findFeatureId(filterFeature);
        if (featureIndex == -1) {
            return allStudentIds;
        }
        
        // Here we filter students based on feature type and value
        ArrayList<Integer> filteredIds = new ArrayList<>();
        
        for (int studentId : allStudentIds) {
            try {
                Feature studentFeature = StudentInfoModel.getFeature(studentId, featureIndex);
                
                boolean matches = false;
                
                // Here we check if the student's feature matches the filter: exact match for categorical, threshold for numerical
                if (studentFeature instanceof CategoricalFeature) {
                    String category = ((CategoricalFeature) studentFeature).getCategory();
                    matches = category.equals(filterValue);
                } else if (studentFeature instanceof NumericalFeature) {
                    double value = ((NumericalFeature) studentFeature).getValue();
                    if (filterValue != null) {
                        if (filterValue.equals("All")) {
                            matches = true;
                        } else if (filterValue.startsWith("Above ")) {
                            double threshold = Double.parseDouble(filterValue.substring(6));
                            matches = value > threshold;
                        } else if (filterValue.startsWith("Below ")) {
                            double threshold = Double.parseDouble(filterValue.substring(6));
                            matches = value <= threshold;
                        }
                    }
                }
                
                if (matches) {
                    filteredIds.add(studentId);
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        return filteredIds.stream().mapToInt(i -> i).toArray();
    }
    
    /** 
     * Generate student data points for the chart series, applying X and Y axis filters 
     */
    public static void generateStudentData(XYChart.Series<Number, Number> series, 
        String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, 
        double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        for (int i = 0; i < filteredStudentIds.length; i++) {
            int studentId = filteredStudentIds[i];
            // Here we use the index in the filtered list as the X-value (not the global student ID)
            double xValue = i;
            double yValue = getStudentYValueForFiltered(studentId, yAxisData, selectedCourse, selectedFeature);
            
            // Here we only add the point if it passes both X and Y filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    /** 
     * Generate course data points for the chart series, applying X and Y axis filters 
     */
    public static void generateCourseData(XYChart.Series<Number, Number> series, String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd) {
        // Here we calculate the valid course index range from the filter bounds
        int startIdx = (int) Math.max(0, Math.ceil(xFilterStart));
        int endIdx = (int) Math.min(CurrentGradesModel.courseCount - 1, Math.floor(xFilterEnd));
        
        for (int i = startIdx; i <= endIdx; i++) {
            double xValue = i;
            double yValue = getCourseYValue(i, yAxisData);
            
            // Here we only add the point if it passes both X and Y filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    /** 
     * Generate predicted and actual grade data points for comparison, applying X and Y axis filters 
     */
    public static void generatePredictedVsActualData(XYChart.Series<Number, Number> predictedSeries, XYChart.Series<Number, Number> actualSeries, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        // Here we find the course ID and return early if the course is not found
        int courseId = findCourseId(selectedCourse);
        if (courseId == -1) {
            return;
        }
        
        // Here we generate data points for each filtered student
        for (int i = 0; i < filteredStudentIds.length; i++) {
            double xValue = i;
            int studentId = filteredStudentIds[i];
            
            double predictedGrade = getPredictedGrade(studentId, selectedCourse, selectedFeature);
            double actualGrade = CurrentGradesModel.getGrade(studentId, courseId);
            
            // Here we add the predicted grade point if it passes the filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                predictedGrade >= yFilterStart && predictedGrade <= yFilterEnd && predictedGrade != -1) {
                predictedSeries.getData().add(new XYChart.Data<>(xValue, predictedGrade));
            }
            
            // Here we add the actual grade point if it passes the filters (separate from predicted)
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                actualGrade >= yFilterStart && actualGrade <= yFilterEnd && actualGrade != -1) {
                actualSeries.getData().add(new XYChart.Data<>(xValue, actualGrade));
            }
        }
    }
    
    /** 
     * Add tooltips to all data points in the series showing X and Y axis values 
     */
    public static void addTooltipsToSeries(XYChart.Series<Number, Number> series, String xAxisData, String yAxisData, String courseName) {
        for (XYChart.Data<Number, Number> data : series.getData()) {
            // Here we include the course name in the tooltip for prediction-related data
            String tooltipText = courseName != null && (yAxisData.equals("Predicted Grade") || yAxisData.equals("Actual Grade"))
                ? String.format("%s: %.2f\n%s: %.2f\nCourse: %s", xAxisData, data.getXValue().doubleValue(), yAxisData, data.getYValue().doubleValue(), courseName)
                : String.format("%s: %.2f\n%s: %.2f", xAxisData, data.getXValue().doubleValue(), yAxisData, data.getYValue().doubleValue());
            
            Tooltip tooltip = new Tooltip(tooltipText);
            // Here we install the tooltip immediately if the node exists, otherwise we wait for node creation
            if (data.getNode() != null) {
                Tooltip.install(data.getNode(), tooltip);
            }
            // Here we "listen" for node creation because JavaFX nodes are created lazily
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                }
            });
        }
    }
    
    /** 
     * Create a scatter chart comparing grades between two courses,
     * with the option to show a Y=X reference line 
     */
    public static ScatterChart<Number, Number> createCourseCorrelationChart(String xCourse, String yCourse, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, int[] filteredStudentIds, boolean showYEqualsX) {
        int xCourseId = findCourseId(xCourse);
        int yCourseId = findCourseId(yCourse);
        
        // Here we return an empty chart if neither course is found
        if (xCourseId == -1 || yCourseId == -1) {
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel(xCourse);
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(yCourse);
            return new ScatterChart<>(xAxis, yAxis);
        }
        
        // Here we create the axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xCourse);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yCourse);
        
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(yCourse + " vs " + xCourse);
        
        if (showYEqualsX) {
            XYChart.Series<Number, Number> yEqualsXSeries = new XYChart.Series<>();
            yEqualsXSeries.setName("Y = X Line");
            
            /*
             * We generate Y=X line points by calculating the range from filter bounds,
             * clamped to valid grade range [0, 10]
             */
            double minRange = Math.max(0, Math.min(xFilterStart, yFilterStart));
            double maxRange = Math.min(10, Math.max(xFilterEnd, yFilterEnd));
            
            int numPoints = 200;
            for (int i = 0; i <= numPoints; i++) {
                double value = minRange + (maxRange - minRange) * i / numPoints;
                XYChart.Data<Number, Number> data = new XYChart.Data<>(value, value);
                yEqualsXSeries.getData().add(data);
                // Here we style it as a thin black line (rendered first, so appears below data points)
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: black; -fx-background-radius: 0.5px; -fx-pref-width: 1px; -fx-pref-height: 1px;");
                    }
                });
            }
            
            scatterChart.getData().add(yEqualsXSeries);
        }
        
        // Here we collect all valid data points first
        ArrayList<XYChart.Data<Number, Number>> dataPoints = new ArrayList<>();
        for (int studentId : filteredStudentIds) {
            try {
                double xGrade = CurrentGradesModel.getGrade(studentId, xCourseId);
                double yGrade = CurrentGradesModel.getGrade(studentId, yCourseId);
                
                // Here we only add the point if both grades are valid and within filter ranges
                if (xGrade != -1 && yGrade != -1 &&
                    xGrade >= xFilterStart && xGrade <= xFilterEnd &&
                    yGrade >= yFilterStart && yGrade <= yFilterEnd) {
                    dataPoints.add(new XYChart.Data<>(xGrade, yGrade));
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        // Here we count overlaps using rounded coordinates (tolerance of 0.1 for grade precision)
        Map<String, Integer> overlapCounts = new HashMap<>();
        double tolerance = 0.1;
        
        for (XYChart.Data<Number, Number> point : dataPoints) {
            double x = point.getXValue().doubleValue();
            double y = point.getYValue().doubleValue();
            
            // Round to nearest tolerance value to group nearby points
            double roundedX = Math.round(x / tolerance) * tolerance;
            double roundedY = Math.round(y / tolerance) * tolerance;
            String key = roundedX + "," + roundedY;
            
            overlapCounts.put(key, overlapCounts.getOrDefault(key, 0) + 1);
        }
        
        // Find max overlap count for normalization
        int maxOverlap = overlapCounts.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        // Create overlap count buckets/ranges for legend
        // We'll create buckets: 1, 2-3, 4-5, 6-10, 11-20, 21+
        ArrayList<OverlapBucket> buckets = new ArrayList<>();
        buckets.add(new OverlapBucket(1, 1, "1 student"));
        buckets.add(new OverlapBucket(2, 3, "2-3 students"));
        buckets.add(new OverlapBucket(4, 5, "4-5 students"));
        buckets.add(new OverlapBucket(6, 10, "6-10 students"));
        buckets.add(new OverlapBucket(11, 20, "11-20 students"));
        buckets.add(new OverlapBucket(21, Integer.MAX_VALUE, "21+ students"));
        
        // Create a single series for all points
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Student Grades");
        
        // Color each point based on its overlap count bucket
        for (XYChart.Data<Number, Number> point : dataPoints) {
            double x = point.getXValue().doubleValue();
            double y = point.getYValue().doubleValue();
            
            // Get overlap count for this point
            double roundedX = Math.round(x / tolerance) * tolerance;
            double roundedY = Math.round(y / tolerance) * tolerance;
            String key = roundedX + "," + roundedY;
            int overlapCount = overlapCounts.getOrDefault(key, 1);
            
            // Calculate color based on actual overlap count (normalize by max overlap)
            double normalizedOverlap = (double) Math.min(overlapCount, maxOverlap) / maxOverlap;
            double brightness = 1.0 - normalizedOverlap;
            brightness = Math.max(0.2, Math.min(1.0, brightness));
            
            // Convert to RGB (using blue as base color, adjust brightness)
            int r = (int) (brightness * 100);
            int g = (int) (brightness * 150);
            int b = (int) (brightness * 255);
            
            String colorStyle = String.format("-fx-background-color: rgb(%d, %d, %d);", r, g, b);
            
            // Apply color styling when node is created
            point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(colorStyle);
                }
            });
            
            series.getData().add(point);
        }
        
        scatterChart.getData().add(series);
        addTooltipsToSeries(series, xCourse, yCourse, null);
        
        // Create custom legend similar to heatmap
        createOverlapLegend(scatterChart, buckets, maxOverlap);
        
        return scatterChart;
    }
    
    /**
     * Helper class to represent overlap count buckets
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
     * Create a custom legend showing overlap count ranges with colored squares
     * Similar to the heatmap legend style
     */
    private static void createOverlapLegend(ScatterChart<Number, Number> chart, ArrayList<OverlapBucket> buckets, int maxOverlap) {
        // Wait for chart to be rendered, then style the legend
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Find legend items
                Set<Node> legendItems = chart.lookupAll("Label.chart-legend-item");
                
                // Hide default legend items (we'll create custom ones)
                for (Node legendNode : legendItems) {
                    legendNode.setVisible(false);
                }
                
                // Create custom legend at the bottom
                VBox customLegend = new VBox(5);
                customLegend.setStyle("-fx-padding: 10px; -fx-background-color: white;");
                
                Label legendTitle = new Label("Students per point:");
                legendTitle.setStyle("-fx-font-weight: bold;");
                customLegend.getChildren().add(legendTitle);
                
                HBox legendRow = new HBox(10);
                legendRow.setStyle("-fx-alignment: center-left;");
                
                for (OverlapBucket bucket : buckets) {
                    // Calculate color for this bucket (use midpoint)
                    int midOverlap = bucket.max == Integer.MAX_VALUE ? Math.max(bucket.min, maxOverlap) : 
                                    Math.min(bucket.max, Math.max(bucket.min, (bucket.min + bucket.max) / 2));
                    double normalizedOverlap = (double) Math.min(midOverlap, maxOverlap) / maxOverlap;
                    double brightness = 1.0 - normalizedOverlap;
                    brightness = Math.max(0.2, Math.min(1.0, brightness));
                    
                    int r = (int) (brightness * 100);
                    int g = (int) (brightness * 150);
                    int b = (int) (brightness * 255);
                    
                    // Create colored square
                    Label colorSquare = new Label();
                    colorSquare.setPrefSize(20, 20);
                    colorSquare.setStyle(String.format("-fx-background-color: rgb(%d, %d, %d); -fx-border-color: black; -fx-border-width: 1px;", r, g, b));
                    
                    // Create label
                    Label label = new Label(bucket.label);
                    label.setStyle("-fx-font-size: 12px;");
                    
                    HBox legendItem = new HBox(5);
                    legendItem.getChildren().addAll(colorSquare, label);
                    legendRow.getChildren().add(legendItem);
                }
                
                customLegend.getChildren().add(legendRow);
                
                // Add custom legend to chart's parent if it's a BorderPane
                Node chartParent = chart.getParent();
                if (chartParent instanceof javafx.scene.layout.BorderPane) {
                    ((javafx.scene.layout.BorderPane) chartParent).setBottom(customLegend);
                }
            }
        });
    }
}

