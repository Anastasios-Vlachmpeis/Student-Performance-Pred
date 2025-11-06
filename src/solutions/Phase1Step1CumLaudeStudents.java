package solutions;

import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1CumLaudeStudents {

    //code testing
    public static void main(String[] args) {
        printCumLaudeStudents();
    }

    public static void printCumLaudeStudents() {

        double sumCumLaud = 0;
        System.out.println("\nThe students graduated cum-laude (above 8 mean grade):");
        int[] studentIds = GraduateGradesModel.getAllStudentIds();
        for (int studentId : studentIds) {
            if (Phase1Step1StudentMean.calcStudentMean(studentId) > 8) {
                System.out.println("Student ID: " + studentId + " (mean grade = " + String.format("%.2f", Phase1Step1StudentMean.calcStudentMean(studentId)) + ")");
                sumCumLaud++;
            }


        }
        double percentCumLaud = sumCumLaud / studentIds.length * 100;
        System.out.println("Total Cum Laude Students : " + (int) sumCumLaud);
        System.out.println("Percentage of Cum Laude Students relative to total graduates : " + String.format("%.2f", percentCumLaud) + "%");


    }
}
