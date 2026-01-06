package solutions;

import datamodels.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Phase 1 Step 4: Variance Reduction Forest Methods
 * Contains methods for building decision stump forests using greedy variance reduction algorithm.
 */
public class Phase1Step4VarianceReduction {

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

        // get variance of these averages (using Phase1Step3's calculateVariance method)
        return Phase1Step3.calculateVariance(averagedPredictions);
    }

    /**
     * Helper that converts a split feature into a fully specified decision stump for the given course.
     * Returns null when the split does not produce meaningful predictions (no grades available).
     */
    private static DecisionStump buildDecisionStumpForSplit(int courseId, Feature splitFeature) {
        // Get mean grades for below and above the split (using Phase1Step3's method)
        double[] tabulatedMeans = Phase1Step3.meanGradesOfTabulation(courseId, splitFeature);

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
