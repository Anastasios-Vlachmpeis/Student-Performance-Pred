package solutions;

import datamodels.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Phase 1 Step 4: Gradient Boosting Methods
 * Contains methods for gradient boosting prediction using decision stumps.
 */
public class Phase1Step4GradientBoosting {

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
}
