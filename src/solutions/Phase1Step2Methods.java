package solutions;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Consolidated methods for Phase 1 Step 2.
 * Contains all statistical analysis methods for current grades (with NG handling).
 */
public class Phase1Step2Methods {

    // ========== Analysis Methods ==========

    /**
     * Q1: Which courses are the most difficult/easy?
     * Prints the 5 hardest and 5 easiest courses based on mean grades.
     * If more than 75% of students have NGs for a course,
     * it uses the average of mean and median instead.
     */
    public static void printHardestAndEasiestCourses() {
        String[] courses = CurrentGradesModel.getCourses();
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        int C = courses.length;
        double[] means = new double[C];
        double[] medians = new double[C];
        int[] ngCounts = new int[C];

        for (int c = 0; c < C; c++) {
            means[c] = CurrentGradesModel.calcCourseMean(c);
            medians[c] = CurrentGradesModel.calcCourseMedian(c);

            int ngCount = 0;
            for (int studentId : studentIds) {
                if (CurrentGradesModel.getGrade(studentId, c) == -1) ngCount++;
            }
            ngCounts[c] = ngCount;
        }

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

        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean));

        int outputCount = Math.min(5, courseMeans.length);

        System.out.println("\nHardest " + outputCount + " courses for current students:");
        for (int i = 0; i < outputCount; i++) {
            int id = courseMeans[i].courseId;
            System.out.println((i + 1) + ") " + CurrentGradesModel.getCourseName(id)
                    + " (mean = " + String.format("%.2f", courseMeans[i].mean) + ")");
        }

        System.out.println("\nEasiest " + outputCount + " courses for current grades:");
        for (int i = 0; i < outputCount; i++) {
            int idx = courseMeans.length - 1 - i;
            int id = courseMeans[idx].courseId;
            System.out.println((i + 1) + ") " + CurrentGradesModel.getCourseName(id)
                    + " (mean = " + String.format("%.2f", courseMeans[idx].mean) + ")");
        }
    }

    /**
     * Q3: "Are there courses that seem similar or related?"
     * Computes Pearson correlation between all pairs of courses,
     * but only considers students who have valid (non-NG) grades
     * in both courses. Displays the top positively correlated course
     * pairs as the most "similar" courses, while skipping NG entries.
     */
    private static double pearsonBetweenCoursesIgnoreNG(int i, int j) {
        ArrayList<Double> gradesA = new ArrayList<>();
        ArrayList<Double> gradesB = new ArrayList<>();
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            double gradeA = CurrentGradesModel.getGrade(studentId, i);
            double gradeB = CurrentGradesModel.getGrade(studentId, j);
            if (gradeA != -1 && gradeB != -1) {
                gradesA.add(gradeA);
                gradesB.add(gradeB);
            }
        }
        int n = gradesA.size();
        if (n <= 1) return Double.NaN;

        double sumA = 0, sumB = 0;
        for (int k = 0; k < n; k++) {
            sumA += gradesA.get(k);
            sumB += gradesB.get(k);
        }
        double meanA = sumA / n;
        double meanB = sumB / n;

        double sumSqA = 0, sumSqB = 0;
        for (int k = 0; k < n; k++) {
            double diffA = gradesA.get(k) - meanA;
            double diffB = gradesB.get(k) - meanB;
            sumSqA += diffA * diffA;
            sumSqB += diffB * diffB;
        }
        double stdA = Math.sqrt(sumSqA / (n - 1));
        double stdB = Math.sqrt(sumSqB / (n - 1));

        if (stdA == 0.0 || stdB == 0.0) return Double.NaN;

        double covSum = 0;
        for (int k = 0; k < n; k++) {
            covSum += (gradesA.get(k) - meanA) * (gradesB.get(k) - meanB);
        }
        double cov = covSum / (n - 1);
        return cov / (stdA * stdB);
    }

    private static CoursePairCorrelation[] computeAllCourseCorrelationsIgnoreNG() {
        String[] courses = CurrentGradesModel.getCourses();
        final int C = courses.length;
        ArrayList<CoursePairCorrelation> pairList = new ArrayList<>();

        for (int i = 0; i < C; i++) {
            for (int j = i + 1; j < C; j++) {
                double r = pearsonBetweenCoursesIgnoreNG(i, j);
                pairList.add(new CoursePairCorrelation(i, j, r));
            }
        }
        return pairList.toArray(new CoursePairCorrelation[0]);
    }

    public static void printTopKCorrelatedCoursePairsIgnoreNG(int k) {
        CoursePairCorrelation[] pairs = computeAllCourseCorrelationsIgnoreNG();

        pairs = Arrays.stream(pairs)
                .filter(p -> !Double.isNaN(p.r) && p.r > 0)
                .toArray(CoursePairCorrelation[]::new);

        Arrays.sort(pairs, (a, b) -> Double.compare(b.r, a.r));

        int limit = Math.min(k, pairs.length);
        System.out.println("\nTop " + limit + " most similar course pairs for current grades:");
        for (int t = 0; t < limit; t++) {
            CoursePairCorrelation p = pairs[t];
            String nameA = CurrentGradesModel.getCourseName(p.courseA);
            String nameB = CurrentGradesModel.getCourseName(p.courseB);

            System.out.println((t + 1) + ") " + nameA + " and " + nameB + " have correlation r = " + String.format("%.3f", p.r));
        }
    }

    /**
     * Q4: Which students perform significantly better in hard courses compared to easy ones?
     * We use the same logic for determining the 5 hardest and 5 easiest courses (based on
     * course mean/median and NG handling). We only consider students who have valid grades
     * for all 5 hardest and 5 easiest courses.
     */
    public static void analyzeStudentPerformanceHardVsEasyNG() {
        final int C = CurrentGradesModel.courseCount;
        final int S = CurrentGradesModel.studentCount;

        double[] means = new double[C];
        double[] medians = new double[C];
        int[] ngCounts = new int[C];

        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int c = 0; c < C; c++) {
            means[c] = CurrentGradesModel.calcCourseMean(c);
            medians[c] = CurrentGradesModel.calcCourseMedian(c);
            for (int studentId : studentIds) {
                if (CurrentGradesModel.getGrade(studentId, c) == -1) ngCounts[c]++;
            }
        }

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
        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean));

        int hardCount = Math.min(5, courseMeans.length / 2);
        int easyCount = Math.min(5, courseMeans.length / 2);

        int[] hardest = new int[hardCount];
        int[] easiest = new int[easyCount];
        for (int i = 0; i < hardCount; i++) hardest[i] = courseMeans[i].courseId;
        for (int i = 0; i < easyCount; i++) easiest[i] = courseMeans[courseMeans.length - 1 - i].courseId;

        ArrayList<StudentPerformanceNG> studentResults = new ArrayList<>();

        for (int studentId : studentIds) {
            double hardSum = 0.0, easySum = 0.0;
            int hardCountValid = 0, easyCountValid = 0;

            for (int i = 0; i < hardCount; i++) {
                double grade = CurrentGradesModel.getGrade(studentId, hardest[i]);
                if (grade != -1) {
                    hardSum += (grade - means[hardest[i]]);
                    hardCountValid++;
                }
            }

            for (int i = 0; i < easyCount; i++) {
                double grade = CurrentGradesModel.getGrade(studentId, easiest[i]);
                if (grade != -1) {
                    easySum += (grade - means[easiest[i]]);
                    easyCountValid++;
                }
            }

            if (hardCountValid < hardCount || easyCountValid < easyCount) {
                continue;
            }

            double hardAvg = hardSum / hardCount;
            double easyAvg = easySum / easyCount;
            double diff = hardAvg - easyAvg;

            studentResults.add(new StudentPerformanceNG(studentId, diff));
        }

        List<StudentPerformanceNG> betterStudents = new ArrayList<>();
        for (StudentPerformanceNG sp : studentResults) {
            if (sp.diff > 1) betterStudents.add(sp);
        }

        betterStudents.sort((a, b) -> {
            int diffCompare = Double.compare(b.diff, a.diff);
            if (diffCompare != 0) return diffCompare;

            double meanA = CurrentGradesModel.calcStudentMean(a.studentId);
            double meanB = CurrentGradesModel.calcStudentMean(b.studentId);
            return Double.compare(meanB, meanA);
        });

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

    /**
     * Answering the final question of step 3: HOW MANY STUDENTS GRADUATE THIS YEAR?
     * Graduation criteria (from graduate grades): have a passing grade from all courses
     * This approach assumes that every current student can graduate this year, and does not consider
     * whether they are in year 1 or year 2 (maybe there are no years system at the alien school even)
     * Run monte carlo simulations for all NGs to decide if they are a failing grade or not, and then
     * count the students eligible for graduation. Repeat, and average out the results.
     */
    public static double predictGraduateAmountMonteCarloSimulation(int numberOfIterations, int maxResitsAllowed) {
        double[] passingRates = calcPassingRates();

        long sumOfGraduates = 0;
        Random random = new Random();
        for (int iteration = 1; iteration <= numberOfIterations; iteration++) {
            int numberOfGraduates = 0;
            for (int studentId : CurrentGradesModel.getAllStudentIds()) {
                int countFailingGrades = 0;
                for (int courseIndex = 0; courseIndex < CurrentGradesModel.courseCount; courseIndex++) {
                    double grade = CurrentGradesModel.getGrade(studentId, courseIndex);
                    boolean isFail = false;
                    if (grade != -1 && grade < 6) {
                        isFail = true;
                    } else {
                        double coursePassingRate = passingRates[courseIndex];
                        if (random.nextDouble() >= coursePassingRate) {
                            isFail = true;
                        }
                    }
                    if (isFail) {
                        isFail = random.nextDouble() >= passingRates[courseIndex];
                    }

                    countFailingGrades += isFail ? 1 : 0;
                }
                if (countFailingGrades == 0) {
                    numberOfGraduates++;
                }
            }

            sumOfGraduates += numberOfGraduates;
        }

        return sumOfGraduates / (double)numberOfIterations;
    }

    private static double[] calcPassingRates() {
        double[] passingRates = new double[CurrentGradesModel.courseCount];
        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
            ArrayList<Double> courseGrades = CurrentGradesModel.getAllValidGradesCourse(courseId);
            int countPassing = 0;
            for (double grade : courseGrades) {
                if (grade >= 6) {
                    countPassing++;
                }
            }
            if (countPassing >= 30) {
                passingRates[courseId] = countPassing / (double) courseGrades.size();
            } else {
                passingRates[courseId] = -1;
            }
        }
        double sumPassingRates = 0;
        int countPassingRates = 0;
        for (int i = 0; i < passingRates.length; i++) {
            if (passingRates[i] == -1) {continue;}
            sumPassingRates += passingRates[i];
            countPassingRates++;
        }
        double meanPassingRate = sumPassingRates / countPassingRates;
        for (int i = 0; i < passingRates.length; i++) {
            if (passingRates[i] == -1) {
                passingRates[i] = meanPassingRate;
            }
        }

        return passingRates;
    }

    // ========== Helper Classes ==========

    /**
     * Helper class that stores a course's mean and id
     */
    static class CourseMean {
        int courseId;
        double mean;
        CourseMean(int courseId, double mean) {
            this.courseId = courseId;
            this.mean = mean;
        }
    }

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
     * Helper class for storing student performance difference
     */
    static class StudentPerformanceNG {
        int studentId;
        double diff;

        StudentPerformanceNG(int studentId, double diff) {
            this.studentId = studentId;
            this.diff = diff;
        }
    }
}
