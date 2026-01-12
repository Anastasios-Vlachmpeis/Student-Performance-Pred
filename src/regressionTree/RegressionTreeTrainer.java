package regressionTree;

import datamodels.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


 //Trains a regression tree using recursive variance reduction.  Each internal node is a decision stump, and leaves store the average grade of the subset.

public class RegressionTreeTrainer {

    // default parameters
    // maximum depth of the tree to prevent overfitting, ive tried 4 and 5 but 3 gave the best value
    private static final int DEFAULT_MAX_DEPTH = 3;
    // minimum number of samples required to split further, ive chosen this randomly idk if there is any other value that gives better result
    private static final int DEFAULT_MIN_SAMPLES = 10;

    // Default train method
    public static TreeNode train(List<Integer> studentIds, int courseId) {
        return trainRecursive(studentIds, courseId, 0, DEFAULT_MAX_DEPTH, DEFAULT_MIN_SAMPLES);
    }

    // train method with custom parameters
    public static TreeNode train(List<Integer> studentIds, int courseId, int maxDepth, int minSamples) {
        return trainRecursive(studentIds, courseId, 0, maxDepth, minSamples);
    }

     //recursively builds the regression tree. at each step, finds the best decision stump and splits the data accordingly
    private static TreeNode trainRecursive(
            List<Integer> studentIds,
            int courseId,
            int depth,
            int maxDepth,
            int minSamples
    ) {
        // stop if maximum depth reached or too few samples left
        if (depth >= maxDepth || studentIds.size() < minSamples) {
            return TreeNode.leaf(meanGrade(studentIds, courseId));
        }

        // find the best decision stump for this subset
        DecisionStump bestStump =
                findBestDecisionStump(studentIds, courseId);

        // if no useful split exists, return a leaf
        if (bestStump == null) {
            return TreeNode.leaf(meanGrade(studentIds, courseId));
        }

        // split students into left and right groups
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        for (int studentId : studentIds) {
            Feature f = StudentInfoModel.getFeature(
                    studentId,
                    bestStump.getSplittingFeature().getFeatureId()
            );

            if (SplitCondition.evaluate(f, bestStump.getSplittingFeature())) {
                right.add(studentId);
            } else {
                left.add(studentId);
            }
        }

        // if split does not actually divide data(if one side is 0), stop splitting
        if (left.isEmpty() || right.isEmpty()) {
            return TreeNode.leaf(meanGrade(studentIds, courseId));
        }

        // create an internal node and recurse on both branches
        TreeNode node = TreeNode.internal(bestStump);
        node.left = trainRecursive(left, courseId, depth + 1, maxDepth, minSamples);
        node.right = trainRecursive(right, courseId, depth + 1, maxDepth, minSamples);

        return node;
    }

     //Finds the best decision stump for a subset of students by maximizing variance reduction.
    private static DecisionStump findBestDecisionStump(
            List<Integer> studentIds,
            int courseId
    ) {
        double parentVariance = varianceOfStudents(studentIds, courseId);

        DecisionStump bestStump = null;
        double bestReduction = 0;

        // try randomly selected 3 features to increase randomness
        int[] featureArray = StudentInfoModel.getAllFeatureIds();
        List<Integer> allFeatures = new ArrayList<>();
        for (int f : featureArray) {
            allFeatures.add(f);
        }
        Collections.shuffle(allFeatures);
        List<Integer> randomFeatures = allFeatures.subList(0, 3);

        for (int featureId : randomFeatures) {

            Feature sample = StudentInfoModel.getFeature(
                    studentIds.get(0),
                    featureId
            );

            // if its numerical
            if (sample instanceof NumericalFeature) {
                for (int studentId : studentIds) {
                    double value = ((NumericalFeature)
                            StudentInfoModel.getFeature(studentId, featureId))
                            .getValue();

                    DecisionStump stump =
                            buildStump(courseId, featureId, value, true);

                    double reduction =
                            varianceReduction(studentIds, courseId, stump, parentVariance);

                    if (reduction > bestReduction) {
                        bestReduction = reduction;
                        bestStump = stump;
                    }
                }
            }

            // if its categorical
            if (sample instanceof CategoricalFeature) {
                for (String category :
                        CategoricalFeature.getRange(featureId)) {

                    DecisionStump stump =
                            buildStump(courseId, featureId, category, false);

                    double reduction =
                            varianceReduction(studentIds, courseId, stump, parentVariance);

                    if (reduction > bestReduction) {
                        bestReduction = reduction;
                        bestStump = stump;
                    }
                }
            }
        }

        return bestStump;
    }


