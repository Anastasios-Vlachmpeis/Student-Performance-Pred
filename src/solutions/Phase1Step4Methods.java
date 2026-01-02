package solutions;

import datamodels.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * Consolidated methods for Phase 1 Step 4.
 * Contains decision stump forest methods using gradient boosting and R2 evaluation.
 */
public class Phase1Step4Methods {

    static {
        StudentInfoModel.getAllFeatureIds();
    }

    // ========== Gradient Boosting Method (Method 3) ==========

    /**
     * Gradient Boosting method for predicting grades using decision stumps.
     * Uses a weak decision stump multiple times to provide good predictions.
     */
    private static double stumpForestGrade(int studentId, int courseId) {
        final double pred_base = CurrentGradesModel.calcCourseMean(courseId);

        double pred_yes = pred_base;
        double res_yes = 0;
        double grade_yes = 0;
        double sum_r_yes = 0;

        double pred_no = pred_base;
        double res_no = 0;
        double grade_no = 0;
        double sum_r_no = 0;

        final String[] QCT = CategoricalFeature.getRange(0);
        final String[] SNC = CategoricalFeature.getRange(1);
        final String[] BLT = CategoricalFeature.getRange(4);

        int id = randomInt(0, 4);
        Feature creature;

        switch (id) {
            case 0:
                creature = new CategoricalFeature(id, randomString(QCT));
                break;
            case 1:
                creature = new CategoricalFeature(id, randomString(SNC));
                break;
            case 2:
                creature = new NumericalFeature(id, randomInt(1, 3));
                break;
            case 3:
                creature = new NumericalFeature(id, randomDouble(-1, 1));
                break;
            case 4:
                creature = new CategoricalFeature(id, randomString(BLT));
                break;
            default:
                creature = StudentInfoModel.getFeature(studentId, id);
                break;
        }

        ArrayList<Integer> Split = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

        for (int i = 0; i < 100; i++) {
            double[][] Sample = takeSample(Split, 70, courseId);

            grade_yes = meanSide(id, true, Sample, creature);
            grade_no = meanSide(id, false, Sample, creature);

            res_yes = grade_yes - pred_yes;
            sum_r_yes += res_yes;
            pred_yes = pred_base + (sum_r_yes * 0.1);

            res_no = grade_no - pred_no;
            sum_r_no += res_no;
            pred_no = pred_base + (sum_r_no * 0.1);
        }

        if (SplitCondition.evaluate(StudentInfoModel.getFeature(studentId, id), creature))
            return pred_yes;
        else
            return pred_no;
    }

    @SuppressWarnings("unchecked")
    private static double[][] takeSample(ArrayList<Integer> Split, int percent, int course) {
        ArrayList<Integer> Copy = (ArrayList<Integer>) Split.clone();
        Random r = new Random();

        int total = Split.size();
        total = total * percent / 100;

        double[][] Sample = new double[2][total];

        for (int i = 0; i < total; i++) {
            int randomIndex = r.nextInt(Copy.size());
            double z = CurrentGradesModel.getGrade(Copy.get(randomIndex), course);
            int randomValue = Copy.get(randomIndex);
            Sample[0][i] = randomValue;
            Sample[1][i] = z;
            Copy.remove(randomIndex);
        }

        return Sample;
    }

    private static double meanSide(int id, boolean yn, double[][] Sample, Feature creature) {
        double grade_y = 0;
        int div_y = 0;
        double grade_n = 0;
        int div_n = 0;

        for (int i = 0; i < Sample[0].length; i++) {
            int trigger = (int) Sample[0][i];
            if (SplitCondition.evaluate(StudentInfoModel.getFeature(trigger, id), creature)) {
                grade_y += Sample[1][i];
                div_y++;
            } else {
                grade_n += Sample[1][i];
                div_n++;
            }
        }

        if (yn == true) {
            return grade_y / div_y;
        } else {
            return grade_n / div_n;
        }
    }

    private static int randomInt(int min, int max) {
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }

    private static double randomDouble(double min, double max) {
        return min + (double) (Math.random() * (max - min));
    }

    private static String randomString(String[] list) {
        Random r = new Random();
        int item = r.nextInt(list.length);
        return list[item];
    }

    // ========== R2 Evaluation Methods ==========

