package scatterplot;

import datamodels.*;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import java.util.ArrayList;

public class ScatterPlotGenerator {

    public ScatterChart<Number, Number> createChart(String xAxisData, String yAxisData, 
                                                     double xAxisFilterStart, double xAxisFilterEnd,
                                                     double yAxisFilterStart, double yAxisFilterEnd,
                                                     String selectedCourse, String selectedFeature,
                                                     boolean showActualGrades) {
        
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisData);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisData);
        
        // Create the scatter chart
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        String title = showActualGrades ? "Predicted vs Actual Grades vs " + xAxisData : yAxisData + " vs " + xAxisData;
        scatterChart.setTitle(title);
        
        // Generate data based on axis selections
        if (xAxisData.equals("Per Student")) {
            if (showActualGrades && yAxisData.equals("Predicted Grade")) {
                // Create two series: one for predicted, one for actual
                XYChart.Series<Number, Number> predictedSeries = new XYChart.Series<>();
                predictedSeries.setName("Predicted Grades");
                XYChart.Series<Number, Number> actualSeries = new XYChart.Series<>();
                actualSeries.setName("Actual Grades");
                
                generatePredictedVsActualData(predictedSeries, actualSeries, xAxisFilterStart, xAxisFilterEnd, 
                                             yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature);
                
                scatterChart.getData().add(predictedSeries);
                scatterChart.getData().add(actualSeries);
                
                // Add tooltips to both series
                addTooltipsToSeries(predictedSeries, xAxisData, "Predicted Grade", selectedCourse);
                addTooltipsToSeries(actualSeries, xAxisData, "Actual Grade", selectedCourse);
                
                // Apply colors using inline styles after nodes are created
                applySeriesColors(predictedSeries, actualSeries);
            } else {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName("Data Points");
                generateStudentData(series, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd, selectedCourse, selectedFeature);
                scatterChart.getData().add(series);
                addTooltipsToSeries(series, xAxisData, yAxisData, selectedCourse);
            }
        } else if (xAxisData.equals("Per Course")) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data Points");
            generateCourseData(series, yAxisData, xAxisFilterStart, xAxisFilterEnd, yAxisFilterStart, yAxisFilterEnd);
            scatterChart.getData().add(series);
            addTooltipsToSeries(series, xAxisData, yAxisData, null);
        }
        
        return scatterChart;
    }
    
    private void generateStudentData(XYChart.Series<Number, Number> series, String yAxisData,
                                     double xFilterStart, double xFilterEnd,
                                     double yFilterStart, double yFilterEnd,
                                     String selectedCourse, String selectedFeature) {
        int startIdx = (int) Math.max(0, Math.ceil(xFilterStart));
        int endIdx = (int) Math.min(CurrentGradesModel.studentCount - 1, Math.floor(xFilterEnd));
        
        // Get all student IDs to map indices to global IDs
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        
        for (int i = startIdx; i <= endIdx && i < allStudentIds.length; i++) {
            double xValue = i; // Use index as X value
            int studentId = allStudentIds[i];
            double yValue = getStudentYValue(i, studentId, yAxisData, selectedCourse, selectedFeature);
            
            // Apply filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    private void generatePredictedVsActualData(XYChart.Series<Number, Number> predictedSeries,
                                               XYChart.Series<Number, Number> actualSeries,
                                               double xFilterStart, double xFilterEnd,
                                               double yFilterStart, double yFilterEnd,
                                               String selectedCourse, String selectedFeature) {
        if (selectedCourse == null) {
            return;
        }
        
        // Find the course ID from the course name
        String[] courses = CurrentGradesModel.getCourses();
        int courseId = -1;
        for (int i = 0; i < courses.length; i++) {
            if (courses[i].equals(selectedCourse)) {
                courseId = i;
                break;
            }
        }
        
        if (courseId == -1) {
            return;
        }
        
        int startIdx = (int) Math.max(0, Math.ceil(xFilterStart));
        int endIdx = (int) Math.min(CurrentGradesModel.studentCount - 1, Math.floor(xFilterEnd));
        
        // Get all student IDs to map indices to global IDs
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        
        for (int i = startIdx; i <= endIdx && i < allStudentIds.length; i++) {
            double xValue = i; // Use index as X value
            int studentId = allStudentIds[i];
            
            // Get predicted grade
            double predictedGrade = getPredictedGrade(studentId, selectedCourse, selectedFeature);
            
            // Get actual grade
            double actualGrade = CurrentGradesModel.getGrade(studentId, courseId);
            
            // Add predicted grade point if it passes filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                predictedGrade >= yFilterStart && predictedGrade <= yFilterEnd && predictedGrade != -1) {
                predictedSeries.getData().add(new XYChart.Data<>(xValue, predictedGrade));
            }
            
            // Add actual grade point if it passes filters (only if not NG)
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                actualGrade >= yFilterStart && actualGrade <= yFilterEnd && actualGrade != -1) {
                actualSeries.getData().add(new XYChart.Data<>(xValue, actualGrade));
            }
        }
    }
    
    private void generateCourseData(XYChart.Series<Number, Number> series, String yAxisData,
                                    double xFilterStart, double xFilterEnd,
                                    double yFilterStart, double yFilterEnd) {
        int startIdx = (int) Math.max(0, Math.ceil(xFilterStart));
        int endIdx = (int) Math.min(CurrentGradesModel.courseCount - 1, Math.floor(xFilterEnd));
        
        for (int i = startIdx; i <= endIdx; i++) {
            double xValue = i;
            double yValue = getCourseYValue(i, yAxisData);
            
            // Apply filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    private double getStudentYValue(int studentIndex, int studentId, String yAxisData, 
                                    String selectedCourse, String selectedFeature) {
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
            case "Number of NGs" -> CurrentGradesModel.getStudentNG(studentIndex);
            case "Predicted Grade" -> getPredictedGrade(studentId, selectedCourse, selectedFeature);
            default -> 0;
        };
    }
    
    private double getPredictedGrade(int studentId, String selectedCourse, String selectedFeature) {
        if (selectedCourse == null || selectedFeature == null) {
            return 0;
        }
        
        try {
            // Find the course ID from the course name
            String[] courses = CurrentGradesModel.getCourses();
            int courseId = -1;
            for (int i = 0; i < courses.length; i++) {
                if (courses[i].equals(selectedCourse)) {
                    courseId = i;
                    break;
                }
            }
            
            if (courseId == -1) {
                return 0;
            }
            
            // Get the feature ID from the feature name
            String[] featureNames = StudentInfoModel.featureNames;
            int featureId = -1;
            for (int i = 0; i < featureNames.length; i++) {
                if (featureNames[i].equals(selectedFeature)) {
                    featureId = i; // Feature index matches feature ID in this case
                    break;
                }
            }
            
            if (featureId == -1) {
                return 0;
            }
            
            // Create a decision stump based on the selected feature
            // We'll use the mean of grades for students above and below the feature's median value
            DecisionStump decisionStump = createDecisionStumpForFeature(courseId, featureId);
            
            return decisionStump.predictGrade(studentId);
            
        } catch (Exception e) {
            // If prediction fails, return 0
            return 0;
        }
    }
    
    private DecisionStump createDecisionStumpForFeature(int courseId, int featureId) {
        // Get all students with grades in this course
        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        
        if (studentIds.isEmpty()) {
            // No students with grades, return a default stump
            double defaultMean = CurrentGradesModel.getCourseMeansMean();
            // Get a default feature from any student
            int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
            if (allStudentIds.length > 0) {
                Feature defaultFeature = StudentInfoModel.getFeature(allStudentIds[0], featureId);
                return new DecisionStump(defaultFeature, defaultMean, defaultMean);
            } else {
                // Fallback: create a simple numerical feature
                Feature defaultFeature = new NumericalFeature(featureId, 0.5);
                return new DecisionStump(defaultFeature, defaultMean, defaultMean);
            }
        }
        
        // Get the feature for the first student to determine type
        Feature sampleFeature = StudentInfoModel.getFeature(studentIds.get(0), featureId);
        
        // Split students based on feature and calculate means
        ArrayList<Double> aboveSplit = new ArrayList<>();
        ArrayList<Double> belowSplit = new ArrayList<>();
        
        // Determine split value (median for numerical, or category for categorical)
        Feature splitFeature;
        if (sampleFeature instanceof NumericalFeature) {
            // Use median value of the feature for numerical features
            ArrayList<Double> featureValues = new ArrayList<>();
            for (int studentId : studentIds) {
                Feature feature = StudentInfoModel.getFeature(studentId, featureId);
                if (feature instanceof NumericalFeature) {
                    featureValues.add(((NumericalFeature) feature).getValue());
                }
            }
            if (featureValues.isEmpty()) {
                // Fallback to default value
                splitFeature = new NumericalFeature(featureId, 0.5);
            } else {
                featureValues.sort(null);
                double medianValue = featureValues.size() % 2 == 0 ?
                    (featureValues.get(featureValues.size() / 2 - 1) + featureValues.get(featureValues.size() / 2)) / 2.0 :
                    featureValues.get(featureValues.size() / 2);
                splitFeature = new NumericalFeature(featureId, medianValue);
            }
        } else {
            // For categorical, use the first category as split
            String firstCategory = ((CategoricalFeature) sampleFeature).getCategory();
            splitFeature = new CategoricalFeature(featureId, firstCategory);
        }
        
        // Tabulate grades based on split
        for (int studentId : studentIds) {
            double grade = CurrentGradesModel.getGrade(studentId, courseId);
            Feature studentFeature = StudentInfoModel.getFeature(studentId, featureId);
            
            if (SplitCondition.evaluate(studentFeature, splitFeature)) {
                aboveSplit.add(grade);
            } else {
                belowSplit.add(grade);
            }
        }
        
        // Calculate means
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
    
    private double getCourseYValue(int courseId, String yAxisData) {
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
    
    private int getPassingStudents(int courseId) {
        int count = 0;
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1 && grade >= 6.0) {
                    count++;
                }
            } catch (IllegalArgumentException e) {
                // Skip invalid student IDs
                continue;
            }
        }
        return count;
    }
    
    private int getNonPassingStudents(int courseId) {
        int count = 0;
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1 && grade < 6.0) {
                    count++;
                }
            } catch (IllegalArgumentException e) {
                // Skip invalid student IDs
                continue;
            }
        }
        return count;
    }
    
    private int getCumLaudeStudents(int courseId) {
        int count = 0;
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                // Cum-laude is typically grade > 8.0
                if (grade != -1 && grade > 8.0) {
                    count++;
                }
            } catch (IllegalArgumentException e) {
                // Skip invalid student IDs
                continue;
            }
        }
        return count;
    }
    
    private void applySeriesColors(XYChart.Series<Number, Number> predictedSeries, XYChart.Series<Number, Number> actualSeries) {
        // Apply colors when nodes become available
        predictedSeries.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                // Blue for predicted grades
                newNode.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 3px;");
            }
        });
        
        actualSeries.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                // Red for actual grades
                newNode.setStyle("-fx-background-color: #F44336; -fx-background-radius: 3px;");
            }
        });
        
        // Also style individual data points
        for (XYChart.Data<Number, Number> data : predictedSeries.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 3px;");
                }
            });
        }
        
        for (XYChart.Data<Number, Number> data : actualSeries.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-background-color: #F44336; -fx-background-radius: 3px;");
                }
            });
        }
    }
    
    private void addTooltipsToSeries(XYChart.Series<Number, Number> series, String xAxisData, String yAxisData, String courseName) {
        for (XYChart.Data<Number, Number> data : series.getData()) {
            Number xValue = data.getXValue();
            Number yValue = data.getYValue();
            
            // Format the tooltip text
            String tooltipText;
            if (courseName != null && (yAxisData.equals("Predicted Grade") || yAxisData.equals("Actual Grade"))) {
                tooltipText = String.format("%s: %.2f\n%s: %.2f\nCourse: %s", 
                    xAxisData, xValue.doubleValue(), 
                    yAxisData, yValue.doubleValue(),
                    courseName);
            } else {
                tooltipText = String.format("%s: %.2f\n%s: %.2f", 
                    xAxisData, xValue.doubleValue(), 
                    yAxisData, yValue.doubleValue());
            }
            
            Tooltip tooltip = new Tooltip(tooltipText);
            
            // Install tooltip if node is already available
            if (data.getNode() != null) {
                Tooltip.install(data.getNode(), tooltip);
            }
            
            // Set tooltip when node becomes available (for nodes created later)
            data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                }
            });
        }
    }
}

