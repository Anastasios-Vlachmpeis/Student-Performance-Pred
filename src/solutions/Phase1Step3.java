package solutions;

import datamodels.*;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class holds all methods to answer questions in step 3 and methods required by the project guide
 */
public class Phase1Step3 {

    public static void main(String[] args) {
        //System.out.println(Arrays.deepToString(guessStudentFuturePerformance(310772)));
        testVarianceReductionForest(25);
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
            System.out.print(CurrentGradesModel.getCourseName(courseId) + ": " + bestDecisionStump.asRule());
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
     /**
      * Phase 1 Step 4: Build a forest to reduce prediction variance
      * We pick a maximum of 10 decision stumps for a course, that
      * when averaged together make predictions with the lowest variance
      *
      * It works by starting with no stumps and adding them one by one,
      * and each time, we add the stump that helps lower the variance the most
      *
      * @param allStumps all possible DecisionStump objects for the course
      * @param courseId the course id
      * @return about 10 DecisionStump objects chosen to lower variance
      */
    public static DecisionStump[] buildVarianceReductionForest(DecisionStump[] allStumps, int courseId) {
        // handle edge case where no input stumps
        if (allStumps == null || allStumps.length == 0) {
            return new DecisionStump[0];
        }

        // set target forest size (max 10)
        int targetSize = Math.min(10, allStumps.length);

        // get ids of students who have grades for this course
        ArrayList<Integer> studentsWithGrades = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        
        // return empty if no students found
        if (studentsWithGrades.isEmpty()) {
            return new DecisionStump[0];
        }

        // track selected stumps for the forest
        ArrayList<DecisionStump> selectedStumps = new ArrayList<>();
        boolean[] used = new boolean[allStumps.length];

        // greedy selection loop
        for (int iteration = 0; iteration < targetSize; iteration++) {
            DecisionStump bestStump = null;
            int bestStumpIndex = -1;
            double bestVariance = Double.MAX_VALUE;

            // check each unused stump
            for (int i = 0; i < allStumps.length; i++) {
                if (used[i]) continue;

                // test forest with this stump added
                ArrayList<DecisionStump> testForest = new ArrayList<>(selectedStumps);
                testForest.add(allStumps[i]);

                // compute variance for this option
                double variance = calculateVarianceOfAveragedPredictions(testForest, studentsWithGrades);

                // remember lowest-variance option
                if (variance < bestVariance) {
                    bestVariance = variance;
                    bestStump = allStumps[i];
                    bestStumpIndex = i;
                }
            }

            // add best stump or stop if none left
            if (bestStump != null) {
                selectedStumps.add(bestStump);
                used[bestStumpIndex] = true;
            } else {
                break;
            }
        }

        // convert list to array
        DecisionStump[] result = new DecisionStump[selectedStumps.size()];
        for (int i = 0; i < selectedStumps.size(); i++) {
            result[i] = selectedStumps.get(i);
        }

        return result;
    }

    /**
     * Calculates the variance of averaged predictions from a forest of decision stumps.
     * For each student, averages predictions from all stumps in the forest, then calculates
     * variance of these averaged predictions across all students.
     * 
     * @param forest List of DecisionStump objects in the forest
     * @param studentIds List of student IDs to calculate predictions for
     * @return Variance of the averaged predictions, or -1 if forest is empty or no students
     */
    private static double calculateVarianceOfAveragedPredictions(ArrayList<DecisionStump> forest, ArrayList<Integer> studentIds) {
        // handle empty forest or students
        if (forest == null || forest.isEmpty() || studentIds == null || studentIds.isEmpty()) {
            return -1;
        }

        // store average prediction per student
        ArrayList<Double> averagedPredictions = new ArrayList<>();

        for (int studentId : studentIds) {
            // sum predictions for all stumps
            double sumPredictions = 0;
            for (DecisionStump stump : forest) {
                sumPredictions += stump.predictGrade(studentId);
            }

            // average the predictions per student
            double averagedPrediction = sumPredictions / forest.size();
            averagedPredictions.add(averagedPrediction);
        }

        // get variance of these averages
        return calculateVariance(averagedPredictions);
    }

    /**
     * Helper that converts a split feature into a fully specified decision stump for the given course.
     * Returns null when the split does not produce meaningful predictions (no grades available).
     */
    private static DecisionStump buildDecisionStumpForSplit(int courseId, Feature splitFeature) {
        // Get mean grades for below and above the split
        double[] tabulatedMeans = meanGradesOfTabulation(courseId, splitFeature);

        // If no grades exist for either split, return null (no prediction possible)
        if (tabulatedMeans[0] == -1 && tabulatedMeans[1] == -1) {
            return null;
        }
        // Fill in missed means so both splits have values
        if (tabulatedMeans[0] == -1) {
            tabulatedMeans[0] = tabulatedMeans[1];
        }
        if (tabulatedMeans[1] == -1) {
            tabulatedMeans[1] = tabulatedMeans[0];
        }

        // Create a decision stump from the split feature and the two means
        return new DecisionStump(splitFeature, tabulatedMeans[1], tabulatedMeans[0]);
    }

    /**
     * We generate a diverse set of potential decision stumps for the given course by sampling each
     * feature's possible split points
     */
    private static DecisionStump[] generatePotentialStumpsForCourse(int courseId) {
        ArrayList<DecisionStump> stumps = new ArrayList<>();
        int[] studentIds = StudentInfoModel.getAllStudentIds();

        // we return an empty array if no students
        if (studentIds.length == 0) {
            return new DecisionStump[0];
        }

        // loop over all possible student features
        for (int featureId : StudentInfoModel.getAllFeatureIds()) {
            Feature sampleFeature = StudentInfoModel.getFeature(studentIds[0], featureId);

            if (sampleFeature instanceof CategoricalFeature) {
                // categorical feature handling by collection of all unique categories seen
                ArrayList<String> seenCategories = new ArrayList<>();
                for (int studentId : studentIds) {
                    CategoricalFeature studentFeature = (CategoricalFeature) StudentInfoModel.getFeature(studentId, featureId);
                    String category = studentFeature.getCategory();

                    // duplicate split avoidance by only using each category once
                    boolean alreadyListed = false;
                    for (String seen : seenCategories) {
                        if (seen.equals(category)) {
                            alreadyListed = true;
                            break;
                        }
                    }
                    if (alreadyListed) {
                        continue;
                    }
                    seenCategories.add(category);

                    // building of a decision stump for this category split
                    DecisionStump stump = buildDecisionStumpForSplit(
                            courseId,
                            new CategoricalFeature(featureId, category)
                    );
                    if (stump != null) {
                        stumps.add(stump);
                    }
                }
            } else if (sampleFeature instanceof NumericalFeature) {
                // numerical feature handling by collection of up to 20 unique values for splitting
                ArrayList<Double> uniqueValues = new ArrayList<>();

                for (int studentId : studentIds) {
                    NumericalFeature studentFeature = (NumericalFeature) StudentInfoModel.getFeature(studentId, featureId);
                    double value = studentFeature.getValue();

                    // we avoid duplicate split values
                    boolean alreadyListed = false;
                    for (double seen : uniqueValues) {
                        if (seen == value) {
                            alreadyListed = true;
                            break;
                        }
                    }
                    if (alreadyListed) {
                        continue;
                    }

                    uniqueValues.add(value);
                    // we build a stump for this value split
                    DecisionStump stump = buildDecisionStumpForSplit(
                            courseId,
                            new NumericalFeature(featureId, value)
                    );
                    if (stump != null) {
                        stumps.add(stump);
                    }

                    // only sample up to 20 unique splits per numerical feature to lessen computation load
                    if (uniqueValues.size() >= 20) {
                        break;
                    }
                }
            }
        }

        // return all found stumps as an array
        return stumps.toArray(new DecisionStump[0]);
    }

    /**
     * A test that shows the variance reduction forest builder on a course
     * It prints the selected stumps and compares the variance of averaged predictions before and after
     * the greedy selection
     */
    public static void testVarianceReductionForest(int courseId) {
        // print test header
        String courseName = CurrentGradesModel.getCourseName(courseId);
        System.out.println("Variance Reduction Forest Test");
        System.out.println("Course: " + courseName + " (ID " + courseId + ")");

        // make decision stumps for this course
        DecisionStump[] candidateStumps = generatePotentialStumpsForCourse(courseId);
        System.out.println("Candidate stumps generated: " + candidateStumps.length);
        if (candidateStumps.length == 0) {
            System.out.println("No candidates available for this course.");
            return;
        }

        // build the forest with greedy method
        DecisionStump[] forest = buildVarianceReductionForest(candidateStumps, courseId);
        System.out.println("Forest size: " + forest.length);
        if (forest.length == 0) {
            System.out.println("Forest builder returned no stumps.");
            return;
        }

        // get student ids with grades for this course
        ArrayList<Integer> studentsWithGrades = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        ArrayList<DecisionStump> candidateList = new ArrayList<>(Arrays.asList(candidateStumps));
        ArrayList<DecisionStump> forestList = new ArrayList<>(Arrays.asList(forest));

        // calculate and show variances
        double candidateVariance = calculateVarianceOfAveragedPredictions(candidateList, studentsWithGrades);
        double forestVariance = calculateVarianceOfAveragedPredictions(forestList, studentsWithGrades);

        System.out.printf("Variance using all candidates: %.4f%n", candidateVariance);
        System.out.printf("Variance using greedy forest: %.4f%n", forestVariance);

        // print the stumps that were picked
        System.out.println("Selected decision stumps:");
        for (DecisionStump stump : forest) {
            System.out.println("  - " + stump.asRule());
        }

        // show predictions for the first five students
        System.out.println("Sample averaged predictions (first five students with grades):");
        int sampleSize = Math.min(5, studentsWithGrades.size());
        for (int i = 0; i < sampleSize; i++) {
            int studentId = studentsWithGrades.get(i);
            double sum = 0.0;
            for (DecisionStump stump : forest) {
                sum += stump.predictGrade(studentId);
            }
            double averagedPrediction = sum / forest.length;
            double actualGrade = CurrentGradesModel.getGrade(studentId, courseId);
            System.out.printf("  Student %d -> predicted %.2f, actual %.2f%n", studentId, averagedPrediction, actualGrade);
        }
    }
}
