package tools;

import datamodels.*;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Utility class containing shared data calculation and chart generation logic
 * used by ScatterPlotGenerator and JointPlotGenerator only, but can be used by other chart generators.
 * 
 * Methods that can be used by other chart types:
 * - getStudentYValueForFiltered(int studentId, String yAxisData, String selectedCourse, String selectedFeature) -- Calculates the Y-value for a student based on the selected Y-axis data and course/feature
 * - getCourseYValue(int courseId, String yAxisData) -- Calculates the Y-value for a course based on the selected Y-axis data
 * - getPredictedGrade(int studentId, String selectedCourse, String selectedFeature) -- Predicts the grade for a student based on the selected course and feature
 * - findCourseId(String courseName) -- Finds the course ID for a given course name
 * - findFeatureId(String featureName) -- Finds the feature ID for a given feature name
 * - createDecisionStumpForFeature(int courseId, int featureId) -- Creates a decision stump for a given course and feature
 * - getPassingStudents(int courseId) -- Gets the number of passing students for a given course
 * - getNonPassingStudents(int courseId) -- Gets the number of non-passing students for a given course
 * - getCumLaudeStudents(int courseId) -- Gets the number of cum-laude students for a given course
 * - countStudentsByGrade(int courseId, Predicate<Double> condition) -- Counts the number of students for a given course that satisfy a given condition
 * - getFilteredStudentIds(String filterFeature, String filterValue) -- Gets the filtered student IDs based on a given filter feature and value
 * - generateStudentData(XYChart.Series<Number, Number> series, String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) -- Generates student data for a given Y-axis data, filter start/end, and selected course/feature
 * - generateCourseData(XYChart.Series<Number, Number> series, String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd) -- Generates course data for a given Y-axis data, filter start/end
 * - generatePredictedVsActualData(XYChart.Series<Number, Number> predictedSeries, XYChart.Series<Number, Number> actualSeries, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) -- Generates predicted vs actual data for a given Y-axis data, filter start/end, and selected course/feature
 * - addTooltipsToSeries(XYChart.Series<Number, Number> series, String xAxisData, String yAxisData, String courseName) -- Adds tooltips to a given series
 * - createCourseCorrelationChart(String xCourse, String yCourse, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, int[] filteredStudentIds, boolean showYEqualsX) -- Creates a course correlation chart for a given X and Y course, filter start/end, and filtered student IDs
 */
public class ChartDataUtils {
    
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
            
