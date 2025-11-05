package solutions;

import datamodels.*;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class holds all methods to answer questions in step 3 and methods required by the project guide
 */
public class Phase1Step3 {

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(guessStudentFuturePerformance(310772)));
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
    private static double[] meanGradesOfTabulation(int courseId, Feature splitFeature) {
        // 2 arrayLists first containing grades below split, second containing grades above split
        ArrayList<Double>[] tabulation = tabulateCourseByStudentFeature(courseId, splitFeature);

        // the mean grade of the two groups after the tabulations
        double[] means = new double[2];

        for (int i = 0; i < 2; i++) {
            ArrayList<Double> subGroup = tabulation[i];
            double sum = 0;
            for (double grade: subGroup) {
               sum += grade;
            }
            // mean of an empty dataset is undefined so we are going to make it a -1
            means[i] = subGroup.isEmpty() ? -1 : sum / subGroup.size();
        }
        return means;
    }

    /**
     * Splits students with grade of a course into two groups, one is above and one is below the splitting criteria
     * @param splitFeature Feature object representing the splitting criteria
     * @return An array of length 2. First index has the grades below the split, second index has the grades above the split
     */
    private static ArrayList<Double>[] tabulateCourseByStudentFeature(int courseId, Feature splitFeature) {

        // all students' ids that have an actual grade (no NG) in the given course
        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

        ArrayList<Double> aboveSplit = new ArrayList<>();
        ArrayList<Double> belowSplit = new ArrayList<>();
        for (int studentId: studentIds) {
            double grade = CurrentGradesModel.getGrade(studentId, courseId);

            Feature studentFeature = StudentInfoModel.getFeature(studentId, splitFeature.getFeatureId());
            // decides if the split condition is satisfied (can handle numeric and category type features as well)
            if (SplitCondition.evaluate(studentFeature, splitFeature)) {
                aboveSplit.add(grade);
            } else {
                belowSplit.add(grade);
            }
        }

        ArrayList<Double>[] results = new ArrayList[] {belowSplit, aboveSplit};
        return results;
    }

    /**
     * Write code that for a given course, finds the best property to help guess the grade
     * a student will get for that course. In this case, you can define best according to a
     * measure called variance reduction.
     * -----------------------------------------------
     * Variance reduction is very simple to information gain, but it calculates variance, and then
     * subtracts the weighted variance of the datasets after the split.
     */
    private static Feature findBestPropertyToGuessGrade(int courseId) {
        ArrayList<Double> courseGrades = CurrentGradesModel.getAllValidGradesCourse(courseId);

        // calculate initial variance
        double sum = 0;
        for (double courseGrade: courseGrades) {
           sum += courseGrade;
        }
        double initialVariance = sum / courseGrades.size();

        //-------------------------------------------------------------//
        // iterate over all possible splitting options of all features //
        //-------------------------------------------------------------//

        // to keep track best feature based on variance reduction
        Feature bestSplit = null;
        double bestVarianceReduction = -1; // if all splits give a zero reduction then the first one will be the "best"

        // go through all known features
        for (int featureId : StudentInfoModel.getAllFeatureIds()) {

            // we need different logic for going through all splitting options
            // based on the type of features

            // numerical feature
            if (NumericalFeature.isIdAllowed(featureId)) {
                double rangeMax = NumericalFeature.getRangeMax(featureId);
                double rangeMin = NumericalFeature.getRangeMin(featureId);
                double STEP_SIZE = (rangeMax - rangeMin) / 1000.0;   // hardcoded value. 3 digits precision
                for (double step = rangeMin; step < rangeMax; step += STEP_SIZE) {
                    NumericalFeature splitFeature = new NumericalFeature(featureId, step);
                    double varianceReduction = calculateVarianceReduction(courseId, splitFeature, initialVariance);

                    if (varianceReduction > bestVarianceReduction) {
                        bestVarianceReduction = varianceReduction;
                        bestSplit = splitFeature;
                    }
                }
            }

            // categorical features
            if (CategoricalFeature.isIdAllowed(featureId)) {
                String[] categoryRange = CategoricalFeature.getRange(featureId);
                for (String splitCategory : categoryRange) {
                    CategoricalFeature splitFeature = new CategoricalFeature(featureId, splitCategory);
                    double varianceReduction = calculateVarianceReduction(courseId, splitFeature, initialVariance);

                    if (varianceReduction > bestVarianceReduction) {
                        bestVarianceReduction = varianceReduction;
                        bestSplit = splitFeature;
                    }
                }
            }


        }

        // now we have the best split
        return bestSplit;
    }

    /**
     * Helper function to calculate variance for tabulations working with the results from
     * tabulateCourseByStudentFeature().
     * So it takes a list of lists that contain the grades of each subpart after splitting
     */
    private static double calculateVarianceReduction(int courseId, Feature splitFeature, double initialVariance) {
        /// Formula for variance reduction:
        ///     VarianceReduction = InitialVariance - sum(subgroupVariance * subgroupSize / totalSize)
        /// Variance for any group (including total dataset before splitting):
        ///     Variance = (sum of (groupMean - individual item)^2 )/ totalSize

        ArrayList<Double>[] tabulationResults = tabulateCourseByStudentFeature(courseId, splitFeature);

        // knowing the size before the split is needed to calculate variance reduction
        int sizeBeforeSplit = 0;
        for (ArrayList<Double> subGroup : tabulationResults) {
            sizeBeforeSplit += subGroup.size();
        }
        // empty dataset's variance cannot be reduced
        if (sizeBeforeSplit == 0) {return 0.0;}


        double sumWeightedSubGroupVariances = 0;
        for (ArrayList<Double> subGroup : tabulationResults) {
            // skip empty splits, as they would not contribute to the weighted subgroup variance sum after all
            if (subGroup.isEmpty()) {continue;}

            double subGroupVariance = calculateVariance(subGroup);

            // add subgroup variance to the weighted sum
            sumWeightedSubGroupVariances += subGroupVariance * subGroup.size() / sizeBeforeSplit;
        }

        // finish computing variance reduction
        return initialVariance - sumWeightedSubGroupVariances;
    }

    /**
     * Calculates variance of a list of numbers.
     * @return -1 when the dataset is empty
     */
    private static double calculateVariance(ArrayList<Double> numbers) {
        if (numbers.isEmpty()) {
            return -1;
        }
        // first calculate the mean
        double sum = 0;
        for (Double num : numbers) {
            sum += num;
        }
        double mean = sum / numbers.size();

        double sumOfSquaredDifferences = 0;
        for (double num : numbers) {
            sumOfSquaredDifferences += Math.pow(mean - num, 2);
        }
        return sumOfSquaredDifferences / numbers.size();
    }

    /** Implementing last question of step 3 of phase 1:
     * Then write code that uses the previous method to produce the best single guess for
     * the future performance of a student.
     * Returns a 2D array where first row contains the courseIds of the future courses, and
     * the second row contains the predicted grade for the given future course.
     * */
    public static double[][] guessStudentFuturePerformance(int studentId) {
        // find future courses first
        ArrayList<Integer> futureCourseIds = new ArrayList<>();
        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
           double grade = CurrentGradesModel.getGrade(studentId, courseId);
           // NG means the student had not taken the exam yet. (NG is encoded as -1.)
           if (grade == -1) {
               futureCourseIds.add(courseId);
           }
        }

        // first row is course ids
        // second row is prediction for course
        double[][] predictions = new double[2][futureCourseIds.size()];
        for (int i = 0; i < futureCourseIds.size(); i++) {
            int courseId = futureCourseIds.get(i);
            predictions[0][i] = courseId;
            predictions[1][i] = findBestDecisionStumpForCourse(courseId).predictGrade(studentId);
        }

        return predictions;
    }

    public static void printBestRulesForGradePrediction() {
        // go through all curses
        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {

            DecisionStump bestDecisionStump = findBestDecisionStumpForCourse(courseId);

            // Print out the whole rule as specified in project manual and enter into new line
            System.out.print(CurrentGradesModel.courses[courseId] + ": " + bestDecisionStump.asRule());
            System.out.println();
        }
    }

    private static DecisionStump findBestDecisionStumpForCourse(int courseId) {
        Feature bestSplit = findBestPropertyToGuessGrade(courseId);
        // this is Y and Z in the rule if X then grade Y, else grade Z (X is the bestSplit)
        double[] tabulatedMeans = meanGradesOfTabulation(courseId, bestSplit);

        // sometimes the best split (according to variance reduction) is to make no split at all
        // then mean of the empty split will be a -1 so we use the other spit's mean
        if (tabulatedMeans[0] == -1) {
            tabulatedMeans[0] = tabulatedMeans[1];
        }
        else if (tabulatedMeans[1] == -1) {
            tabulatedMeans[1] = tabulatedMeans[0];
        }

        // sometimes a course has no grades whatsoever in the current grades
        if (tabulatedMeans[0] == -1 && tabulatedMeans[1] == -1) {
            tabulatedMeans[0] = tabulatedMeans[1] = CurrentGradesModel.getCourseMeansMean();
        }

        // if this was a decision stump this is how it would look
        DecisionStump decisionStump = new DecisionStump(bestSplit, tabulatedMeans[1], tabulatedMeans[0]);


        return decisionStump;
    }
}
