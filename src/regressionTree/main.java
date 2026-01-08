package regressionTree;

import datamodels.CurrentGradesModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class main {

    public static void main(String[] args) {
        // Step 1: Find optimal hyperparameters globally
        TreeOptimizer.Hyperparameters best = TreeOptimizer.findGlobalBestHyperparameters();
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Step 2: Evaluate performance across all courses with optimal hyperparameters
        TreeOptimizer.evaluateAllCourses(best.maxDepth, best.minSamples);
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Step 3: Optional - Show detailed results for a specific course
        int exampleCourseId = 1;
        System.out.println("=== Detailed Example: " + CurrentGradesModel.getCourseName(exampleCourseId) + " ===");
        
        ArrayList<Integer> allStudents = 
            CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(exampleCourseId);
        Collections.shuffle(allStudents);
        
        int splitIndex = (int) (allStudents.size() * 0.8);
        List<Integer> trainSet = new ArrayList<>(allStudents.subList(0, splitIndex));
        List<Integer> testSet = new ArrayList<>(allStudents.subList(splitIndex, allStudents.size()));
        
        TreeNode tree = RegressionTreeTrainer.train(trainSet, exampleCourseId, best.maxDepth, best.minSamples);
        
        System.out.println("\nTree Structure:");
        tree.printTree("");
        
        System.out.println("\nTest Set Performance:");
        double mae = TreeOptimizer.calculateMAE(tree, testSet, exampleCourseId);
        double mse = TreeOptimizer.calculateMSE(tree, testSet, exampleCourseId);
        double r2 = TreeOptimizer.calculateR2(tree, testSet, exampleCourseId);
        
        System.out.printf("MAE: %.3f%n", mae);
        System.out.printf("MSE: %.3f%n", mse);
        System.out.printf("R²:  %.3f%n", r2);
    }
}
