package regressionTree;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.Collections;

public class regressionForest {

    public static double createRegressionForest(int treeNumber, int courseId, int studentId){

        ArrayList<Integer> allStudents = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
        double sum = 0;

        //for the given number of regression trees, it creates a new tree
        // with randomly selected 70 percent of the students, and it returns the mean of them at the end

        for (int i = 1; i <= treeNumber; i++) {
            ArrayList<Integer> trainingStudents = getRandom(allStudents);

            if (trainingStudents.isEmpty()) {
                System.out.println("No training data available.");
                return -1;
            }

            TreeNode regressionTree = RegressionTreeTrainer.train(trainingStudents, courseId);
            double predictedGrade = regressionTree.predict(studentId);
            //System.out.println("Predicted grade for regression tree " + i + ": " + predictedGrade);
            sum = sum + predictedGrade;
        }

        double forestPrediction = Math.round(sum / treeNumber);

        return forestPrediction;

    }

    //helper method to get randomly selected students (70 percent of the whole group each time)
    public static ArrayList<Integer> getRandom(ArrayList<Integer> students) {

        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Integer> shuffled = new ArrayList<>(students);
        Collections.shuffle(shuffled);

        int sampleSize = (int) Math.ceil(shuffled.size() * 0.7);

        return new ArrayList<>(shuffled.subList(0, sampleSize));
    }

}
