package regressionTree;

import datamodels.CurrentGradesModel;
import java.util.*;

/**
 * Optimizes regression tree hyperparameters and evaluates performance across all courses.
 */
public class TreeOptimizer {
    
    /**
     * Evaluates a tree configuration on a single course using cross-validation
     */
    public static double evaluateConfiguration(
            int courseId, 
            int maxDepth, 
            int minSamples,
            int folds
    ) {
        ArrayList<Integer> allStudents = 
            CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        
        if (allStudents.size() < folds) {
            return Double.MAX_VALUE; // Not enough data
        }
        
        Collections.shuffle(allStudents);
        int foldSize = allStudents.size() / folds;
        double totalMAE = 0;
        
        for (int fold = 0; fold < folds; fold++) {
            // Split into train and test
            int testStart = fold * foldSize;
            int testEnd = (fold == folds - 1) ? allStudents.size() : (fold + 1) * foldSize;
            
            List<Integer> testSet = new ArrayList<>(allStudents.subList(testStart, testEnd));
            
            List<Integer> trainSet = new ArrayList<>();
            for (int i = 0; i < allStudents.size(); i++) {
                if (i < testStart || i >= testEnd) {
                    trainSet.add(allStudents.get(i));
                }
            }
            
            // Train tree
            TreeNode tree = RegressionTreeTrainer.train(trainSet, courseId, maxDepth, minSamples);
            
            // Evaluate on test set
            double mae = calculateMAE(tree, testSet, courseId);
            totalMAE += mae;
        }
        
        return totalMAE / folds; // Average MAE across folds
    }
    
    /**
     * Finds optimal hyperparameters for a single course
     */
    public static Hyperparameters findBestHyperparameters(int courseId) {
        int[] maxDepths = {2, 3, 4, 5, 6};
        int[] minSamples = {5, 10, 15, 20, 25};
        int folds = 5; // 5-fold cross-validation
        
        double bestMAE = Double.MAX_VALUE;
        int bestMaxDepth = 3;
        int bestMinSamples = 10;
        
        System.out.println("Tuning hyperparameters for: " + CurrentGradesModel.getCourseName(courseId));
        
        for (int depth : maxDepths) {
            for (int samples : minSamples) {
                double mae = evaluateConfiguration(courseId, depth, samples, folds);
                System.out.printf("  Depth=%d, MinSamples=%d -> MAE=%.3f%n", depth, samples, mae);
                
                if (mae < bestMAE) {
                    bestMAE = mae;
                    bestMaxDepth = depth;
                    bestMinSamples = samples;
                }
            }
        }
        
        System.out.printf("Best: Depth=%d, MinSamples=%d, MAE=%.3f%n%n", 
            bestMaxDepth, bestMinSamples, bestMAE);
        
        return new Hyperparameters(bestMaxDepth, bestMinSamples, bestMAE);
    }
    
    /**
     * Finds hyperparameters that work well across ALL courses
     */
    public static Hyperparameters findGlobalBestHyperparameters() {
        int[] maxDepths = {2, 3, 4, 5};
        int[] minSamples = {5, 10, 15, 20};
        int folds = 5;
        
        double bestGlobalMAE = Double.MAX_VALUE;
        int bestMaxDepth = 3;
        int bestMinSamples = 10;
        
        System.out.println("=== Global Hyperparameter Tuning ===");
        System.out.println("Testing across all courses...\n");
        
        for (int depth : maxDepths) {
            for (int samples : minSamples) {
                double totalMAE = 0;
                int validCourses = 0;
                
                for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
                    ArrayList<Integer> students = 
                        CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
                    
                    if (students.size() < folds * 2) {
                        continue; // Skip courses with too few students
                    }
                    
                    double mae = evaluateConfiguration(courseId, depth, samples, folds);
                    totalMAE += mae;
                    validCourses++;
                }
                
                if (validCourses > 0) {
                    double avgMAE = totalMAE / validCourses;
                    System.out.printf("Depth=%d, MinSamples=%d -> Avg MAE=%.3f (across %d courses)%n", 
                        depth, samples, avgMAE, validCourses);
                    
                    if (avgMAE < bestGlobalMAE) {
                        bestGlobalMAE = avgMAE;
                        bestMaxDepth = depth;
                        bestMinSamples = samples;
                    }
                }
            }
        }
        
