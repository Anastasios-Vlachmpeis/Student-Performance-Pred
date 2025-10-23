import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

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
    public static double[] tabulateCourseByStudentFeature(int courseId, FeatureSplit featureSplit) {
        double[] tabulation = new double[2];

        // all students' grades in the given course
        int[] studentIds = CurrentGradesModel.getAllStudentIdsOfCourse(courseId);

        int satisfyCounter = 0;
        int notSatisfyCounter = 0;
        double satisfySum = 0;
        double notSatisfySum = 0;
        for (int studentId: studentIds) {
            // property can be String and double depending on type of feature
            var property = StudentInfoModel.getFeatureOfStudent(studentId, featureSplit.name);
            double grade = CurrentGradesModel.getGrade(studentId, courseId);

            // skip no grades
            if (grade == -1) {
                continue;
            }
            // splitting criteria depends on the type of the feature
            boolean isSplitConditionSatisfied;
            if (featureSplit.isFeatureACategory) {
                isSplitConditionSatisfied = featureSplit.selectionCategory.equals((String) property);
            } else {
                isSplitConditionSatisfied = featureSplit.threshHoldValue > (double) property;
            }

            // evaluate splitting criteria
            if (isSplitConditionSatisfied) {
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
}
