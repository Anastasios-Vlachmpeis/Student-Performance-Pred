package solutions;
import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1StudentMode {

    //code testing
    public static void main(String[] args) {
        calcStudentMode(17);
    }

    public static double calcStudentMode(int studentId) {
        // Calculates the most frequent for a specific student
        double[] studentGrades = GraduateGradesModel.grades[studentId];
        double mode = studentGrades[0];
        int maxCount = 0;

        // Compare each grade with every other grade and increase the count by one for every similar grade
        for (int i = 0; i < studentGrades.length; i++) {
            double current = studentGrades[i];
            int count = 0;

            for (int j = 0; j < studentGrades.length; j++) {
                if (studentGrades[j] == current) {
                    count++;
                }
            }

            // Update mode if this grade appears more
            if (count > maxCount) {
                maxCount = count;
                mode = current;
            }
        }
        return mode;
    }
}
