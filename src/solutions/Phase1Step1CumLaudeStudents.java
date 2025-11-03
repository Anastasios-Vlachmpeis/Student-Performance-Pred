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
        for (int i = 0; i < GraduateGradesModel.grades.length; i++) {
            if (GraduateGradesModel.calcStudentMean(i) > 8) {
                System.out.println("Student ID: " + i + " (mean grade = " + String.format("%.2f", GraduateGradesModel.calcStudentMean(i)) + ")");
                sumCumLaud++;
            }


        }
        double percentCumLaud = sumCumLaud / GraduateGradesModel.grades.length * 100;
        System.out.println("Total Cum Laude Students : " + (int) sumCumLaud);
        System.out.println("Percentage of Cum Laude Students relative to total graduates : " + String.format("%.2f", percentCumLaud) + "%");


    }
}
