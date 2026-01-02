package solutions;

import datamodels.GraduateGradesModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Consolidated methods for Phase 1 Step 1.
 * Contains all statistical analysis methods for graduate grades.
 */
public class Phase1Step1Methods {

    // ========== Basic Statistics Methods ==========

    /**
     * Calculates mean of grades of a specific course
     */
    public static double calcCourseMean(int courseId) {
        double[] courseGrades = GraduateGradesModel.getAllGradesCourse(courseId);
        double sum = 0;
        for (int i = 0; i < courseGrades.length; i++) {
            sum += courseGrades[i];
        }
        return sum / courseGrades.length;
    }

    /**
     * Calculate median of grades of a specific course
     */
    public static double calcCourseMedian(int courseId) {
        double[] gradesByCourse = GraduateGradesModel.getAllGradesCourse(courseId);
        Arrays.sort(gradesByCourse);

        double median;
        if (gradesByCourse.length % 2 == 1) {
            median = gradesByCourse[gradesByCourse.length / 2];
        } else {
            double middleLeft = gradesByCourse[(gradesByCourse.length / 2) - 1];
            double middleRight = gradesByCourse[gradesByCourse.length / 2];
            median = (middleLeft + middleRight) / 2.0;
        }
        return median;
    }

    /**
     * Calculate mode of grades of a specific course
     */
    public static double calcCourseMode(int courseId) {
        int[] gradeFrequencies = new int[5];
        double[] courseGrades = GraduateGradesModel.getAllGradesCourse(courseId);
        for (int i = 0; i < courseGrades.length; i++) {
            gradeFrequencies[(int)courseGrades[i] - 6] += 1;
        }

        int indexHighest = 0;
        for (int i = 0; i < gradeFrequencies.length; i++) {
            if (gradeFrequencies[indexHighest] < gradeFrequencies[i]) {
                indexHighest = i;
            }
        }
        return indexHighest + 6;
    }

    /**
     * Calculates average of grades for a specific student
     */
    public static double calcStudentMean(int studentId) {
        double[] studentGrades = GraduateGradesModel.getAllGradesStudent(studentId);
        double sum = 0;

        for (int i = 0; i < studentGrades.length; i++) {
            double grade = studentGrades[i];
            sum += grade;
        }

        return sum / studentGrades.length;
    }

    /**
     * Calculates middle value of grades for a specific student
     */
    public static double calcStudentMedian(int studentId) {
        double[] studentGrades = GraduateGradesModel.getAllGradesStudent(studentId).clone();
        Arrays.sort(studentGrades);

        double median;
        if (studentGrades.length % 2 == 1) {
            median = studentGrades[studentGrades.length / 2];
        } else {
            double middleLeft = studentGrades[(studentGrades.length / 2) - 1];
            double middleRight = studentGrades[studentGrades.length / 2];
            median = (middleLeft + middleRight) / 2.0;
        }
        return median;
    }

    /**
     * Calculates the most frequent grade for a specific student
     */
    public static double calcStudentMode(int studentId) {
        double[] studentGrades = GraduateGradesModel.getAllGradesStudent(studentId);
        double mode = studentGrades[0];
        int maxCount = 0;

        for (int i = 0; i < studentGrades.length; i++) {
            double current = studentGrades[i];
            int count = 0;

            for (int j = 0; j < studentGrades.length; j++) {
                if (studentGrades[j] == current) {
                    count++;
                }
            }

            if (count > maxCount) {
                maxCount = count;
                mode = current;
            }
        }
        return mode;
    }

    // ========== Analysis Methods ==========

