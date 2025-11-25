package tools;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.List;

public class PearsonCorrelation {

    /**
     * PearsonCorrelation correlation between two course columns courseIdI and j.
     * Compute PearsonCorrelation correlation for a pair of courses, considering only students with grades in both
     * @author Tassos, minor refactoring by Alice
     */
    static double betweenCurrentCourses(int courseIdI, int courseIdJ) {
        // Gathers pairs of grades only when both courses have a grade for the same student
        List<Double> gradesA = new ArrayList<>();
        List<Double> gradesB = new ArrayList<>();
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            double gradeA = CurrentGradesModel.getGrade(studentId, courseIdI);
            double gradeB = CurrentGradesModel.getGrade(studentId, courseIdJ);
            // NG is encoded as -1
            if (gradeA != -1 && gradeB != -1) {
                gradesA.add(gradeA);
                gradesB.add(gradeB);
            }
        }
        int n = gradesA.size();
        if (n <= 1) return Double.NaN;

        // calculate means
        double meanA = Statistics.mean(gradesA);
        double meanB = Statistics.mean(gradesB);

        // calculate std deviations
        double stdA = Statistics.standardDeviation(gradesA);
        double stdB = Statistics.standardDeviation(gradesB);

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
}
