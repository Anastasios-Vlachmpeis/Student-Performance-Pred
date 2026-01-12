package regressionTree;

import datamodels.CurrentGradesModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for evaluating regression trees using R^2, MSE, and MAE metrics
 */
public class ModelEvaluator {

    /**
     * Evaluation results container
     */
    public static class EvaluationMetrics {
        public final double mse;
        public final double mae;
        public final double r2;

        public EvaluationMetrics(double mse, double mae, double r2) {
            this.mse = mse;
            this.mae = mae;
            this.r2 = r2;
        }

        @Override
        public String toString() {
            return String.format("MSE: %.4f, MAE: %.4f, R^2: %.4f", mse, mae, r2);
        }
    }

    //Main method to test the evaluator for all courses
    public static void main(String[] args) {
        final int NUM_TREES = 100; // Number of trees in the forest

        // Single tree metrics
        double totalMSE = 0.0;
        double totalMAE = 0.0;
        double totalR2 = 0.0;

        // Forest metrics
        double totalForestMSE = 0.0;
        double totalForestMAE = 0.0;
        double totalForestR2 = 0.0;

        int courseCount = 0;

        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
            ArrayList<Integer> studentsWithGrades = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

            // Skip courses if they have no students or too few students
            if (studentsWithGrades.isEmpty() || studentsWithGrades.size() < 10) {
                System.out.println(CurrentGradesModel.getCourseName(courseId) + ": Skipped (insufficient data)");
                continue;
            }
            int totalSize = studentsWithGrades.size();
            int trainSize = (int) (totalSize * 0.7);

            // Split into training and evaluation sets
            List<Integer> trainingStudents = new ArrayList<>(studentsWithGrades.subList(0, trainSize));
            List<Integer> evaluationStudents = new ArrayList<>(studentsWithGrades.subList(trainSize, totalSize));

            // Evaluate single tree
            TreeNode tree = RegressionTreeTrainer.train(trainingStudents, courseId);
            EvaluationMetrics treeMetrics = evaluateTree(tree, courseId, evaluationStudents);
            // Evaluate forest
            EvaluationMetrics forestMetrics = evaluateForest(trainingStudents, courseId, evaluationStudents, NUM_TREES);

            System.out.println(CurrentGradesModel.getCourseName(courseId) + ": Tree - " + treeMetrics + ", Forest - " + forestMetrics);

            // Accumulate metrics for averaging
            totalMSE += treeMetrics.mse;
            totalMAE += treeMetrics.mae;
            totalR2 += treeMetrics.r2;

            totalForestMSE += forestMetrics.mse;
            totalForestMAE += forestMetrics.mae;
            totalForestR2 += forestMetrics.r2;

            courseCount++;
        }

        // Print averages across all evaluated courses
        System.out.println("\nSingle Tree - Average across " + courseCount + " courses:");
        System.out.println(String.format("MSE: %.4f, MAE: %.4f, R^2: %.4f", totalMSE / courseCount, totalMAE / courseCount, totalR2 / courseCount));

