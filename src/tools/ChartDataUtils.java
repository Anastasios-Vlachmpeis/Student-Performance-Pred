package tools;

import datamodels.*;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Utility class for data calculation and processing.
 * Used by ScatterPlotGenerator and JointPlotGenerator.
 */
public class ChartDataUtils {
    
    //We get the Y-value of a student based on the selected Y-axis data type
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
                // Student ID mapping to index because getStudentNG expects index, not ID
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
            default -> 0;
        };
    }
    
    //We get the Y-value of a student based on the selected Y-axis data type
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
    
    //Finds the course ID for a given course name or returns -1 if it's not found
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
    
    //Finds the feature ID for a given feature name or returns -1 if it's not found
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
    
    //Decision stump creation for a given course and feature
    //Uses median split for numerical features, first category for categorical features
    public static DecisionStump createDecisionStumpForFeature(int courseId, int featureId) {
        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        
        // Return default decision stump if no students have grades for this course
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
        
        // Determining the split feature;
        // median for numerical, first category for categorical
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
    
    //Returns number of passing students (grade >= 6.0) for a given course
    public static int getPassingStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade >= 6.0);
    }
    
    //Returns number of non-passing students (grade < 6.0) for a given course
    public static int getNonPassingStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade < 6.0);
    }
    
    //Returns number of cum-laude students (grade > 8.0) for a given course
    public static int getCumLaudeStudents(int courseId) {
        return countStudentsByGrade(courseId, grade -> grade > 8.0);
    }
    
    //Counts students for a given course that satisfy the provided grade condition
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
    
    //Returns filtered student IDs based on selected feature and filter value, or
    //Returns all students if no filter has been selected
    public static int[] getFilteredStudentIds(String filterFeature, String filterValue) {
        int[] allStudentIds = CurrentGradesModel.getAllStudentIds();
        
        //Returns all students if no filter has been selected
        if (filterFeature == null || filterFeature.equals("No Feature")) {
            return allStudentIds;
        }
        
        int featureIndex = findFeatureId(filterFeature);
        if (featureIndex == -1) {
            return allStudentIds;
        }
        
        // Filtering students based on the feature's type and value
        ArrayList<Integer> filteredIds = new ArrayList<>();
        
        for (int studentId : allStudentIds) {
            try {
                Feature studentFeature = StudentInfoModel.getFeature(studentId, featureIndex);
                
                boolean matches = false;
                
                // Check if student's feature matches filter;
                // exact match for categorical, threshold for numerical
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
    
    //Generates student data points for the chart series, applying X and Y axis filters
    public static void generateStudentData(XYChart.Series<Number, Number> series, 
        String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, 
        double yFilterEnd, String selectedCourse, String selectedFeature, int[] filteredStudentIds) {
        for (int i = 0; i < filteredStudentIds.length; i++) {
            int studentId = filteredStudentIds[i];
            // Use index in filtered list as X-value (not global student ID)
            double xValue = i;
            double yValue = getStudentYValueForFiltered(studentId, yAxisData, selectedCourse, selectedFeature);
            
            // Only add the point if it passes both of the filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    //Generates course data points for the chart series applying X and Y axis filters
    public static void generateCourseData(XYChart.Series<Number, Number> series, String yAxisData, double xFilterStart, double xFilterEnd, double yFilterStart, double yFilterEnd) {
        // Calculating the valid course index range from filter bounds
        int startIdx = (int) Math.max(0, Math.ceil(xFilterStart));
        int endIdx = (int) Math.min(CurrentGradesModel.courseCount - 1, Math.floor(xFilterEnd));
        
        for (int i = startIdx; i <= endIdx; i++) {
            double xValue = i;
            double yValue = getCourseYValue(i, yAxisData);
            
            // Only add the point if it passes both of the filters
            if (xValue >= xFilterStart && xValue <= xFilterEnd && 
                yValue >= yFilterStart && yValue <= yFilterEnd) {
                series.getData().add(new XYChart.Data<>(xValue, yValue));
            }
        }
    }
    
    //Adds tooltips to all data points in the series showing X and Y axis values
    public static void addTooltipsToSeries(XYChart.Series<Number, Number> series, String xAxisData, String yAxisData, String courseName) {
        for (XYChart.Data<Number, Number> data : series.getData()) {
            String tooltipText = String.format("%s: %.2f\n%s: %.2f", xAxisData, data.getXValue().doubleValue(), yAxisData, data.getYValue().doubleValue());
            
            Tooltip tooltip = new Tooltip(tooltipText);
            // Install tooltip immediately if the node exists, otherwise wait for node creation
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
    
}