            // Create decision stump and predict grade
            DecisionStump decisionStump = createDecisionStumpForFeature(courseId, featureId);
            return decisionStump.predictGrade(studentId);
        } catch (Exception e) {
            return 0;
        }
    }
    
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
    
    public static DecisionStump createDecisionStumpForFeature(int courseId, int featureId) {
        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        
        // Return default decision stump if no students with grades
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
        
        // Determine split feature (median for numerical, first category for categorical)
        Feature sampleFeature = StudentInfoModel.getFeature(studentIds.get(0), featureId);
        ArrayList<Double> aboveSplit = new ArrayList<>();
        ArrayList<Double> belowSplit = new ArrayList<>();
        
        Feature splitFeature;
        if (sampleFeature instanceof NumericalFeature) {
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
    
    public static int getPassingStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade >= 6.0);
    }
    
    public static int getNonPassingStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade < 6.0);
    }
    
    public static int getCumLaudeStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade > 8.0);
    }
    
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
    
    public static int[] getFilteredStudentIds(String filterFeature, String filterValue) {
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        
        // Return all students if no filter is selected
        if (filterFeature == null || filterFeature.equals("No Feature")) {
            return allStudentIds;
        }
        
        int featureIndex = findFeatureId(filterFeature);
        if (featureIndex == -1) {
            return allStudentIds;
        }
        
        // Filter students based on feature type and value
        ArrayList<Integer> filteredIds = new ArrayList<>();
        
        for (int studentId : allStudentIds) {
            try {
                Feature studentFeature = StudentInfoModel.getFeature(studentId, featureIndex);
                
                boolean matches = false;
                
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
    
    public static void generateStudentData(XYChart.Series<Number, Number> series, String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        for (int i = 0; i < filteredStudentIds.length; i++) {
            int studentId = filteredStudentIds[i];
            double xValue = i;
            double yValue = getStudentYValueForFiltered(studentId, yAxisData, selectedCourse, selectedFeature);
            
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    public static void generateCourseData(XYChart.Series<Number, Number> series, String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd) {
        int startIdx = (int) Math.max(0, Math.ceil(xFilterStart));
        int endIdx = (int) Math.min(CurrentGradesModel.courseCount - 1, Math.floor(xFilterEnd));
        
        for (int i = startIdx; i <= endIdx; i++) {
            double xValue = i;
            double yValue = getCourseYValue(i, yAxisData);
            
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    public static void generatePredictedVsActualData(XYChart.Series<Number, Number> predictedSeries, XYChart.Series<Number, Number> actualSeries, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        int courseId = findCourseId(selectedCourse);
        if (courseId == -1) {
            return;
        }
        
        for (int i = 0; i < filteredStudentIds.length; i++) {
            double xValue = i;
            int studentId = filteredStudentIds[i];
            
            double predictedGrade = getPredictedGrade(studentId, selectedCourse, selectedFeature);
            double actualGrade = CurrentGradesModel.getGrade(studentId, courseId);
            
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                predictedGrade >= yFilterStart && predictedGrade <= yFilterEnd && predictedGrade != -1) {
                predictedSeries.getData().add(new XYChart.Data<>(xValue, predictedGrade));
            }
            
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                actualGrade >= yFilterStart && actualGrade <= yFilterEnd && actualGrade != -1) {
                actualSeries.getData().add(new XYChart.Data<>(xValue, actualGrade));
            }
        }
    }
    
    public static void addTooltipsToSeries(XYChart.Series<Number, Number> series, String xAxisData, String yAxisData, String courseName) {
        for (XYChart.Data<Number, Number> data : series.getData()) {
            String tooltipText = courseName != null && (yAxisData.equals("Predicted Grade") || yAxisData.equals("Actual Grade"))
                ? String.format("%s: %.2f\n%s: %.2f\nCourse: %s", xAxisData, data.getXValue().doubleValue(), yAxisData, data.getYValue().doubleValue(), courseName)
                : String.format("%s: %.2f\n%s: %.2f", xAxisData, data.getXValue().doubleValue(), yAxisData, data.getYValue().doubleValue());
            
            Tooltip tooltip = new Tooltip(tooltipText);
            if (data.getNode() != null) {
                Tooltip.install(data.getNode(), tooltip);
            }
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                }
            });
        }
    }
    
    public static ScatterChart<Number, Number> createCourseCorrelationChart(String xCourse, String yCourse, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd, int[] filteredStudentIds, boolean showYEqualsX) {
        int xCourseId = findCourseId(xCourse);
        int yCourseId = findCourseId(yCourse);
        
        // Create empty chart if courses not found
        if (xCourseId == -1 || yCourseId == -1) {
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel(xCourse);
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(yCourse);
            return new ScatterChart<>(xAxis, yAxis);
        }
        
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xCourse);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yCourse);
        
        // Create the scatter chart
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(yCourse + " vs " + xCourse);
        
        // Add Y=X line if requested
        if (showYEqualsX) {
            XYChart.Series<Number, Number> yEqualsXSeries = new XYChart.Series<>();
            yEqualsXSeries.setName("Y = X Line");
            
            double minRange = Math.max(0, Math.min(xFilterStart, yFilterStart));
            double maxRange = Math.min(10, Math.max(xFilterEnd, yFilterEnd));
            
            int numPoints = 200;
            for (int i = 0; i <= numPoints; i++) {
                double value = minRange + (maxRange - minRange) * i / numPoints;
                XYChart.Data<Number, Number> data = new XYChart.Data<>(value, value);
                yEqualsXSeries.getData().add(data);
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: black; -fx-background-radius: 0.5px; -fx-pref-width: 1px; -fx-pref-height: 1px;");
                    }
                });
            }
            
            scatterChart.getData().add(yEqualsXSeries);
        }
        
        // Add student grade data points
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Student Grades");
        
        for (int studentId : filteredStudentIds) {
            try {
                double xGrade = CurrentGradesModel.getGrade(studentId, xCourseId);
                double yGrade = CurrentGradesModel.getGrade(studentId, yCourseId);
                
                if (xGrade != -1 && yGrade != -1 &&
                    xGrade >= xFilterStart && xGrade <= xFilterEnd &&
                    yGrade >= yFilterStart && yGrade <= yFilterEnd) {
                    series.getData().add(new XYChart.Data<>(xGrade, yGrade));
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        scatterChart.getData().add(series);
        addTooltipsToSeries(series, xCourse, yCourse, null);
        
        return scatterChart;
    }
}

