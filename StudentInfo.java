public class StudentInfo {
    public StudentInfo(int studentId) {

        if (studentId < 0 || studentId > 21243) {
            System.out.println("Student ID doesn't exist");
            return;
        }

        double[] studentGrades = GraduateGradesModel.grades[studentId];

        //MEAN
        double sum = 0;
        for (int i = 0; i < studentGrades.length; i++) {
            double grade = studentGrades[i];
            sum += grade;
        }

        double mean = sum / GraduateGradesModel.grades.length;
        sum = 0;


        //MODE
        double mode = studentGrades[0];
        int maxCount = 0;

        for (int i = 0; i < studentGrades.length; i++) {
            double current = studentGrades[i];
            int count = 0;

            for (int j = 0; j < studentGrades.length; j++) {
                if (studentGrades[j] == current) {
                    count++;
                }
            }

            if (count > maxCount) {
                maxCount = count;
                mode = current;
            }

        }
            System.out.println("For the student with the studentId " + studentId + ":");
            System.out.println("Mean: " + mean);
            System.out.println("Mode: " + mode);

        }
    }
