package solutions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import datamodels.CategoricalFeature;
import datamodels.CurrentGradesModel; 
import datamodels.DecisionStump; 
import datamodels.Feature;
import datamodels.NumericalFeature;
import datamodels.SplitCondition;
import datamodels.StudentInfoModel;


public class phase1step4R2 {

    public static void main(String[] args) {

        System.out.println("\nPhase 1 Step 4 — Evaluating the best forest with R2");

        //generating all decision stumps
        DecisionStump[] allStumps = generateAllDecisionStumps();
        if (allStumps == null || allStumps.length == 0) {
            System.out.println("ERROR: No decision stumps generated.");
            return;
        }

        //Evaluate forests per course
        evaluateForestsForCourse(allStumps);

        System.out.println("Finished Successfully");
    }
        
    //Creates an arrays containing all the decision stumps
public static DecisionStump[] generateAllDecisionStumps() {
    System.out.println("Generating all decision stumps");

    List<DecisionStump> stumpList = new ArrayList<>();
    int studentCount = StudentInfoModel.getAllStudentIds().length;
    int[] allFeatures = StudentInfoModel.getAllFeatureIds();

    final int NUMERIC_THRESHOLDS = 10;
    final int MAX_CATEGORIES = 10;

    // For each feature
    for (int featureId : allFeatures) {

        // checks if the feature is numerical or categorical
        Feature featureType = StudentInfoModel.getFeature(
                StudentInfoModel.getAllStudentIds()[0], featureId
        );

        if (featureType instanceof NumericalFeature) { // For numerical features
            List<Double> values = new ArrayList<>();

            for (int s = 0; s < studentCount; s++) {
                int studentGlobalId = StudentInfoModel.getAllStudentIds()[s];
                Feature f = StudentInfoModel.getFeature(studentGlobalId, featureId);
                if (f instanceof NumericalFeature) {
                    double v = ((NumericalFeature) f).getValue(); //getting all the values for the feature
                    values.add(v);
                }
            }

            if (values.isEmpty()) continue;
            //sort the values
            Collections.sort(values); 
            //remove duplicates (bc we'll use them as thresholds)
            List<Double> uniqueValues = new ArrayList<>();
            double prev = Double.NaN;
            for (double v : values) {
                if (Double.isNaN(prev) || v != prev) {
                    uniqueValues.add(v);
                    prev = v; 
                }
            }

            int total = uniqueValues.size();
            for (int i = 0; i < NUMERIC_THRESHOLDS; i++) {
                int index = (int) ((i / (double) (NUMERIC_THRESHOLDS - 1)) * (total - 1)); //create evenly spaced thresholds
                double threshold = uniqueValues.get(index);

                NumericalFeature splitFeat = new NumericalFeature(featureId, threshold); //create the numerical feature
                double[] means = meanGradesOfTabulation(splitFeat); 
                double below = means[0]; //mean grade for students below the threshold
                double above = means[1]; //mean grade for students above the threshold

                if (above < 0) above = below;
                if (below < 0) below = above;

                stumpList.add(new DecisionStump(splitFeat, above, below)); //add the new stump to the list
            }

        } else if (featureType instanceof CategoricalFeature) { // For categorical features
            Set<String> categories = new HashSet<>();

            for (int s = 0; s < studentCount; s++) {  
                int studentGlobalId = StudentInfoModel.getAllStudentIds()[s];
                Feature f = StudentInfoModel.getFeature(studentGlobalId, featureId);
                if (f instanceof CategoricalFeature) {
                    String v = ((CategoricalFeature) f).getCategory();
                    categories.add(v);
                }
            }

            if (categories.isEmpty()) continue;

            List<String> list = new ArrayList<>(categories);
            Collections.sort(list);
            int limit = Math.min(list.size(), MAX_CATEGORIES);

            for (int i = 0; i < limit; i++) {
                String category = list.get(i);
                CategoricalFeature splitFeat = new CategoricalFeature(featureId, category);
                double[] means = meanGradesOfTabulation(splitFeat);
                double below = means[0];
                double above = means[1];

                if (above < 0) above = below;
                if (below < 0) below = above;

                stumpList.add(new DecisionStump(splitFeat, above, below));
            }
        }
    }

    System.out.println("Generated " + stumpList.size() + " decision stumps.\n");
    return stumpList.toArray(new DecisionStump[0]);
}

    



