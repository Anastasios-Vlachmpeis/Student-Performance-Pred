package tools;

import datamodels.CurrentGradesModel;
import datamodels.GraduateGradesModel;

import java.util.ArrayList;
import java.util.List;

public class PearsonCorrelation {

    /**
     * PearsonCorrelation correlation between two course columns courseIdI and j.
     * Compute PearsonCorrelation correlation for a pair of courses, considering only students with grades in both
     * @author Tassos, minor refactoring by Alice
     */
    public static double betweenCurrentCourses(int courseIdI, int courseIdJ) {
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
        double stdA = Statistics.sampleStandardDeviation(gradesA);
        double stdB = Statistics.sampleStandardDeviation(gradesB);

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

    /**
     * PearsonCorrelation correlation between two course columns courseIdI and j.
     * Compute PearsonCorrelation correlation for a pair of courses of the graduate grades dataset.
     * @author Tassos, minor refactoring by Alice (utilization the Statistics class)
     */
    public static double betweenGraduateCourses(int courseIdI, int courseIdJ) {
        double[] courseIGrades = GraduateGradesModel.getAllGradesCourse(courseIdI);
        double[] courseJGrades = GraduateGradesModel.getAllGradesCourse(courseIdJ);

        double meanI = Statistics.mean(courseIGrades);
        double meanJ = Statistics.mean(courseJGrades);

        double stdI = Statistics.sampleStandardDeviation(courseIGrades);
        double stdJ = Statistics.sampleStandardDeviation(courseJGrades);

        int n = courseIGrades.length; //Get total number of students
        if (n <= 1) return Double.NaN; //Stop if there is less than 2
        if (stdI == 0.0 || stdJ == 0.0) return Double.NaN;

        double covSum = 0.0;
        for (int s = 0; s < n; s++) {
            /*
             * measure how 2 courses move together by getting the sum of
             * the multiplication of the difference between the student's
             * grades and the course means
             */
            covSum += (courseIGrades[s] - meanI) * (courseJGrades[s] - meanJ);
        }

        //Compute the sample covariance
        double cov = covSum / (n - 1);
        //Divide the covariance by the product of the standard deviations
        return cov / (stdI * stdJ);
    }
}
