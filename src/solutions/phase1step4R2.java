package solutions;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;


import datamodels.CurrentGradesModel;
import datamodels.DecisionStump;
import datamodels.Feature;
import datamodels.GraduateGradesModel;
import datamodels.NumericalFeature;
import datamodels.SplitCondition;
import datamodels.StudentInfoModel;


public class phase1step4R2 { // R2 is a comparison between the mean grade and the predicted grade by the model, the closer to 1 the better the model is see 
    //also known as coefficient of determination or R squared
    public static void main(String[] args) {
        int courseCount = CurrentGradesModel.courseCount;

        DecisionStump[] allStumps = new DecisionStump[courseCount];
        System.out.println("Building best decision stump for each course...");

        for (int c = 0; c < courseCount; c++) {
            allStumps[c] = findBestDescisionStumpForCourse(c);
        }

        final int FOREST_COUNT = 10000; //not generating all forest for computation time
        final int FOREST_SIZE = 10;
        Random rng = new Random(42); //setting seed for reproducibility (did you catch the reference to 42 ? :) )

        final int TOP_FOREST = 1000; //keeping the top 1000 ( idk if it is my greatest idea tbh)


        ArrayList<int[]>[] TopForestsByCourse = new ArrayList[courseCount];
        ArrayList<Double>[] TopR2sByCourse = new ArrayList[courseCount];

        for (int c = 0; c < courseCount; c++) {
            TopForestsByCourse[c] = new ArrayList<int[]>(TOP_FOREST);
            TopR2sByCourse[c] = new ArrayList<Double>(TOP_FOREST);
        }

        for (int courseId = 0; courseId < courseCount; courseId++) {
            System.out.println("Evaluating forests for course " + courseId + " (" + GraduateGradesModel.getCourseName(courseId) + ")...");


            int[] allStudents = GraduateGradesModel.getAllStudentIds(); // Get all students from graduate data (from another file)


            ArrayList<Integer> studentsWithGrade = new ArrayList<Integer>();


            for (int studentId : allStudents) {
                double grade = GraduateGradesModel.getGrade(studentId, courseId);
                if (!Double.isNaN(grade)) {
                    studentsWithGrade.add(studentId);
                }
            }


            int sampleSize = Math.min(2000, studentsWithGrade.size()); // choose 2000 to have a mix of accuracy and computation time

            Collections.shuffle(studentsWithGrade, rng);

            List<Integer> sampleofStudents = studentsWithGrade.subList(0, sampleSize);


            PriorityQueue<ForestsR2> topForests = new PriorityQueue<ForestsR2>(TOP_FOREST, new Comparator<ForestsR2>() {
                public int compare(ForestsR2 a, ForestsR2 b) {
                    return Double.compare(a.r2, b.r2);
                }
            });


            for (int f = 0; f < FOREST_COUNT; f++) { //creates the forests

                int[] forest = new int[FOREST_SIZE];

                for (int i = 0; i < FOREST_SIZE; i++) { //pick random stumps to create the forest
                    forest[i] = rng.nextInt(courseCount);
                }


                double[] forestsPred = new double[sampleSize];
                double[] truegrade = new double[sampleSize];

                for (int i = 0; i < sampleSize; i++) {
                    int studentId = sampleofStudents.get(i); // Get student id

                    double sumPred = 0.0;

                    for (int idx : forest) {
                        DecisionStump stump = allStumps[idx]; // Get stump
                        sumPred += stump.predictGrade(studentId); // sum of the predictions of the stumps
                    }
                    forestsPred[i] = sumPred / (double) FOREST_SIZE; // Average prediction of the forest
                    truegrade[i] = GraduateGradesModel.getGrade(studentId, courseId);
                }

                double r2 = calculateR2(truegrade, forestsPred);

                if (topForests.size() < TOP_FOREST) {
                    topForests.add(new ForestsR2(forest, r2)); // Add new forest
                } else if (r2 > topForests.peek().r2) {
                    topForests.poll(); // Remove worst
                    topForests.add(new ForestsR2(forest, r2)); //and add the new one
                }
            }

            while (!topForests.isEmpty()) {
                ForestsR2 fs = topForests.poll();
                TopForestsByCourse[courseId].add(fs.forest);
                TopR2sByCourse[courseId].add(fs.r2); //storse the forest and it's r2 on the heap
            }

            Collections.reverse(TopForestsByCourse[courseId]);
            Collections.reverse(TopR2sByCourse[courseId]);

            System.out.println("  Stored best " + TopForestsByCourse[courseId].size() + " forests for " + GraduateGradesModel.getCourseName(courseId));
            if (!TopR2sByCourse[courseId].isEmpty()) {
                System.out.println("  Top forest R2 for " + GraduateGradesModel.getCourseName(courseId) + ": " + TopR2sByCourse[courseId].get(0));
                // Print top 10 forests with their R^2 and rules
                int toShow = Math.min(10, TopForestsByCourse[courseId].size());
                System.out.println("  Top " + toShow + " forests for " + GraduateGradesModel.getCourseName(courseId) + ":");
                for (int j = 0; j < toShow; j++) {
                    double r2val = TopR2sByCourse[courseId].get(j);
                    System.out.println("    #" + (j + 1) + " R2=" + String.format("%.3f", r2val));
                    int[] forest = TopForestsByCourse[courseId].get(j);
                    // Print each stump rule in the forest
                    for (int idx : forest) {
                        DecisionStump ds = allStumps[idx];
                        System.out.println("      - " + ds.asRule());
                    }
                }
            }


        }
    }

