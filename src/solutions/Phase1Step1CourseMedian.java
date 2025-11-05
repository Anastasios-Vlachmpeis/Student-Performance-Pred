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

        // Get all grades for this course using data model method
        double[] gradesByCourse = GraduateGradesModel.getAllGradesCourse(courseId);
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
