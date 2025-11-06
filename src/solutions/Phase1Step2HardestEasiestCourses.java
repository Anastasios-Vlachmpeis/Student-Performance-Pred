
// This file shows you how to add your code to the solutions folder (java calls folders packages)
// See Main.java's main method's last section Step 3 how these types of classes are incorporated into the
// codebase.

// make this line as the first line. this is just letting java know that your class is inside the solutions folder
package solutions;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.Arrays;

public class Phase1Step2HardestEasiestCourses {

    // quick manual test
    public static void main(String[] args) {
        printHardestAndEasiestCourses();
    }

    /**
     * Q1 : Which courses are the most difficult/easy?
     * Prints the 5 hardest and 5 easiest courses based on mean grades.
     * If more than 75% of students have NGs for a course,
     * it uses the average of mean and median instead.
     */
    public static void printHardestAndEasiestCourses() {

        String[] courses = CurrentGradesModel.getCourses();
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        int C = courses.length;  // total number of courses
        double[] means = new double[C];
        double[] medians = new double[C];
        int[] ngCounts = new int[C];

        // Compute mean, median, and NG count for all courses
        for (int c = 0; c < C; c++) {
            means[c] = CurrentGradesModel.calcCourseMean(c);
            medians[c] = CurrentGradesModel.calcCourseMedian(c);

            int ngCount = 0;
            for (int studentId : studentIds) {
                if (CurrentGradesModel.getGrade(studentId, c) == -1) ngCount++;
            }
            ngCounts[c] = ngCount;
        }

        // Compute effective score (mean or mean+median if >75% NGs)
        ArrayList<CourseMean> courseMeanList = new ArrayList<>();
        for (int c = 0; c < C; c++) {
            double ngRatio = (double) ngCounts[c] / studentIds.length;
            double effectiveScore;
            if (ngRatio > 0.75) {
                if (!Double.isNaN(medians[c])) {
                    effectiveScore = (means[c] + medians[c]) / 2.0;
                } else {
                    effectiveScore = means[c];
                }
            } else {
                effectiveScore = means[c];
            }
            if (effectiveScore > 0) {
                courseMeanList.add(new CourseMean(c, effectiveScore));
            }
        }
        CourseMean[] courseMeans = courseMeanList.toArray(new CourseMean[0]);

        // Sort by ascending score (hardest first)
        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean));

        // Only output up to 5, or as many as we have
        int outputCount = Math.min(5, courseMeans.length);

        // Print hardest courses
        System.out.println("\nHardest " + outputCount + " courses for current students:");
        for (int i = 0; i < outputCount; i++) {
            int id = courseMeans[i].courseId;
            System.out.println((i + 1) + ") " + CurrentGradesModel.getCourseName(id)
                    + " (mean = " + String.format("%.2f", courseMeans[i].mean) + ")");
        }

        // Print easiest courses
        System.out.println("\nEasiest " + outputCount + " courses for current grades:");
        for (int i = 0; i < outputCount; i++) {
            int idx = courseMeans.length - 1 - i;
            int id = courseMeans[idx].courseId;
            System.out.println((i + 1) + ") " + CurrentGradesModel.getCourseName(id)
                    + " (mean = " + String.format("%.2f", courseMeans[idx].mean) + ")");
        }
    }

    //Helper class that stores a course's mean and id
    static class CourseMean {
        int courseId;
        double mean;
        CourseMean(int courseId, double mean) {
            this.courseId = courseId;
            this.mean = mean;
        }
    }
}
