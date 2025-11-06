package solutions;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.Arrays;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step2PearsonCorrelationGraduateGrades {

    // this where to test code
    public static void main(String[] args) {
        printTopKCorrelatedCoursePairsIgnoreNG(10);
    }

    /**
     * Q3: "Are there courses that seem similar or related?"
     * Computes Pearson correlation between all pairs of courses,
     * but only considers students who have valid (non-NG) grades
     * in both courses. Displays the top positively correlated course
     * pairs as the most "similar" courses, while skipping NG entries.
     */

    // Compute Pearson correlation for a pair of courses, considering only students with grades in both
    static double pearsonBetweenCoursesIgnoreNG(int i, int j) {
        // Gathers pairs of grades only when both courses have a grade for the same student
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

        // calculate means
        double sumA = 0, sumB = 0;
        for (int k = 0; k < n; k++) {
            sumA += gradesA.get(k);
            sumB += gradesB.get(k);
        }
        double meanA = sumA / n;
        double meanB = sumB / n;

        // calculate std deviations
        double sumSqA = 0, sumSqB = 0;
        for (int k = 0; k < n; k++) {
            double diffA = gradesA.get(k) - meanA;
            double diffB = gradesB.get(k) - meanB;
            sumSqA += diffA * diffA;
            sumSqB += diffB * diffB;
        }
        double stdA = Math.sqrt(sumSqA / (n - 1));
        double stdB = Math.sqrt(sumSqB / (n - 1));

        // avoid division by zero or missing variability
        if (stdA == 0.0 || stdB == 0.0) return Double.NaN;

        // calculate pearson correlation
        double covSum = 0;
        for (int k = 0; k < n; k++) {
            covSum += (gradesA.get(k) - meanA) * (gradesB.get(k) - meanB);
        }
        double cov = covSum / (n - 1);
        return cov / (stdA * stdB);
    }

    static CoursePairCorrelation[] computeAllCourseCorrelationsIgnoreNG() {
        String[] courses = CurrentGradesModel.getCourses();
        final int C = courses.length; //should be courseCount
        ArrayList<CoursePairCorrelation> pairList = new ArrayList<>();

        //Go through every unique unordered pair
        for (int i = 0; i < C; i++) {
            for (int j = i + 1; j < C; j++) {
                double r = pearsonBetweenCoursesIgnoreNG(i, j);
                pairList.add(new CoursePairCorrelation(i, j, r));
            }
        }
        // Convert to array for sorting/printing
        return pairList.toArray(new CoursePairCorrelation[0]);
    }

    public static void printTopKCorrelatedCoursePairsIgnoreNG(int k) {
        CoursePairCorrelation[] pairs = computeAllCourseCorrelationsIgnoreNG();

        // Keep only r > 0 (positive correlations)
        pairs = Arrays.stream(pairs)
                .filter(p -> !Double.isNaN(p.r) && p.r > 0)
                .toArray(CoursePairCorrelation[]::new);

        //Sort by descending r value
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
}