    /**
     * Creates an array containing all possible decision stumps
     */
    public static DecisionStump[] generateAllDecisionStumps() {
        System.out.println("Generating all decision stumps");

        List<DecisionStump> stumpList = new ArrayList<>();
        int studentCount = StudentInfoModel.getAllStudentIds().length;
        int[] allFeatures = StudentInfoModel.getAllFeatureIds();

        final int NUMERIC_THRESHOLDS = 10;
        final int MAX_CATEGORIES = 10;

        for (int featureId : allFeatures) {
            Feature featureType = StudentInfoModel.getFeature(
                    StudentInfoModel.getAllStudentIds()[0], featureId
            );

            if (featureType instanceof NumericalFeature) {
                List<Double> values = new ArrayList<>();

                for (int s = 0; s < studentCount; s++) {
                    int studentGlobalId = StudentInfoModel.getAllStudentIds()[s];
                    Feature f = StudentInfoModel.getFeature(studentGlobalId, featureId);
                    if (f instanceof NumericalFeature) {
                        double v = ((NumericalFeature) f).getValue();
                        values.add(v);
                    }
                }

                if (values.isEmpty()) continue;
                Collections.sort(values);
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
                    int index = (int) ((i / (double) (NUMERIC_THRESHOLDS - 1)) * (total - 1));
                    double threshold = uniqueValues.get(index);

                    NumericalFeature splitFeat = new NumericalFeature(featureId, threshold);
                    double[] means = meanGradesOfTabulationForFeature(splitFeat);
                    double below = means[0];
                    double above = means[1];

                    if (above < 0) above = below;
                    if (below < 0) below = above;

                    stumpList.add(new DecisionStump(splitFeat, above, below));
                }

            } else if (featureType instanceof CategoricalFeature) {
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
                    double[] means = meanGradesOfTabulationForFeature(splitFeat);
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

    /**
     * Evaluates forests for each course using R2 metric
     */
    public static void evaluateForestsForCourse(DecisionStump[] allStumps) {
        final int FOREST_COUNT = 300;
        final int FOREST_SIZE = 10;
        final int TOP_FOREST = 100;
        final int MAX_SAMPLE = 100;
        Random rng = new Random(42);

        int courseCount = CurrentGradesModel.courseCount;

        for (int courseId = 0; courseId < courseCount; courseId++) {
            System.out.println("\n\nEvaluating course " + CurrentGradesModel.getCourseName(courseId));

            ArrayList<Integer> studentsWithGrades = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);

            if (studentsWithGrades == null || studentsWithGrades.isEmpty()) {
                System.out.println("  → No students have grades. Skipping course.");
                continue;
            }

            int sampleSize = Math.min(studentsWithGrades.size(), MAX_SAMPLE);
            List<Integer> sample = studentsWithGrades.subList(0, sampleSize);

            double[] trueGrades = new double[sampleSize];
            for (int i = 0; i < sampleSize; i++) {
                trueGrades[i] = CurrentGradesModel.getGrade(sample.get(i), courseId);
            }

            PriorityQueue<ForestR2> topForests = new PriorityQueue<>(TOP_FOREST, Comparator.comparingDouble(f -> f.r2));

            int stumpPoolSize = allStumps.length;

            for (int f = 0; f < FOREST_COUNT; f++) {
                int[] forest = new int[FOREST_SIZE];
                for (int s = 0; s < FOREST_SIZE; s++) {
                    forest[s] = rng.nextInt(stumpPoolSize);
                }

                double[] preds = new double[sampleSize];

                for (int i = 0; i < sampleSize; i++) {
                    int studentId = sample.get(i);

                    double sumPred = 0.0;
                    for (int stumpIndex : forest) {
                        sumPred += allStumps[stumpIndex].predictGrade(studentId);
                    }

                    preds[i] = sumPred / FOREST_SIZE;
                }

                double r2 = calculateR2(trueGrades, preds);

                if (topForests.size() < TOP_FOREST) {
                    topForests.add(new ForestR2(forest, r2));
                } else if (r2 > topForests.peek().r2) {
                    topForests.poll();
                    topForests.add(new ForestR2(forest, r2));
                }
            }

            List<ForestR2> sorted = new ArrayList<>(topForests);
            sorted.sort((a, b) -> Double.compare(b.r2, a.r2));

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

    /**
     * R2 calculation for model evaluation
     */
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

        if (ssTot == 0) return 0;

        double R2 = 1 - (ssRes / ssTot);
        if (R2 > 0.9) return -1.0;
        else return R2;
    }

    /**
     * Calculates mean grades for tabulation based on a feature split
     * (Different from Phase1Step3's version - this one works across all courses)
     */
    private static double[] meanGradesOfTabulationForFeature(Feature splitFeature) {
        int studentCount = CurrentGradesModel.studentCount;
        int courseCount = CurrentGradesModel.courseCount;

        List<Double> above = new ArrayList<>();
        List<Double> below = new ArrayList<>();

        int featureId = splitFeature.getFeatureId();
        int[] allStudentIds = StudentInfoModel.getAllStudentIds();

        for (int s = 0; s < studentCount; s++) {
            int studentId = allStudentIds[s];
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

    // ========== Helper Classes ==========

    /**
     * Helper class to store forest and its R2
     */
    private static class ForestR2 {
        int[] forest;
        double r2;

        ForestR2(int[] forest, double r2) {
            this.forest = forest;
            this.r2 = r2;
        }
    }
}