        System.out.println("\nRandom Forest (" + NUM_TREES + " trees) - Average across " + courseCount + " courses:");
        System.out.println(String.format("MSE: %.4f, MAE: %.4f, R^2: %.4f", totalForestMSE / courseCount, totalForestMAE / courseCount, totalForestR2 / courseCount));
    }

    /**
     * Evaluates a random forest on a set of students for a specific course
     * Returns MSE, MAE, and R^2 metrics
     * Trains the forest once and reuses it for all evaluation students
     * @param trainingStudents List of student IDs to train the forest on
     * @param courseId The course ID to evaluate
     * @param evaluationStudentIds List of student IDs to evaluate on
     * @param numTrees Number of trees in the forest
     * @return EvaluationMetrics containing MSE, MAE, and R^2
     */
    public static EvaluationMetrics evaluateForest(
            List<Integer> trainingStudents,
            int courseId,
            List<Integer> evaluationStudentIds,
            int numTrees) {

        // Convert training students to ArrayList for forest method
        ArrayList<Integer> trainingStudentsList = new ArrayList<>(trainingStudents);

        // Train the forest once
        ArrayList<TreeNode> forest = trainForest(trainingStudentsList, courseId, numTrees);

        int n = evaluationStudentIds.size();
        double[] actualGrades = new double[n];
        double[] predictedGrades = new double[n];

        // Collect actual and predicted grades using the pre-trained forest
        for (int i = 0; i < n; i++) {
            int studentId = evaluationStudentIds.get(i);
            double actual = CurrentGradesModel.getGrade(studentId, courseId);
            double predicted = predictWithForest(forest, studentId);

            actualGrades[i] = actual;
            predictedGrades[i] = predicted;
        }

        // Calculate metrics
        double mse = calculateMSE(actualGrades, predictedGrades);
        double mae = calculateMAE(actualGrades, predictedGrades);
        double r2 = calculateR2(actualGrades, predictedGrades);

        return new EvaluationMetrics(mse, mae, r2);
    }

    /**
     * Trains a random forest (all trees) on the training data
     * @param trainingStudents List of student IDs to train on
     * @param courseId The course ID
     * @param numTrees Number of trees to train
     * @return List of trained tree nodes
     */
    private static ArrayList<TreeNode> trainForest(
            ArrayList<Integer> trainingStudents,
            int courseId,
            int numTrees) {

        ArrayList<TreeNode> forest = new ArrayList<>();

        for (int i = 0; i < numTrees; i++) {
            ArrayList<Integer> bootstrapSample = regressionForest.getRandom(trainingStudents);

            if (bootstrapSample.isEmpty()) {
                continue;
            }

            TreeNode tree = RegressionTreeTrainer.train(bootstrapSample, courseId);
            forest.add(tree);
        }

        return forest;
    }

    /**
     * Predicts a grade using a pre-trained forest (averages predictions from all trees)
     * @param forest List of trained tree nodes
     * @param studentId The student ID to predict for
     * @return Predicted grade (average of all trees, not rounded)
     */
    private static double predictWithForest(ArrayList<TreeNode> forest, int studentId) {
        if (forest.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (TreeNode tree : forest) {
            sum += tree.predict(studentId);
        }

        return sum / forest.size();
    }

    /**
     * Evaluates a regression tree on a set of students for a specific course
     * Returns MSE, MAE, and R^2 metrics
     * @param tree The trained regression tree
     * @param courseId The course ID to evaluate
     * @param evaluationStudentIds List of student IDs to evaluate on
     * @return EvaluationMetrics containing MSE, MAE, and R^2
     */
    public static EvaluationMetrics evaluateTree(TreeNode tree, int courseId, List<Integer> evaluationStudentIds) {

        int n = evaluationStudentIds.size();
        double[] actualGrades = new double[n];
        double[] predictedGrades = new double[n];

        // Collect actual and predicted grades
        for (int i = 0; i < n; i++) {
            int studentId = evaluationStudentIds.get(i);
            double actual = CurrentGradesModel.getGrade(studentId, courseId);
            double predicted = tree.predict(studentId);

            actualGrades[i] = actual;
            predictedGrades[i] = predicted;
        }

        // Calculate metrics
        double mse = calculateMSE(actualGrades, predictedGrades);
        double mae = calculateMAE(actualGrades, predictedGrades);
        double r2 = calculateR2(actualGrades, predictedGrades);

        return new EvaluationMetrics(mse, mae, r2);
    }

    /**
     * Mean Squared Error
     * MSE = (1/n) * sum(actual_i - predicted_i)^2
     * @param actual Array of actual values
     * @param predicted Array of predicted values
     * @return MSE value
     */
    public static double calculateMSE(double[] actual, double[] predicted) {
        if (actual.length != predicted.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        int n = actual.length;
        if (n == 0) {
            return 0.0;
        }

        double sumSquared = 0.0;
        for (int i = 0; i < n; i++) {
            double diff = actual[i] - predicted[i];
            sumSquared += diff * diff;
        }

        return sumSquared / n;
    }

    /**
     * Mean Absolute Error
     * MAE = (1/n) * sum(|actual_i - predicted_i|)
     * @param actual Array of actual values
     * @param predicted Array of predicted values
     * @return MAE value
     */
    public static double calculateMAE(double[] actual, double[] predicted) {
        if (actual.length != predicted.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        int n = actual.length;
        if (n == 0) {
            return 0.0;
        }

        double sumAbsolute = 0.0;
        for (int i = 0; i < n; i++) {
            sumAbsolute += Math.abs(actual[i] - predicted[i]);
        }

        return sumAbsolute / n;
    }

    /**
     * R-squared
     * R^2 = 1 - (SS_res / SS_tot)
     * where SS_res = sum(actual_i - predicted_i)^2
     *       SS_tot = sum(actual_i - mean_actual)^2
     * @param actual Array of actual values
     * @param predicted Array of predicted values
     * @return R^2 value (can be negative if model performs worse than mean)
     */
    public static double calculateR2(double[] actual, double[] predicted) {
        if (actual.length != predicted.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        int n = actual.length;
        if (n == 0) {
            return 0.0;
        }

        // Calculate mean of actual values
        double mean = 0.0;
        for (int i = 0; i < n; i++) {
            mean += actual[i];
        }
        mean /= n;

        // Calculate Sum of Squares Total and Residual
        double ssTot = 0.0;
        double ssRes = 0.0;

        for (int i = 0; i < n; i++) {
            ssTot += (actual[i] - mean) * (actual[i] - mean);
            double diff = actual[i] - predicted[i];
            ssRes += diff * diff;
        }

        // Handle edge case: no variance in actual values
        if (ssTot == 0.0) {
            return 0.0;
        }

        return 1.0 - (ssRes / ssTot);
    }
}