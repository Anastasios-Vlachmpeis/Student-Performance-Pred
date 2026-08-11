package GUI.chart;

import datamodels.CurrentGradesModel;
import GUI.style.UIStyling;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tools.ChartDataUtils;

import java.util.Map;
import java.util.TreeMap;

public class HistogramGenerator {

    /**
     * Converts a global student ID to a local StudentId.
     */
    private Integer getStudentIndex(int studentId) {
        int[] allIds = CurrentGradesModel.getAllStudentIds();
        for (int i = 0; i < allIds.length; i++) {
            if (allIds[i] == studentId) return i;
        }
        return null; // Should not happen unless dataset has inconsistencies
    }

    /**
     * Determines the bin width based on the selected X-axis metric.
     * Helps produce readable histograms without overly dense bars.
     */
    private double determineBinWidth(String xAxisData) {
        return switch (xAxisData) {
            case "Mean of Grades" -> 0.2;
            case "Median of Grades" -> 0.5;
            case "Number of NG" -> 6.0;
            case "Number of Passing Students" -> 30.0;
            case "Number of graded Courses" -> 3.0;
            case "Mode of Grades", "Number of failed Courses" -> 1.0;
            default -> 0.5;  // Safe fallback
        };
    }

    /**
     * Creates a histogram BarChart based on the chosen X and Y axis metrics.
     * All logic is performed here.
     */
    public BarChart<String, Number> createChart(
            String xAxisData, String yAxisData,
            double xAxisFilterStart, double xAxisFilterEnd,
            double yAxisFilterStart, double yAxisFilterEnd,
            String filterFeature, String filterValue
    ) {

        // Filter out students based on user-chosen feature settings
        int[] filterStudentIds = ChartDataUtils.getFilteredStudentIds(filterFeature, filterValue);

        // Setup axes for the histogram
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        // Creates Histogram, with title and spacing between bars
        BarChart<String, Number> histogram = new BarChart<>(xAxis, yAxis);
        histogram.setTitle(yAxisData + " with the " + xAxisData);
        histogram.setLegendVisible(false);
        histogram.setCategoryGap(5);
        histogram.setBarGap(0);
        histogram.setAnimated(false);

        // Sets label for X and Y axis
        xAxis.setLabel(xAxisData);
        yAxis.setLabel(yAxisData);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName(yAxisData + " with the " + xAxisData);

        // TreeMap keeps bins sorted numerically by left-bound
        TreeMap<Double, Integer> frequencies = new TreeMap<>();

        // Adds the private determineBinWidth to a double for the following code
        double binWidth = determineBinWidth(xAxisData);

        // Y-axis 1: "Number of Courses"; with the different metrics for the X-axis
        if ("Number of Courses".equals(yAxisData)) {

            // Check if we need to use filtered calculations
            boolean useFiltered = filterFeature != null && !filterFeature.equals("No Feature");

            for (int courseIndex = 0; courseIndex < CurrentGradesModel.courseCount; courseIndex++) {

                double value;

                // Select X-axis metric for the Courses
                try {
                    if (useFiltered) {
                        // Calculate metrics based on filtered students only
                        switch (xAxisData) {
                            case "Mean of Grades" -> value = calcCourseMeanFiltered(courseIndex, filterStudentIds);
                            case "Median of Grades" -> value = calcCourseMedianFiltered(courseIndex, filterStudentIds);
                            case "Mode of Grades" -> value = calcCourseModeFiltered(courseIndex, filterStudentIds);
                            case "Number of NG" -> value = getCourseNGFiltered(courseIndex, filterStudentIds);
                            case "Number of Passing Students" -> value = getPassingStudentsFiltered(courseIndex, filterStudentIds);
                            default -> {
                                continue; // Ignore unsupported combinations
                            }
                        }
                    } else {
                        // Use standard calculations (all students)
                    switch (xAxisData) {
                        case "Mean of Grades" -> value = CurrentGradesModel.calcCourseMean(courseIndex);
                        case "Median of Grades" -> value = CurrentGradesModel.calcCourseMedian(courseIndex);
                        case "Mode of Grades" -> value = CurrentGradesModel.calcCourseMode(courseIndex);
                        case "Number of NG" -> value = CurrentGradesModel.getCourseNG(courseIndex);
                        case "Number of Passing Students" -> value = ChartDataUtils.getPassingStudents(courseIndex);
                        default -> {
                            continue; // Ignore unsupported combinations
                            }
                        }
                    }
                } catch (Exception e){
                        continue; // Skip problematic entries
                    }

                // Apply X-axis filter
                if (value < xAxisFilterStart || value > xAxisFilterEnd) {
                    continue;
                }

                // Compute bin index and left bound
                int binIndex = (int) Math.floor(value / binWidth);
                double left = binIndex * binWidth;

                // Count frequency for this bin
                frequencies.put(left, frequencies.getOrDefault(left, 0) + 1);
            }
        }

        // Y-axis 2: "Number of Students"; with the different metrics for the X-axis
        else if ("Number of Students".equals(yAxisData)) {

            for (int studentId : filterStudentIds) {

                Integer idx = getStudentIndex(studentId);
                if (idx == null) continue; // Should not occur if IDs are valid

                double value;

                // Select X-axis metric for the Students
                try {
                    switch (xAxisData) {
                        case "Mean of Grades" -> value = CurrentGradesModel.calcStudentMean(studentId);
                        case "Median of Grades" -> value = CurrentGradesModel.calcStudentMedian(studentId);
                        case "Mode of Grades" -> value = CurrentGradesModel.calcStudentMode(studentId);
                        case "Number of NG" -> value = CurrentGradesModel.getStudentNG(idx);
                        case "Number of graded Courses" -> {
                            int count = 0;
                            for (double g : CurrentGradesModel.getAllGradesStudent(studentId)) {
                                if (g != -1) count++;
                            }
                            value = count;
                        }
                        case "Number of failed Courses" -> value = CurrentGradesModel.getFailedCourses(idx);
                        default -> {
                            continue; // Unsupported X-axis metric
                        }
                    }
                } catch (Exception e) {
                    continue; // Skip invalid data entries
                }

                // Apply X-axis filtering
                if (value < xAxisFilterStart || value > xAxisFilterEnd) {
                    continue;
                }

                // Compute bin left boundary
                int binIndex = (int) Math.floor(value / binWidth);
                double left = binIndex * binWidth;

                // Count occurrences in this bin
                frequencies.put(left, frequencies.getOrDefault(left, 0) + 1);
            }
        }

        // Creates the bars for the chart, from the sorted bins
        // First pass: collect filtered data and find max frequency
        java.util.ArrayList<XYChart.Data<String, Number>> barData = new java.util.ArrayList<>();
        int maxFrequency = 1;
        
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            double left = entry.getKey();
            double right = left + binWidth;
            int count = entry.getValue();

            // Apply Y-axis frequency filter
            if (count < yAxisFilterStart || count > yAxisFilterEnd) {
                continue;
            }

            // Create readable label
            String label = (binWidth == 1)
                    ? String.format("%d-%d", (int) left, (int) right)
                    : String.format("%.1f-%.1f", left, right);

            // Add bar to histogram
            barData.add(new XYChart.Data<>(label, count));
            maxFrequency = Math.max(maxFrequency, count);
        }
        
