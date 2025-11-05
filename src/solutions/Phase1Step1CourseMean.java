package solutions;
import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1CourseMean {

    //code testing
    public static void main(String[] args) {
        calcCourseMean(17);
    }

    public static double calcCourseMean(int courseId) {
        // Calculates mean of grades of a specific course
        double[] courseGrades = GraduateGradesModel.getAllGradesCourse(courseId);
        double sum = 0;
        for (int i = 0; i < courseGrades.length; i++) {
            sum += courseGrades[i];
        }
        return sum / courseGrades.length;

    }
}