    // Helper class to store a forest and its R2 score.
    private static class ForestsR2 {

        int[] forest;

        double r2;

        ForestsR2(int[] forest, double r2) {
            this.forest = forest;
            this.r2 = r2;
        }
    }


    public static double calculateR2(double[] truegrade, double[] forestsPred) {

        int n = truegrade.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) sum += truegrade[i];
        double mean = sum / n; //mean of true grades
        double ssTot = 0.0;
        double ssRes = 0.0;
        for (int i = 0; i < n; i++) {
            ssTot += Math.pow(truegrade[i] - mean, 2); //total sum of squares
            ssRes += Math.pow(truegrade[i] - forestsPred[i], 2); //residual sum of squares
        }
        return 1.0 - (ssRes / ssTot); // R^2 formula
    }


    private static DecisionStump findBestDescisionStumpForCourse(int courseId) { //same name as in step3 for coherance purpose
        Feature bestSplit = findBestPropertyToGuessGrade(courseId); // Find best feature/threshold

        double[] tabulatedMeans = meanGradesOfTabulation(courseId, bestSplit);

        // Handle cases where one or both groups are empty
        if (tabulatedMeans[0] == -1) {
            tabulatedMeans[0] = tabulatedMeans[1];
        } else if (tabulatedMeans[1] == -1) {
            tabulatedMeans[1] = tabulatedMeans[0];
        }
        if (tabulatedMeans[0] == -1 && tabulatedMeans[1] == -1) {
            tabulatedMeans[0] = tabulatedMeans[1] = CurrentGradesModel.calcCourseMean(courseId);
        }


        return new DecisionStump(bestSplit, tabulatedMeans[1], tabulatedMeans[0]);
    }


    // Computes the mean grades for students above and below the split.
    private static double[] meanGradesOfTabulation(int courseId, Feature splitFeature) {
        ArrayList<Double>[] tabulation = tabulateCourseByStudentFeature(courseId, splitFeature);
        double[] means = new double[2];
        for (int i = 0; i < 2; i++) {
            ArrayList<Double> subGroup = tabulation[i];
            double sum = 0;
            for (int k = 0; k < subGroup.size(); k++) sum += subGroup.get(k);
            means[i] = subGroup.isEmpty() ? -1 : sum / subGroup.size();
        }
        return means;
    }

    // Splits students into two groups (above/below split) and collects their grades.
    private static ArrayList<Double>[] tabulateCourseByStudentFeature(int courseId, Feature splitFeature) {

        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

        ArrayList<Double> aboveSplit = new ArrayList<Double>(); // Students above split

        ArrayList<Double> belowSplit = new ArrayList<Double>(); // Students below split

        for (int ii = 0; ii < studentIds.size(); ii++) {

            int studentId = studentIds.get(ii);

            double grade = CurrentGradesModel.getGrade(studentId, courseId);

            Feature studentFeature = StudentInfoModel.getFeature(studentId, splitFeature.getFeatureId());
            if (SplitCondition.evaluate(studentFeature, splitFeature)) {
                aboveSplit.add(grade);
            } else {
                belowSplit.add(grade);
            }
        }

        ArrayList<Double>[] results = (ArrayList<Double>[]) new ArrayList[2];
        results[0] = belowSplit;
        results[1] = aboveSplit;
        return results;
    }


    private static Feature findBestPropertyToGuessGrade(int courseId) {

        int[] featureIds = StudentInfoModel.getAllFeatureIds();


        if (featureIds.length > 0) {

            int featureId = featureIds[0];

            if (NumericalFeature.isIdAllowed(featureId)) {

                double rangeMax = NumericalFeature.getRangeMax(featureId);

                double rangeMin = NumericalFeature.getRangeMin(featureId);
                double midpoint = (rangeMin + rangeMax) / 2.0;
                return new NumericalFeature(featureId, midpoint);
            }
        }


        return new NumericalFeature(0, 0.0);
    }

}