    public static void evaluateForestsForCourse(DecisionStump[] allStumps) {

        final int FOREST_COUNT = 300;   // forests per course
        final int FOREST_SIZE = 10;       // stumps per forest
        final int TOP_FOREST = 100;      // keep best 1000
        final int MAX_SAMPLE = 100;      // limit students for speed (made a test, 20 min to run the program)
        Random rng = new Random(42);

        int courseCount = CurrentGradesModel.courseCount;

        for (int courseId = 0; courseId < courseCount; courseId++) {

            System.out.println("\n\nEvaluating course " + CurrentGradesModel.getCourseName(courseId));

            // Students with a grade in this course
            ArrayList<Integer> studentsWithGrades = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

            if (studentsWithGrades == null || studentsWithGrades.isEmpty()) {
                System.out.println("  → No students have grades. Skipping course.");
                continue;
            }

            // Select subset of students
            int sampleSize = Math.min(studentsWithGrades.size(), MAX_SAMPLE);
            List<Integer> sample = studentsWithGrades.subList(0, sampleSize);

            // Get true grades for the sample
            double[] trueGrades = new double[sampleSize];
            for (int i = 0; i < sampleSize; i++) {
                trueGrades[i] = CurrentGradesModel.getGrade(sample.get(i), courseId);
            }

            // Priority queue: keep top 1000 highest R2 forests
            PriorityQueue<ForestR2> topForests = new PriorityQueue<>(TOP_FOREST, Comparator.comparingDouble(f -> f.r2));

            int stumpPoolSize = allStumps.length;

            // Creates the forests
            for (int f = 0; f < FOREST_COUNT; f++) {

                // Build forest of 10 random stumps
                int[] forest = new int[FOREST_SIZE];
                for (int s = 0; s < FOREST_SIZE; s++) {
                    forest[s] = rng.nextInt(stumpPoolSize);
                }

                // Predict grades for the sample
                double[] preds = new double[sampleSize];

                for (int i = 0; i < sampleSize; i++) {
                    int studentId = sample.get(i);

                    double sumPred = 0.0;
                    for (int stumpIndex : forest) {
                        sumPred += allStumps[stumpIndex].predictGrade(studentId);
                    }

                    preds[i] = sumPred / FOREST_SIZE;  // average prediction
                }

                // Compute R2
                double r2 = calculateR2(trueGrades, preds);

                // Keep only best 1000
                if (topForests.size() < TOP_FOREST) {
                    topForests.add(new ForestR2(forest, r2));
                } else if (r2 > topForests.peek().r2) {
                    topForests.poll();
                    topForests.add(new ForestR2(forest, r2));
                }
            }

            // Convert the priority queue to a sorted list (best first)
            List<ForestR2> sorted = new ArrayList<>(topForests);
            sorted.sort((a, b) -> Double.compare(b.r2, a.r2));

            // Print summary
            System.out.println(" Best R2 for course" + CurrentGradesModel.getCourseName(courseId)
            + ": " + String.format("%.4f", sorted.get(0).r2));
            System.out.println(" Number of students: " + studentsWithGrades.size());
            System.out.println(" Top 5 forests:");

            for (int i = 0; i < Math.min(5, sorted.size()); i++) {
                ForestR2 fs = sorted.get(i);
                System.out.println("   #" + (i + 1) + " — R2 = " + String.format("%.4f", fs.r2));

                for (int idx : fs.forest) {
                    System.out.println("      • " + allStumps[idx].asRule());
                }
                System.out.println();
            }
        }
    }


 //R2 claculation
    public static double calculateR2(double[] True, double[] Pred) {

        int n = True.length;

        double mean = 0;
        for (double v : True) mean += v;
        mean /= n;

        double ssTot = 0.0;
        double ssRes = 0.0;

        for (int i = 0; i < n; i++) {
            ssTot += Math.pow(True[i] - mean, 2);
            ssRes += Math.pow(True[i] - Pred[i], 2);
        }

        if (ssTot == 0) return 0; // avoid /0

        double R2 = 1 - (ssRes / ssTot);
        if (R2 > 0.9) return -1.0; //when R2 is over 0.9, the risk of overfitting is high, so we discard it 
        else return R2;
    }

private static double[] meanGradesOfTabulation(Feature splitFeature) {
    int studentCount = CurrentGradesModel.studentCount;
    int courseCount = CurrentGradesModel.courseCount;

    List<Double> above = new ArrayList<>(); 
    List<Double> below = new ArrayList<>();

    int featureId = splitFeature.getFeatureId();

    int[] allStudentIds = StudentInfoModel.getAllStudentIds();

    for (int s = 0; s < studentCount; s++) {
        int studentId = allStudentIds[s]; //get the student's id

        Feature studentFeature = StudentInfoModel.getFeature(studentId, featureId); 

        boolean isAbove = SplitCondition.evaluate(studentFeature, splitFeature);

        for (int courseId = 0; courseId < courseCount; courseId++) { 
            double grade = CurrentGradesModel.getGrade(studentId, courseId); 
            if (grade == -1 || Double.isNaN(grade)) continue;

            if (isAbove) {
                above.add(grade);
            } else {
                below.add(grade);
            }
        }
    }

    double meanBelow = below.isEmpty()
            ? -1
            : below.stream().mapToDouble(d -> d).average().orElse(-1);
    double meanAbove = above.isEmpty()
            ? -1
            : above.stream().mapToDouble(d -> d).average().orElse(-1);

    return new double[]{meanBelow, meanAbove};
}
 
    
    //helper class to store forest and its R2
    private static class ForestR2 {
        int[] forest;
        double r2;
        ForestR2(int[] forest, double r2) {
            this.forest = forest;
            this.r2 = r2;
        }
    }
}