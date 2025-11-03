package solutions;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step2PerformanceHardVsEasyCourses {

    // this where to test code
    public static void main(String[] args) {
        analyzeStudentPerformanceHardVsEasyNG();
    }

    /**
     * Q4: Which students perform significantly better in hard courses compared to easy ones?
     *
     * We use the same logic for determining the 5 hardest and 5 easiest courses (based on
     * course mean/median and NG handling). We only consider students who have valid grades
     * for all 5 hardest and 5 easiest courses.
     */
    public static void analyzeStudentPerformanceHardVsEasyNG() {

        final int C = CurrentGradesModel.courseCount;  // total number of courses
        final int S = CurrentGradesModel.studentCount; // total number of students

        //We compute course means and medians, accounting for NG ratio
        double[] means = new double[C];
        double[] medians = new double[C];
        int[] ngCounts = new int[C];

        for (int c = 0; c < C; c++) {
            means[c] = CurrentGradesModel.calcCourseMean(c);
            medians[c] = CurrentGradesModel.calcCourseMedian(c);
            for (int s = 0; s < S; s++) {
                if (CurrentGradesModel.grades[s][c] == -1) ngCounts[c]++;
            }
        }

        //Adjusted difficulty score for courses with high NG ratios
        ArrayList<CourseMean> courseMeanList = new ArrayList<>();
        for (int c = 0; c < C; c++) {
            double ngRatio = (double) ngCounts[c] / S;
            double effectiveScore = means[c];
            if (ngRatio > 0.75 && !Double.isNaN(medians[c])) {
                effectiveScore = (means[c] + medians[c]) / 2.0;
            }
            if (effectiveScore > 0) {
                courseMeanList.add(new CourseMean(c, effectiveScore));
            }
        }

        CourseMean[] courseMeans = courseMeanList.toArray(new CourseMean[0]);
        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean)); // ascending (hardest first)

        int hardCount = Math.min(5, courseMeans.length / 2);
        int easyCount = Math.min(5, courseMeans.length / 2);

        int[] hardest = new int[hardCount];
        int[] easiest = new int[easyCount];
        for (int i = 0; i < hardCount; i++) hardest[i] = courseMeans[i].courseId;
        for (int i = 0; i < easyCount; i++) easiest[i] = courseMeans[courseMeans.length - 1 - i].courseId;

        //We evaluate each student's relative performance in hard vs easy courses
        ArrayList<StudentPerformanceNG> studentResults = new ArrayList<>();

        for (int s = 0; s < S; s++) {
            double hardSum = 0.0, easySum = 0.0;
            int hardCountValid = 0, easyCountValid = 0;

            //Check hard courses
            for (int i = 0; i < hardCount; i++) {
                double grade = CurrentGradesModel.grades[s][hardest[i]];
                if (grade != -1) {
                    hardSum += (grade - means[hardest[i]]);
                    hardCountValid++;
                }
            }

            //check easy courses
            for (int i = 0; i < easyCount; i++) {
                double grade = CurrentGradesModel.grades[s][easiest[i]];
                if (grade != -1) {
                    easySum += (grade - means[easiest[i]]);
                    easyCountValid++;
                }
            }

            //We only include students who have grades for all 5 hardest & 5 easiest
            if (hardCountValid < hardCount || easyCountValid < easyCount) {
                continue;
            }

            double hardAvg = hardSum / hardCount;
            double easyAvg = easySum / easyCount;
            double diff = hardAvg - easyAvg;

            /**
             * We convert the internal index s to the actual StudentID to prevent NullPointerException.
             */
            int realStudentId = -1;
            for (var entry : CurrentGradesModel.studentID2index.entrySet()) {
                if (entry.getValue() == s) {
                    realStudentId = entry.getKey();
                    break;
                }
            }
            studentResults.add(new StudentPerformanceNG(realStudentId, diff));
        }

        /**
         * Filtering and sorting of students who perform significantly better in hard courses
         */
        List<StudentPerformanceNG> betterStudents = new ArrayList<>();
        for (StudentPerformanceNG sp : studentResults) {
            if (sp.diff > 1) betterStudents.add(sp); // keep only those above threshold
        }

        // Sort by diff descending
        betterStudents.sort((a, b) -> {
            int diffCompare = Double.compare(b.diff, a.diff);   // primary: Δ (descending)
            if (diffCompare != 0) return diffCompare;

            // secondary tiebreaker: overall mean grade (descending)
            double meanA = CurrentGradesModel.calcStudentMean(a.studentId);
            double meanB = CurrentGradesModel.calcStudentMean(b.studentId);
            return Double.compare(meanB, meanA);
        });

        /**
         * Print results (top 10)
         */
        System.out.println("\nTop students performing significantly better in hard courses (Δ > 1.2):");
        int limit = Math.min(10, betterStudents.size());
        for (int i = 0; i < limit; i++) {
            StudentPerformanceNG sp = betterStudents.get(i);
            System.out.println((i + 1) + ") Student " + sp.studentId +
                    " (Δ = " + String.format("%.2f", sp.diff) +
                    ", Mean = " + String.format("%.2f", CurrentGradesModel.calcStudentMean(sp.studentId)) + ")");
        }


        if (studentResults.isEmpty()) {
            System.out.println("No students have complete grades in all 10 courses.");
        }
    }

    /** Helper class for course averages (reused) */
    static class CourseMeanNG {
        int courseId;
        double mean;

        CourseMeanNG(int courseId, double mean) {
            this.courseId = courseId;
            this.mean = mean;
        }
    }

    /** Helper class for storing student performance difference */
    static class StudentPerformanceNG {
        int studentId;
        double diff;

        StudentPerformanceNG(int studentId, double diff) {
            this.studentId = studentId;
            this.diff = diff;
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
