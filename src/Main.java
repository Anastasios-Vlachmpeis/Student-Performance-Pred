import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/** Entry point of the project. You can access the data models from here.*/
public class Main {
    public static void main(String[] args) {
        // some computers use commas as the digit separator at floating point numbers.
        // US uses dots.
        Locale.setDefault(Locale.US);

        GraduateGradesModel.loadCSV();
        CurrentGradesModel.loadCSV();
        StudentInfoModel.loadCSV();

        // PUT CODE HERE //
        // You can invoke methods of Model Static Classes
        // or just use them in methods of this class
        /*
         * Q3: Are there courses that seem similar or related?
         * Find top 10 most similar course pairs.
         */
        int TOP_K = 10; // Take the top 10 course pairs with the highest correlation
        GraduateGradesModel.printTopKCorrelatedCoursePairs(TOP_K);

        /**
         * Q4: Which students performed significantly better in the difficult courses, compared to the easy ones?
         * Find the top 10 best performing ones.
         */
        GraduateGradesModel.analyzeStudentPerformanceHardVsEasy();

        System.out.println(CurrentGradesModel.getCourseMean(0));
        /*
        Testing the first task of step 3 in phase 1.
         */
        System.out.println(Arrays.toString(tabulateCourseByStudentFeature(0, "SNC", "Harmonized")));

    }

    /** First Task from Phase 1: Step 3.
     * Write a method that checks the difference in average grade obtained for a given
     * course by students with a specific property. For this, you will need to be able to
     * specify the course as an input to the method, but also a way to define how to separate
     * the students into different groups, e.g., by specifying a property name and a selection
     * or boundary value to apply. You can compare not only average scores, but also the
     * difference in variation between the values.
     * ----------------------------------------------------------------------------------------
     * If student's property IS the boundary value (or above in case of doubles) then it
     * is in the subgroup that satisfies the splitting criteria.
     * */
    public static double[] tabulateCourseByStudentFeature(int courseId, String featureName, String splittingCriteria) {
        double[] tabulation = new double[2];

        // all students' grades in the given course
        int[] studentIds = CurrentGradesModel.getAllStudentIdsOfCourse(courseId);

        int satisfyCounter = 0;
        int notSatisfyCounter = 0;
        double satisfySum = 0;
        double notSatisfySum = 0;
        for (int studentId: studentIds) {
            String property = (String) StudentInfoModel.getFeatureOfStudent(studentId, featureName);
            double grade = CurrentGradesModel.getGrade(studentId, courseId);
            // skip no grades
            if (grade == -1) {
                continue;
            }
            // in this signature we are testing string-ish properties so the criteria is just if they are equal
            if (splittingCriteria.equals(property)) {
                satisfySum += grade;
                satisfyCounter++;
            } else {
                notSatisfySum += grade;
                notSatisfyCounter++;
            }
        }

        // handle cases where one subgroup has no members (would result in division by 0)
        if (notSatisfyCounter == 0) {
            tabulation[0] = -1;
        } else {
            tabulation[0] = notSatisfySum / (double) notSatisfyCounter;
        }
        if (satisfyCounter == 0) {
            tabulation[1] = -1;
        } else {
            tabulation[1] = satisfySum / (double) satisfyCounter;
        }

        System.out.println("(notSatisfySum + satisfySum)/(satisfyCounter + notSatisfyCounter) = " + (notSatisfySum + satisfySum)/(satisfyCounter + notSatisfyCounter));

        return tabulation;
    }

    public static double[] tabulateCourseByStudentFeature(int courseId, String featureName, double splittingCriteria) {
        double[] tabulation = new double[2];

        // all students' grades in the given course
        int[] studentIds = CurrentGradesModel.getAllStudentIdsOfCourse(courseId);

        int satisfyCounter = 0;
        int notSatisfyCounter = 0;
        double satisfySum = 0;
        double notSatisfySum = 0;
        for (int studentId: studentIds) {
            double property = (double) StudentInfoModel.getFeatureOfStudent(studentId, featureName);
            double grade = CurrentGradesModel.getGrade(studentId, courseId);
            // skip no grades
            if (grade == -1) {
                continue;
            }
            // in this signature we are testing string-ish properties so the criteria is just if they are equal
            if (splittingCriteria <= property) {
                satisfySum += grade;
                satisfyCounter++;
            } else {
                notSatisfySum += grade;
                notSatisfyCounter++;
            }
        }

        // handle cases where one subgroup has no members (would result in division by 0)
        if (notSatisfyCounter == 0) {
            tabulation[0] = -1;
        } else {
            tabulation[0] = notSatisfySum / (double) notSatisfyCounter;
        }
        if (satisfyCounter == 0) {
            tabulation[1] = -1;
        } else {
            tabulation[1] = satisfySum / (double) satisfyCounter;
        }

        return tabulation;
    }
}
