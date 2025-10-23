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
        System.out.println(
                Arrays.deepToString(
                        tabulateCourseByStudentFeature(
                                0,
                                new FeatureSplit("SNC", "Harmonized")
                        ).toArray()
                )
        );
        /*
            Testing the second task of step 3 in phase 1.
        */
        System.out.println(
                findBestPropertyToGuessGrade(0).toString()
        );


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
    public static ArrayList<ArrayList<Double>> tabulateCourseByStudentFeature(int courseId, FeatureSplit featureSplit) {
        /*
        * Elements:
        *   0. - mean of subgroup not in splitting criteria
        *   1. - mean of subgroup within splitting criteria
        *   2. - size of subgroup not in splitting criteria
        *   3. - size of subgroup withing splitting criteria
        * */
        double[] tabulation = new double[4];

        // all students' grades in the given course
        int[] studentIds = CurrentGradesModel.getAllStudentIdsOfCourse(courseId);

        ArrayList<Double> subSetSatisfy = new ArrayList<>();
        ArrayList<Double> subSetNotSatisfy = new ArrayList<>();
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
                subSetSatisfy.add(grade);
            } else {
                subSetNotSatisfy.add(grade);
            }
        }

        ArrayList<ArrayList<Double>> results = new ArrayList<>();
        results.add(subSetNotSatisfy);
        results.add(subSetSatisfy);
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
    public static FeatureSplit findBestPropertyToGuessGrade(int courseId) {
        ArrayList<Double> courseGrades = CurrentGradesModel.getAllGradesOfCourse(courseId);

        // calculate initial variance
        double sum = 0;
        for (double courseGrade: courseGrades) {
           sum += courseGrade;
        }
        double initialVariance = sum / courseGrades.size();

        //-------------------------------------------------------------//
        // iterate over all possible splitting options of all features //
        //-------------------------------------------------------------//
        double bestReducedVariance = -1;    // if all splits give a zero reduction then the first one will be the "best"
        FeatureSplit bestSplit = null;

        // first the categorical features (they have index 0, 1, 4)
        int[] categoricalFeatureIndexes = {0, 1, 4};
        for (int featureIndex : categoricalFeatureIndexes) {
            // checks all possible categorical splits of a feature and saves the best
            for (Object element : StudentInfoModel.featureRanges.get(featureIndex)) {
                // this will be used as the splitting criteria
                String featureProperty = (String) element;
                FeatureSplit split = new FeatureSplit(StudentInfoModel.featureNames[featureIndex], featureProperty);
                double reducedVariance = calculateVarianceReduction(
                        tabulateCourseByStudentFeature(courseId, split),
                        initialVariance);

                if (reducedVariance > bestReducedVariance) {
                    bestReducedVariance = reducedVariance;
                    bestSplit = split;
                }
            }
        }
        // Then we check the numerical feature checks.
        // Have to have a stepsize. which will be the range length divided by 1000.
        // The indexes of these features are 2 and 3
        int[] numericalFeatureIndexes = {2, 3};
        for (int featureIndex : numericalFeatureIndexes) {
            // checks all possible categorical splits of a feature and saves the best
            double rangeBottom = (double) StudentInfoModel.featureRanges.get(featureIndex).get(0);
            double rangeTop = (double) StudentInfoModel.featureRanges.get(featureIndex).get(1);
            double stepSize = Math.abs(rangeBottom - rangeTop) / 1000;
            for (double splitThreshold = rangeBottom; splitThreshold < rangeTop; splitThreshold += stepSize) {
                // this will be used as the splitting criteria
                FeatureSplit split = new FeatureSplit(StudentInfoModel.featureNames[featureIndex], splitThreshold);
                double reducedVariance = calculateVarianceReduction(
                        tabulateCourseByStudentFeature(courseId, split),
                        initialVariance);

                if (reducedVariance > bestReducedVariance) {
                    bestReducedVariance = reducedVariance;
                    bestSplit = split;
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
    public static double calculateVarianceReduction(ArrayList<ArrayList<Double>> tabulationResults, double initialVariance) {
        /// Formula for variance reduction:
        ///     VarianceReduction = InitialVariance - sum(subgroupVariance * subgroupSize / totalSize)
        /// Variance for any group (including total dataset before splitting):
        ///     Variance = (sum of (groupMean - individual item)^2 )/ totalSize

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

            // first calculate mean of subgroup
            double sum = 0;
            for (double grade: subGroup) {
                sum += grade;
            }
           double subGroupMeanGrade = sum / subGroup.size();

            // calculate weighted variance of subgroup
            double sumOfSubGroupSquareOfDifferences = 0; //  sum of (groupMean - individual item)^2
            for (double grade: subGroup) {
                sumOfSubGroupSquareOfDifferences += Math.pow((subGroupMeanGrade - grade), 2);
            }
            double subGroupVariance = sumOfSubGroupSquareOfDifferences / subGroup.size();

            // add subgroup variance to the weighted sum
            sumWeightedSubGroupVariances += subGroupVariance * subGroup.size() / sizeBeforeSplit;
        }

        // finish computing variance reduction
        return initialVariance - sumWeightedSubGroupVariances;
    }
}