    /**
     * Prints all students who graduated cum-laude (above 8 mean grade)
     */
    public static void printCumLaudeStudents() {
        double sumCumLaud = 0;
        System.out.println("\nThe students graduated cum-laude (above 8 mean grade):");
        int[] studentIds = GraduateGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            if (calcStudentMean(studentId) > 8) {
                System.out.println("Student ID: " + studentId + " (mean grade = " + String.format("%.2f", calcStudentMean(studentId)) + ")");
                sumCumLaud++;
            }
        }
        double percentCumLaud = sumCumLaud / studentIds.length * 100;
        System.out.println("Total Cum Laude Students : " + (int) sumCumLaud);
        System.out.println("Percentage of Cum Laude Students relative to total graduates : " + String.format("%.2f", percentCumLaud) + "%");
    }

    /**
     * Prints the most difficult and easiest courses based on mean grades
     */
    public static void printBestAndWorstCourse() {
        double bestMean = 0.0;
        double worstMean = 10.0;
        int bestCourseId = -1;
        int worstCourseId = -1;

        String[] courses = GraduateGradesModel.getCourses();
        for (int i = 0; i < courses.length; i++) {
            double mean = calcCourseMean(i);

            if (mean > bestMean) {
                bestMean = mean;
                bestCourseId = i;
            }
            if (mean < worstMean) {
                worstMean = mean;
                worstCourseId = i;
            }
        }

        System.out.println("\nMost difficult and easiest courses:");
        System.out.println("Most difficult course: " + GraduateGradesModel.getCourseName(bestCourseId) + " (mean grade = " + bestMean + ")");
        System.out.println("Easiest course: " + GraduateGradesModel.getCourseName(worstCourseId) + " (mean grade = " + worstMean + ")");
    }

    /**
     * Sample standard deviation of a course's grades
     */
    private static double courseStd(int courseId, double mean) {
        double sumSq = 0.0;
        double[] courseGrades = GraduateGradesModel.getAllGradesCourse(courseId);
        int n = courseGrades.length;
        for (int s = 0; s < n; s++) {
            double diff = courseGrades[s] - mean;
            sumSq += diff * diff;
        }
        if (n <= 1) return 0.0;
        return Math.sqrt(sumSq / (n - 1));
    }

    /**
     * Pearson correlation between two course columns i and j
     */
    private static double pearsonBetweenCourses(int i, double meanI, double stdI,
                                                int j, double meanJ, double stdJ) {
        double[] courseIGrades = GraduateGradesModel.getAllGradesCourse(i);
        double[] courseJGrades = GraduateGradesModel.getAllGradesCourse(j);
        int n = courseIGrades.length;
        if (n <= 1) return Double.NaN;
        if (stdI == 0.0 || stdJ == 0.0) return Double.NaN;

        double covSum = 0.0;
        for (int s = 0; s < n; s++) {
            covSum += (courseIGrades[s] - meanI) * (courseJGrades[s] - meanJ);
        }

        double cov = covSum / (n - 1);
        return cov / (stdI * stdJ);
    }

    /**
     * Build all pair correlations between courses
     */
    private static CoursePairCorrelation[] computeAllCourseCorrelations() {
        String[] courses = GraduateGradesModel.getCourses();
        final int C = courses.length;
        double[] means = new double[C];
        double[] stds  = new double[C];

        for (int c = 0; c < C; c++) {
            means[c] = calcCourseMean(c);
            stds[c]  = courseStd(c, means[c]);
        }

        CoursePairCorrelation[] pairs = new CoursePairCorrelation[(C * (C - 1)) / 2];
        int idx = 0;

        for (int i = 0; i < C; i++) {
            for (int j = i + 1; j < C; j++) {
                double r = pearsonBetweenCourses(i, means[i], stds[i], j, means[j], stds[j]);
                pairs[idx++] = new CoursePairCorrelation(i, j, r);
            }
        }
        return pairs;
    }

    /**
     * Print the top-k most similar course pairs
     */
    public static void printTopKCorrelatedCoursePairs(int k) {
        CoursePairCorrelation[] pairs = computeAllCourseCorrelations();

        pairs = Arrays.stream(pairs)
                .filter(p -> !Double.isNaN(p.r) && p.r > 0)
                .toArray(CoursePairCorrelation[]::new);

        Arrays.sort(pairs, (a, b) -> Double.compare(b.r, a.r));

        int limit = Math.min(k, pairs.length);
        System.out.println("\nTop " + limit + " most similar course pairs:");
        for (int t = 0; t < limit; t++) {
            CoursePairCorrelation p = pairs[t];
            String nameA = GraduateGradesModel.getCourseName(p.courseA);
            String nameB = GraduateGradesModel.getCourseName(p.courseB);

            System.out.println((t + 1) + ") " + nameA + " and " + nameB + " have correlation r = " + p.r);
        }
    }

    /**
     * Analyzes which students perform significantly better in hard courses compared to easy ones.
     * Finds the 5 easiest and 5 hardest courses based on their mean grade, and identifies
     * the top 10 students who performed significantly better in hard courses.
     */
    public static void analyzeStudentPerformanceHardVsEasy() {
        String[] courses = GraduateGradesModel.getCourses();
        int[] studentIds = GraduateGradesModel.getAllStudentIds();
        final int C = courses.length;
        final int S = studentIds.length;

        double[] means = new double[C];
        for (int c = 0; c < C; c++) {
            means[c] = calcCourseMean(c);
        }

        CourseMean[] courseMeans = new CourseMean[C];
        for (int c = 0; c < C; c++) {
            courseMeans[c] = new CourseMean(c, means[c]);
        }

        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean));

        int[] hardest = new int[5];
        int[] easiest = new int[5];
        for (int i = 0; i < 5; i++) {
            hardest[i] = courseMeans[i].courseId;
            easiest[i] = courseMeans[C - 1 - i].courseId;
        }

        System.out.println("\nHardest 5 courses :");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ") " + GraduateGradesModel.getCourseName(hardest[i]) +
                    " (mean = " + courseMeans[i].mean + ")");
        }

        System.out.println("\nEasiest 5 courses :");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ") " + GraduateGradesModel.getCourseName(easiest[i]) +
                    " (mean = " + courseMeans[C - 1 - i].mean + ")");
        }

        StudentPerformance[] studentResults = new StudentPerformance[S];

        for (int s = 0; s < S; s++) {
            int studentId = studentIds[s];
            double hardSum = 0.0;
            double easySum = 0.0;

            for (int i = 0; i < 5; i++) {
                double gradeHard = GraduateGradesModel.getGrade(studentId, hardest[i]);
                double gradeEasy = GraduateGradesModel.getGrade(studentId, easiest[i]);

                hardSum += (gradeHard - means[hardest[i]]);
                easySum += (gradeEasy - means[easiest[i]]);
            }

            double hardAvg = hardSum / 5;
            double easyAvg = easySum / 5;
            double diff = hardAvg - easyAvg;

            studentResults[s] = new StudentPerformance(studentId, diff);
        }

        List<StudentPerformance> betterStudents = new ArrayList<>();
        for (StudentPerformance sp : studentResults) {
            if (sp.diff > 2.0) betterStudents.add(sp);
        }

        betterStudents.sort((a, b) -> {
            int diffCompare = Double.compare(b.diff, a.diff);
            if (diffCompare != 0) return diffCompare;

            double meanA = calcStudentMean(a.studentId);
            double meanB = calcStudentMean(b.studentId);
            return Double.compare(meanB, meanA);
        });

        System.out.println("\nTop 10 students performing significantly better in hard courses:");
        int limit = Math.min(10, betterStudents.size());
        for (int i = 0; i < limit; i++) {
            StudentPerformance sp = betterStudents.get(i);
            System.out.println((i + 1) + ") Student " + sp.studentId + " (Δ = " + sp.diff + ")");
        }
    }

    // ========== Helper Classes ==========

    /**
     * Helper class to store pairs of courses and their correlation r
     */
    static class CoursePairCorrelation {
        int courseA;
        int courseB;
        double r;

        CoursePairCorrelation(int a, int b, double r) {
            this.courseA = a;
            this.courseB = b;
            this.r = r;
        }
    }

    /**
     * Helper class to store courseId + mean pair
     */
    static class CourseMean {
        int courseId;
        double mean;

        CourseMean(int id, double mean) {
            this.courseId = id;
            this.mean = mean;
        }
    }

    /**
     * Helper class to store student performance difference
     */
    static class StudentPerformance {
        int studentId;
        double diff;

        StudentPerformance(int id, double d) {
            this.studentId = id;
            this.diff = d;
        }
    }
}