     //builds a decision stump for a given split value and computes mean grades for both branches.
    private static DecisionStump buildStump(
            int courseId,
            int featureId,
            Object value,
            boolean numerical
    ) {
        Feature splitFeature = numerical
                ? new NumericalFeature(featureId, (double) value)
                : new CategoricalFeature(featureId, (String) value);

        double[] means = meanGradesForSplit(courseId, splitFeature);

        // if no valid grades available
        if (means[0] == -1 && means[1] == -1) {
            return null;
        }

        // fill missing branch means if needed
        if (means[0] == -1) means[0] = means[1];
        if (means[1] == -1) means[1] = means[0];

        return new DecisionStump(splitFeature, means[1], means[0]);
    }


     //computes variance reduction produced by a stump to find the best split
    private static double varianceReduction(
            List<Integer> studentIds,
            int courseId,
            DecisionStump stump,
            double parentVariance
    ) {
        if (stump == null) return 0;

        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        for (int id : studentIds) {
            Feature f = StudentInfoModel.getFeature(
                    id,
                    stump.getSplittingFeature().getFeatureId()
            );
            if (SplitCondition.evaluate(f, stump.getSplittingFeature())) {
                right.add(id);
            } else {
                left.add(id);
            }
        }

        if (left.isEmpty() || right.isEmpty()) return 0;

        double leftVar = varianceOfStudents(left, courseId);
        double rightVar = varianceOfStudents(right, courseId);

        int total = studentIds.size();
        double weighted =
                (leftVar * left.size() + rightVar * right.size()) / total;

        return parentVariance - weighted;
    }


     //computes mean grade for a group of students for a specific course
    private static double meanGrade(List<Integer> students, int courseId) {
        double sum = 0;
        int count = 0;

        for (int s : students) {
            double g = CurrentGradesModel.getGrade(s, courseId);
            if (g >= 0) {
                sum += g;
                count++;
            }
        }
        return count == 0 ? -1 : sum / count;
    }


     //computes variance of grades for a group of students, it is done to find best split
    private static double varianceOfStudents(List<Integer> students, int courseId) {
        ArrayList<Double> grades = new ArrayList<>();

        for (int s : students) {
            double g = CurrentGradesModel.getGrade(s, courseId);
            if (g >= 0) grades.add(g);
        }

        if (grades.isEmpty()) return 0;

        double mean = 0;
        for (double g : grades) mean += g;
        mean /= grades.size();

        double sum = 0;
        for (double g : grades) {
            sum += (g - mean) * (g - mean);
        }

        return sum / grades.size();
    }


     // computes mean grades for both sides of a split.

    private static double[] meanGradesForSplit(
            int courseId,
            Feature splitFeature
    ) {
        ArrayList<Double> below = new ArrayList<>();
        ArrayList<Double> above = new ArrayList<>();

        for (int studentId :
                CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId)) {

            double grade = CurrentGradesModel.getGrade(studentId, courseId);
            Feature f = StudentInfoModel.getFeature(
                    studentId,
                    splitFeature.getFeatureId()
            );

            if (SplitCondition.evaluate(f, splitFeature)) {
                above.add(grade);
            } else {
                below.add(grade);
            }
        }

        return new double[]{
                mean(below),
                mean(above)
        };
    }

    //computes the mean of a student list
    private static double mean(List<Double> list) {
        if (list.isEmpty()) return -1;
        double s = 0;
        for (double v : list) s += v;
        return s / list.size();
    }
}