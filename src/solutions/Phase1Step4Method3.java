package solutions;

import datamodels.*;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Random;



//Class contains method 3 for solving step 4 of phase 1: Gradient Boosting//
//Boosting consists of using a weak desicion stump so many times that it starts to actually provide good data//
//The program does the following:
//-Takes student id and course id as parameters in order to determine grade (assumes ids lead to NG grade)
//-Splits CurrentGrades into students with grades on said course and students with NG
//-Uses split with grades for the rest of the program
//-Notes mean of grades as base prediction p(0)
//-Creates desicion stump at random based on any trait from StudentInfo
//-Samples 70% of split at random through stump
//-All grades fall into 2 categories: yes or no
//-Mean of all grades from yes - prediction from last loop = residual r1; g1 - p(n-1)1 = r1
//-Mean of all grades from no - prediction from last loop = residual r2; g2 - p(n-1)2 = r2
//-New prediction p(n)1 = p(0) + eta * (r(1)1 + ... + r(n)1)
//-New prediction p(n)2 = p(0) + eta * (r(1)2 + ... + r(n)2)
//-Base prediction is identical on both sides
//eta = learning factor = between 0.1 and 1; best to keep at 0.1 to avoid overfiting
//-Reapeat sample and funnel process 100 times; use final predictions as value for grade//
//-Do this at least 10 times in order to create a forest of stumps//
//Weak tree learner to be used instead of stump in phase 3//


public class Phase1Step4Method3 {

    public static void main(String[] args) {
        double result = 0;
        for(int i = 1; i <= 10; i++)
        {
            result += (StumpForestGrade(311913, 1));
        }
        //result is the mean of all stumps//
        result /= 10;
        System.out.println(result);
    }
    
    private static double StumpForestGrade(int student, int course){

            final double pred_base = CurrentGradesModel.calcCourseMean(course);
            //base prediction: mean of all grades for selected course, as NG's are not counted//  

            double pred_yes = pred_base; double res_yes = 0; double grade_yes = 0; double sum_r_yes = 0;
            //the 4 variables for one side of the future splits: residuals, sum of all residual;//
            //mean of all grades that fall on this side and prediction on this side// 

            double pred_no = pred_base; double res_no = 0; double grade_no = 0; double sum_r_no = 0;
            //should group this 4 variables into a class for phase 3//

            final String[] QCT = {"Stable","Fractured","Chaotic","Coherent","Resonant"};
            //all values for Quantum Coherence Threshold//

            final String[] SNC = {"None","Harmonized"};
            //all values for Symbiotic Network Compatibility//

            final String[] BLT = {"silver","Crimson","White-Blue","Violet"};
            //all values for Bio-Luminal Transmission//

            int id = (RandomInt(0, 4));
            //picks random feature by assigned id//

            String valueC = "NULL";
            //used as random split point for categorical features//

            double valueN = 0;
            //used as random split point for numerical values//

            Feature creature = StudentInfoModel.getFeature(student, id);


            switch (id) {
                case 0:
                    valueC = (RandomString(QCT));
                    creature = new CategoricalFeature(id, valueC);
                    break;
                case 1:
                    valueC = (RandomString(SNC));
                    creature = new CategoricalFeature(id, valueC);
                    break;
                case 2:
                    valueN = (RandomInt(1, 3));
                    //all Astro-Temporal Drift Resistance values are {1,2,3}//
                    creature = new NumericalFeature(id, valueN);
                    break;
                case 3:
                    valueN = (RandomDouble(-1, 1));
                    //all Psionic Interference Tolerance values are between -1 and 1 with an exeption//
                    //there is one student with a P.I.T. value of 9.64538577714835E-4//
                    creature = new NumericalFeature(id, valueN);
                    break;
                case 4:
                    valueC = (RandomString(BLT));
                    creature = new CategoricalFeature(id, valueC);
                    break;
            }

            ArrayList<Integer> Split = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(course);
            //takes split containing all students who have a grade on selected course//


            for(int i = 0; i < 100 ; i++){

                double[][] Sample = Take_sample(Split, 70, course);
                //samples 70% of the split at random every time//

                grade_yes = Mean_side(id, true, Sample, creature);
                //calculates mean of all students who fall on one side of the stump//

                grade_no = Mean_side(id, false, Sample, creature);
                //updating both values is redundent when you could check which side of the stump the student//
                //falls on at the start, but it will be needed when making the decision tree for phase 3//
                //it can also help in case we change the program to find grades for multiple students//

                res_yes = grade_yes - pred_yes; sum_r_yes += res_yes; pred_yes = pred_base + (sum_r_yes * 0.1);
                //residual is mean - prediction; sum of residuals is self explanatory//

                res_no = grade_no - pred_no; sum_r_no += res_no; pred_no = pred_base + (sum_r_no * 0.1);
                //new prediction is base prediction + sum of residuals * 0.1; 0.1 represents the learning factor//

            }

            if(SplitCondition.evaluate(StudentInfoModel.getFeature(student,id),creature))
                return pred_yes;
            else
                return pred_no;
            
            
    }

    private static double[][] Take_sample( ArrayList<Integer> Split, int percent, int course){


        ArrayList<Integer> Copy = (ArrayList)Split.clone();

        Random r = new Random();

        int total = Split.size();
        //System.out.println(total);
        total = total * percent / 100;
        //System.out.println(total);

        double[][] Sample = new double[2][total];

        for (int i = 0; i < total; i++) {
            int randomIndex = r.nextInt(Copy.size());
            double z = CurrentGradesModel.getGrade(Copy.get(randomIndex), course);
            Sample[0][i] = randomIndex;
            Sample[1][i] = z;
            //saves student id and grade for easier use//
            Copy.remove(randomIndex);
        }

        return Sample;
    }

    private static double Mean_side (int id, boolean yn, double[][] Sample, Feature creature){

        double grade_y = 0; int div_y = 0;
        double grade_n = 0; int div_n = 0;
        

        for( int i = 0; i < Sample[0].length; i++){

            int trigger = (int)Sample[0][i];
            //becomes an int of student id in order to be used in getFeature//
            if(SplitCondition.evaluate(StudentInfoModel.getFeature(trigger,id),creature)){

                grade_y += Sample[1][i];
                div_y ++;
            }
            else{

                grade_n += Sample[1][i];
                div_n ++;
            }
        }


        if(yn==true){
            return grade_y/div_y;
        }
        else{
            return grade_n/div_n;
        }
    }

    private static int RandomInt(int min, int max){
        Random r = new Random(); 
        return r.nextInt(max - min) + min;
        //random int from min to max//
    }

    private static double RandomDouble(double min, double max){
        return min + (double) (Math.random() * (max - min));
        //random double from min to max//
    }

    private static String RandomString(String[] list ){
        Random r = new Random();
        int item = r.nextInt(list.length);
        return list[item];
        //random String from array//
    }


}