        // Store maxFrequency as final for use in lambda
        final int finalMaxFrequency = maxFrequency;
        
        // Second pass: apply coloring based on frequency
        for (XYChart.Data<String, Number> data : barData) {
            int frequency = data.getYValue().intValue();
            
            // Calculate color using centralized method
            String colorStyle = UIStyling.calculateBarColorStyle(frequency, finalMaxFrequency);
            
            // Apply color styling when node is created
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(colorStyle);
                }
            });
            
            series1.getData().add(data);
        }

        histogram.getData().add(series1);
        
        // Style bars after chart is rendered
        histogram.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                for (XYChart.Data<String, Number> data : series1.getData()) {
                    int frequency = data.getYValue().intValue();
                    
                    // Calculate color using centralized method
                    String colorStyle = UIStyling.calculateBarColorStyle(frequency, finalMaxFrequency);
                    
                    if (data.getNode() != null) {
                        data.getNode().setStyle(colorStyle);
                    }
                }
            }
        });
        
        return histogram;
    }

    /**
     * Creates a histogram for a specific course's grades.
     * Used by JointPlotGenerator to avoid duplicating binning logic.
     * 
     * @param courseName The name of the course
     * @param filterStart Minimum grade value to include
     * @param filterEnd Maximum grade value to include
     * @param filteredStudentIds Array of student IDs to include in the histogram
     * @param binWidth Width of each bin (default 0.1 for grades)
     * @param horizontal If true, creates a horizontal histogram (for Y-axis)
     * @return A BarChart histogram of the course grades
     */
    public BarChart<?, ?> createCourseGradeHistogram(
            String courseName,
            double filterStart,
            double filterEnd,
            int[] filteredStudentIds,
            double binWidth,
            boolean horizontal) {
        
        int courseId = ChartDataUtils.findCourseId(courseName);
        if (courseId == -1) {
            // Return empty chart if course not found
            if (horizontal) {
                return new BarChart<>(new NumberAxis(), new CategoryAxis());
            } else {
                return new BarChart<>(new CategoryAxis(), new NumberAxis());
            }
        }

        // Count frequencies for each grade bin
        Map<Double, Integer> frequencies = new TreeMap<>();
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1 && grade >= filterStart && grade <= filterEnd) {
                    double bin = Math.floor(grade / binWidth) * binWidth;
                    frequencies.put(bin, frequencies.getOrDefault(bin, 0) + 1);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        // Find max frequency for normalization
        int maxFrequency = frequencies.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        final int finalMaxFrequency = maxFrequency;

        if (horizontal) {
            // Horizontal histogram (for Y-axis)
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Frequency");
            CategoryAxis yAxis = new CategoryAxis();
            yAxis.setLabel(courseName);
            
            BarChart<Number, String> histogram = new BarChart<>(xAxis, yAxis);
            histogram.setTitle("Y-Axis Distribution");
            histogram.setLegendVisible(false);
            histogram.setCategoryGap(0);
            histogram.setBarGap(0);
            histogram.setAnimated(false);
            
            XYChart.Series<Number, String> series = new XYChart.Series<>();
            Map<String, Integer> frequencyMap = new TreeMap<>();
            
            for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
                String binLabel = String.format("%.1f", entry.getKey());
                frequencyMap.put(binLabel, entry.getValue());
                series.getData().add(new XYChart.Data<>(entry.getValue(), binLabel));
            }
            
            histogram.getData().add(series);
            
            // Style bars after chart is rendered
            histogram.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    for (XYChart.Data<Number, String> data : series.getData()) {
                        String binLabel = data.getYValue();
                        int frequency = frequencyMap.getOrDefault(binLabel, 1);
                        
                        String colorStyle = UIStyling.calculateBarColorStyle(frequency, finalMaxFrequency);
                        data.nodeProperty().addListener((obs2, oldNode, newNode) -> {
                            if (newNode != null) {
                                newNode.setStyle(colorStyle);
                            }
                        });
                        
                        if (data.getNode() != null) {
                            data.getNode().setStyle(colorStyle);
                        }
                    }
                }
            });
            
            return histogram;
        } else {
            // Vertical histogram (for X-axis)
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel(courseName);
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Frequency");
            
            BarChart<String, Number> histogram = new BarChart<>(xAxis, yAxis);
            histogram.setTitle("X-Axis Distribution");
            histogram.setLegendVisible(false);
            histogram.setCategoryGap(0);
            histogram.setBarGap(0);
            histogram.setAnimated(false);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            Map<String, Integer> frequencyMap = new TreeMap<>();
            
            for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
                String binLabel = String.format("%.1f", entry.getKey());
                frequencyMap.put(binLabel, entry.getValue());
                series.getData().add(new XYChart.Data<>(binLabel, entry.getValue()));
            }
            
            histogram.getData().add(series);
            
            // Style bars after chart is rendered
            histogram.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        String binLabel = data.getXValue();
                        int frequency = frequencyMap.getOrDefault(binLabel, 1);
                        
                        String colorStyle = UIStyling.calculateBarColorStyle(frequency, finalMaxFrequency);
                        data.nodeProperty().addListener((obs2, oldNode, newNode) -> {
                            if (newNode != null) {
                                newNode.setStyle(colorStyle);
                            }
                        });
                        
                        if (data.getNode() != null) {
                            data.getNode().setStyle(colorStyle);
                        }
                    }
                }
            });
            
            return histogram;
        }
    }

    /**
     * Calculates mean of grades for a course based on filtered students only.
     */
    private double calcCourseMeanFiltered(int courseId, int[] filteredStudentIds) {
        double sum = 0;
        int gradeCounter = 0;
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1) {
                    sum += grade;
                    gradeCounter++;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return gradeCounter == 0 ? -1 : sum / gradeCounter;
    }

    /**
     * Calculates median of grades for a course based on filtered students only.
     */
    private double calcCourseMedianFiltered(int courseId, int[] filteredStudentIds) {
        java.util.ArrayList<Double> courseGrades = new java.util.ArrayList<>();
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1) {
                    courseGrades.add(grade);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        if (courseGrades.isEmpty()) {
            return -1;
        }
        
        courseGrades.sort(null);
        if (courseGrades.size() % 2 == 1) {
            return courseGrades.get(courseGrades.size() / 2);
        } else {
            int middleRight = courseGrades.size() / 2;
            int middleLeft = middleRight - 1;
            return (courseGrades.get(middleLeft) + courseGrades.get(middleRight)) / 2.0;
        }
    }

    /**
     * Calculates mode of grades for a course based on filtered students only.
     */
    private double calcCourseModeFiltered(int courseId, int[] filteredStudentIds) {
        int[] gradeFrequency = new int[11]; // allows for 0 grade
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1) {
                    gradeFrequency[(int)grade - 1]++;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        
        int indexMostFrequent = 0;
        for (int i = 0; i < gradeFrequency.length; i++) {
            if (gradeFrequency[i] > gradeFrequency[indexMostFrequent]) {
                indexMostFrequent = i;
            }
        }
        
        if (indexMostFrequent == 0 && gradeFrequency[0] == 0) {
            return -1;
        }
        return indexMostFrequent;
    }

    /**
     * Gets number of NG (No Grade) for a course based on filtered students only.
     */
    private int getCourseNGFiltered(int courseId, int[] filteredStudentIds) {
        int count = 0;
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade == -1) {
                    count++;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return count;
    }

    /**
     * Gets number of passing students (grade >= 6.0) for a course based on filtered students only.
     */
    private int getPassingStudentsFiltered(int courseId, int[] filteredStudentIds) {
        int count = 0;
        for (int studentId : filteredStudentIds) {
            try {
                double grade = CurrentGradesModel.getGrade(studentId, courseId);
                if (grade != -1 && grade >= 6.0) {
                    count++;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return count;
    }
}