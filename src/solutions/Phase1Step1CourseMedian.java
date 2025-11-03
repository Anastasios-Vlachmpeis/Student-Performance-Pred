package solutions;
import datamodels.GraduateGradesModel;

import java.util.Arrays;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1CourseMedian {

    //code testing
    public static void main(String[] args) {
        calcCourseMedian(1);
    }

    public static double calcCourseMedian(int courseId) {
        // Calculate median of grades of a specific course

        // Since grades stores arrays of grades of student's. The array of grades of courses is vertical
        // Let's reconstruct it locally in order to sort, which is necessary for getting the median
        double[] gradesByCourse = new double[GraduateGradesModel.grades.length];    // one course the same number of grades as number of students
        for (int studentId = 0; studentId < GraduateGradesModel.grades.length; studentId++) {
            gradesByCourse[studentId] = GraduateGradesModel.grades[studentId][courseId];
        }
        // Now that we have a local copy sorting it does not mess up grades 2D array
        Arrays.sort(gradesByCourse);

        // Now finding the median branches based on parity of the number of elements
        double median;
        if (gradesByCourse.length % 2 == 1) {
            // When odd, it is exactly the middle element
            median = gradesByCourse[gradesByCourse.length / 2];
        } else {
            // Average of the middle two value
            double middleLeft, middleRight;
            middleRight = gradesByCourse[gradesByCourse.length / 2];
            middleLeft = gradesByCourse[(gradesByCourse.length / 2) - 1];
            median = (middleLeft + middleRight) / 2.0;
        }

        return median;
    }
}
