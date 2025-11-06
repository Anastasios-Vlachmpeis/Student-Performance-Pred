package solutions;
import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1CourseMode {

    //code testing
    public static void main(String[] args) {
        calcCourseMode(17);
    }

    public static double calcCourseMode(int courseId) {
        // Calculate mode of grades of a specific course
        double mode;
        // Count the frequencies of all grades (6.0, 7.0, 8.0, 9.0, 10.0)
        // index 0 is 6.0, index 1 is 7.0, etc.
        // So we need to subtract 6 from the grade to get its corresponding index in the
        int[] gradeFrequencies = new int[5];
        double[] courseGrades = GraduateGradesModel.getAllGradesCourse(courseId);
        for (int i = 0; i < courseGrades.length; i++) {
            gradeFrequencies[(int)courseGrades[i] - 6] += 1;
        }

        // Searches highest frequency
        int indexHighest = 0;
        for (int i = 0; i < gradeFrequencies.length; i++) {
            if (gradeFrequencies[indexHighest] < gradeFrequencies[i]) {
                indexHighest = i;
            }
        }
        mode = indexHighest + 6;    // automatically cast as double

        return mode;
    }

}
