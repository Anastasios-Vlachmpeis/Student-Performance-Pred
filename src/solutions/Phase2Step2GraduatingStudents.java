package solutions;

import datamodels.CurrentGradesModel;

public class Phase2Step2GraduatingStudents {

    //Code testing
    public static void main(String[] args) {
        getGraduatingStudents();
    }


    public static int getFailedCourses(int studentId) {
        //This method is used to get the number of failed courses for the given student.
        int count = 0;


        //Go through all the courses for the giving student id and increase the count for every failed course
        for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
            if (CurrentGradesModel.getGrade(studentId, i) < 6.0 && CurrentGradesModel.getGrade(studentId, i) != -1) {
                count++;
            }
        }

        return count;
    }


    public static void getGraduatingStudents() {
        //Q2
        //This method finds the students that are close to graduating by checking if they have any failed courses and their number of NG.
        int count = 0;
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        //Go through all the failed and non graded courses for each student
        for (int i = 0; i < CurrentGradesModel.studentCount; i++) {
            int fails = getFailedCourses(studentIds[i]);
            int ngs = CurrentGradesModel.getStudentNG(i);

            //Increase the count for every student who has less than 5 NG's and no failed course
            if (fails == 0 && ngs < 5) {
                // System.out.println("Possible graduation of the student: " + i);  // DEBUG
                count++;
            }
        }
        System.out.println("Number of expected students to graduate this year: " + count);


    }
}