        System.out.printf("\n=== Global Best ===\n");
        System.out.printf("Depth=%d, MinSamples=%d, Avg MAE=%.3f%n", 
            bestMaxDepth, bestMinSamples, bestGlobalMAE);
        
        return new Hyperparameters(bestMaxDepth, bestMinSamples, bestGlobalMAE);
    }
    
    /**
     * Evaluates tree performance across all courses
     */
    public static void evaluateAllCourses(int maxDepth, int minSamples) {
        System.out.println("=== Evaluating All Courses ===");
        System.out.printf("Using: maxDepth=%d, minSamples=%d%n%n", maxDepth, minSamples);
        
        double totalMAE = 0;
        double totalMSE = 0;
        int validCourses = 0;
        
        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
            ArrayList<Integer> allStudents = 
                CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
            
            if (allStudents.size() < 20) {
                System.out.printf("Skipping %s (only %d students)%n", 
                    CurrentGradesModel.getCourseName(courseId), allStudents.size());
                continue;
            }
            
            Collections.shuffle(allStudents);
            int splitIndex = (int) (allStudents.size() * 0.8);
            List<Integer> trainSet = new ArrayList<>(allStudents.subList(0, splitIndex));
            List<Integer> testSet = new ArrayList<>(allStudents.subList(splitIndex, allStudents.size()));
            
            TreeNode tree = RegressionTreeTrainer.train(trainSet, courseId, maxDepth, minSamples);
            
            double mae = calculateMAE(tree, testSet, courseId);
            double mse = calculateMSE(tree, testSet, courseId);
            double r2 = calculateR2(tree, testSet, courseId);
            
            System.out.printf("%-40s MAE=%.3f  MSE=%.3f  R²=%.3f%n",
                CurrentGradesModel.getCourseName(courseId), mae, mse, r2);
            
            totalMAE += mae;
            totalMSE += mse;
            validCourses++;
        }
        
        System.out.printf("\n=== Overall Performance ===\n");
        System.out.printf("Average MAE: %.3f%n", totalMAE / validCourses);
        System.out.printf("Average MSE: %.3f%n", totalMSE / validCourses);
    }
    
    /**
     * Calculates Mean Absolute Error
     */
    public static double calculateMAE(TreeNode tree, List<Integer> testSet, int courseId) {
        double sumError = 0;
        for (int studentId : testSet) {
            double predicted = tree.predict(studentId);
            double actual = CurrentGradesModel.getGrade(studentId, courseId);
            sumError += Math.abs(predicted - actual);
        }
        return sumError / testSet.size();
    }
    
    /**
     * Calculates Mean Squared Error
     */
    public static double calculateMSE(TreeNode tree, List<Integer> testSet, int courseId) {
        double sumSquaredError = 0;
        for (int studentId : testSet) {
            double predicted = tree.predict(studentId);
            double actual = CurrentGradesModel.getGrade(studentId, courseId);
            double error = predicted - actual;
            sumSquaredError += error * error;
        }
        return sumSquaredError / testSet.size();
    }
    
    /**
     * Calculates R² (coefficient of determination)
     */
    public static double calculateR2(TreeNode tree, List<Integer> testSet, int courseId) {
        // Calculate mean of actual values
        double sumActual = 0;
        for (int studentId : testSet) {
            sumActual += CurrentGradesModel.getGrade(studentId, courseId);
        }
        double meanActual = sumActual / testSet.size();
        
        // Calculate SS_res and SS_tot
        double ssRes = 0;
        double ssTot = 0;
        for (int studentId : testSet) {
            double predicted = tree.predict(studentId);
            double actual = CurrentGradesModel.getGrade(studentId, courseId);
            ssRes += (actual - predicted) * (actual - predicted);
            ssTot += (actual - meanActual) * (actual - meanActual);
        }
        
        if (ssTot == 0) return 0;
        return 1 - (ssRes / ssTot);
    }
    
    /**
     * Stores hyperparameter configuration and its performance
     */
    public static class Hyperparameters {
        public final int maxDepth;
        public final int minSamples;
        public final double mae;
        
        public Hyperparameters(int maxDepth, int minSamples, double mae) {
            this.maxDepth = maxDepth;
            this.minSamples = minSamples;
            this.mae = mae;
        }
    }
}
