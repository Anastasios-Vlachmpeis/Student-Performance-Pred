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
        double sum = 0;
        for (int studentId = 0; studentId < GraduateGradesModel.grades.length; studentId++) {
            sum += GraduateGradesModel.grades[studentId][courseId];
        }
        return sum / GraduateGradesModel.grades.length;

    }
}
