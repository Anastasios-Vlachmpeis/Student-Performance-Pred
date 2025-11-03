package solutions;
import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1StudentMean {

    //code testing
    public static void main(String[] args) {
        calcStudentMean(17);
    }

    public static double calcStudentMean(int studentId) {
        // Calculates average of grades for a specific student
        double[] studentGrades = GraduateGradesModel.grades[studentId];
        double sum = 0;

        // Sum all grades of the student
        for (int i = 0; i < studentGrades.length; i++) {
            double grade = studentGrades[i];
            sum += grade;
        }

        // Divide total sum by number of grades to get mean
        double mean = sum / studentGrades.length;

        return mean;
    }
}
