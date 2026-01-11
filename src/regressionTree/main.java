package regressionTree;

import datamodels.CurrentGradesModel;
import java.util.ArrayList;

public class main {

    public static void main(String[] args) {

        int studentId = 313173;   // student to predict
        int courseId = 1;        // course to predict
        double actualgrade = CurrentGradesModel.getGrade(studentId, courseId); //actual grade if exists so we can compare

        // training data: students who already have grades
        ArrayList<Integer> trainingStudents =
                CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

        if (trainingStudents.isEmpty()) {
            System.out.println("No training data available.");
            return;
        }

        // train regression tree for the given course with all students
        TreeNode regressionTree =
                RegressionTreeTrainer.train(trainingStudents, courseId);

        // predict the grade of a specific student
        double forestPredictedGrade = regressionForest.createRegressionForest(100, courseId, studentId);


        System.out.println("Regression Tree Prediction");
        System.out.println("Student ID: " + studentId);
        System.out.println("Course: " + CurrentGradesModel.getCourseName(courseId));
        System.out.println("Regression forest predicted grade:" + forestPredictedGrade);
        System.out.println("Actual grade:" + actualgrade);
    }
}