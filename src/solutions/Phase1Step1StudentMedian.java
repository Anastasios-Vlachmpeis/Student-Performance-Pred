package solutions;
import datamodels.GraduateGradesModel;

import java.util.Arrays;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1StudentMedian {

    //code testing
    public static void main(String[] args) {
        calcStudentMedian(17);
    }

    public static double calcStudentMedian(int studentId) {
        // Calculates middle value of grades for a specific student
        double[] studentGrades = GraduateGradesModel.getAllGradesStudent(studentId).clone();

        // Sort the array from the smallest number to the largest one
        Arrays.sort(studentGrades);

        double median;

        if (studentGrades.length % 2 == 1) {
            // If odd number of grades, take the middle one
            median = studentGrades[studentGrades.length / 2];
        } else {
            // If even, average of the two middle grades
            double middleLeft, middleRight;
            middleLeft = studentGrades[(studentGrades.length / 2) - 1];
            middleRight = studentGrades[studentGrades.length / 2];
            median = (middleLeft + middleRight) / 2.0;
        }

        return median;
    }
}
