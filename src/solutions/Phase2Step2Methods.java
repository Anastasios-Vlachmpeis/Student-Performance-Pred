package solutions;

import datamodels.CurrentGradesModel;

/**
 * Consolidated methods for Phase 2 Step 2.
 * Contains methods for analyzing graduating students.
 */
public class Phase2Step2Methods {

    /**
     * Gets the number of failed courses for the given student.
     */
    public static int getFailedCourses(int studentId) {
        int count = 0;

        for (int i = 0; i < CurrentGradesModel.courseCount; i++) {
            if (CurrentGradesModel.getGrade(studentId, i) < 6.0 && CurrentGradesModel.getGrade(studentId, i) != -1) {
                count++;
            }
        }

        return count;
    }

    /**
     * Finds the students that are close to graduating by checking if they have any failed courses
     * and their number of NG. Students with less than 5 NGs and no failed courses are considered
     * eligible for graduation.
     */
    public static void getGraduatingStudents() {
        int count = 0;
        int[] studentIds = CurrentGradesModel.getAllStudentIds();
        for (int i = 0; i < CurrentGradesModel.studentCount; i++) {
            int fails = getFailedCourses(studentIds[i]);
            int ngs = CurrentGradesModel.getStudentNG(i);

            if (fails == 0 && ngs < 5) {
                count++;
            }
        }
        System.out.println("Number of expected students to graduate this year: " + count);
    }
}
