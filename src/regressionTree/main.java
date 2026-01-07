package regressionTree;

import datamodels.CurrentGradesModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class main {

    public static void main(String[] args) {
        int courseId = 1;  // course to predict
        
        // Get all students with grades for this course
        ArrayList<Integer> allStudents = 
                CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

        if (allStudents.isEmpty()) {
            System.out.println("No training data available.");
            return;
        }

        // Shuffle data for randomization
        Collections.shuffle(allStudents);

        // Split into train (80%) and test (20%)
        int splitIndex = (int) (allStudents.size() * 0.8);
        List<Integer> trainStudents = allStudents.subList(0, splitIndex);
        List<Integer> testStudents = allStudents.subList(splitIndex, allStudents.size());

        System.out.println("~~~ Regression Tree Training ~~~");
        System.out.println("Course: " + CurrentGradesModel.getCourseName(courseId));
        System.out.println("Total students: " + allStudents.size());
        System.out.println("Training set: " + trainStudents.size());
        System.out.println("Test set: " + testStudents.size());
        System.out.println();

        // Configure hyperparameters
        int maxDepth = 3;
        int minSamples = 10;

        // Train regression tree
        TreeNode regressionTree = RegressionTreeTrainer.train(trainStudents, courseId,  maxDepth, minSamples);

        // Print tree structure
        System.out.println("~~~ Tree Structure ~~~");
        regressionTree.printTree("");
        System.out.println();

        // Evaluate on test set
        System.out.println("~~~ Test Set Evaluation ~~~");
        double sumError = 0;
        int testCount = 0;
        
        for (int studentId : testStudents) {
            double predicted = regressionTree.predict(studentId);
            double actual = CurrentGradesModel.getGrade(studentId, courseId);
            double error = Math.abs(predicted - actual);
            sumError += error;
            testCount++;
        }

        double meanAbsoluteError = sumError / testCount;
        System.out.println("Mean Absolute Error: " + String.format("%.2f", meanAbsoluteError));
        System.out.println();

        // Sample prediction
        if (!testStudents.isEmpty()) {
            int sampleStudentId = testStudents.get(0);
            double predictedGrade = regressionTree.predict(sampleStudentId);
            double actualGrade = CurrentGradesModel.getGrade(sampleStudentId, courseId);
            
            System.out.println("=== Sample Prediction ===");
            System.out.println("Student ID: " + sampleStudentId);
            System.out.println("Predicted grade: " + String.format("%.2f", predictedGrade));
            System.out.println("Actual grade: " + String.format("%.2f", actualGrade));
        }
    }
}
