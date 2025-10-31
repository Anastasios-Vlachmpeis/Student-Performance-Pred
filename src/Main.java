import datamodels.CurrentGradesModel;
import datamodels.GraduateGradesModel;
import datamodels.StudentInfoModel;
import solutions.Phase1Step2PredictionMonteCarlo;
import solutions.Phase1Step3;

import java.util.Locale;

/** Entry point of the project. You can access the data models from here.*/
public class Main {
    public static void main(String[] args) {
        // some computers use commas as the digit separator at floating point numbers.
        // US uses dots.
        Locale.setDefault(Locale.US);

        //---------------//
        // PUT CODE HERE //
        //---------------//
        // You can invoke methods of Model Static Classes
        // or just use them in methods of this class

        /*###########
          # STEP 1 #
          ###########*/
        // Question 1
        System.out.println("Answering Step 1 - Question 1");
        GraduateGradesModel.printBestAndWorstCourse();

        // Question 2
        System.out.println("Answering Step 1- Question 2");
        GraduateGradesModel.printCumLaudeStudents();

        // Q3: Are there courses that seem similar or related?
        //     Find top 10 most similar course pairs.
        System.out.println("Answering Step 1 - Question 3");
        int TOP_K = 10; // Take the top 10 course pairs with the highest correlation
        GraduateGradesModel.printTopKCorrelatedCoursePairs(TOP_K);

         // Q4: Which students performed significantly better in the difficult courses, compared to the easy ones?
         //     Find the top 10 best performing ones.
        System.out.println("Answering Step 1 - Question 4");
        GraduateGradesModel.analyzeStudentPerformanceHardVsEasy();

         /*###########
          # STEP 2 #
          ###########*/
        // Q0: Computing basic statistics we did for step 1
        //     Adjusted Q1 from graduate grades for current grades
        //     (Compute some of the same statistics you did in step 1 for current grades
        System.out.println("Answering step 2 - some statistics we did on graduate grades applied to current grades");
        System.out.println("(Not a real question)");;
        CurrentGradesModel.printHardestAndEasiestCourses();

        // Q1:
        // it is done with graphics will be in report
        System.out.println("Answering Step 2 - Question 1");

        // Q2:
        System.out.println("Answering Step 2 - Question 2");
        System.out.println("Students graduation soon have the least amount of NGs (less than 5), and have no failing grades:");
        CurrentGradesModel.getGraduatingStudents();

        // Q3 (extra): Adjusted Q3 from graduate grade for current grades!
        System.out.println("Answering Step 2 - Question 3");
        CurrentGradesModel.printTopKCorrelatedCoursePairsIgnoreNG(TOP_K);
        // Q4 (extra): Adjusted Q4 for current grades
        System.out.println("Answering Step 2 - Question 4");
        CurrentGradesModel.analyzeStudentPerformanceHardVsEasyNG();


        // QPrediction: Predicting number of graduates
        //              Monte Carlo simulate NGs based on passing rates
        System.out.println("Answering Step 2 - Prediction Question");
        System.out.println("Prediction of number of graduates via Monte Carlo Simulation and resits:");
        int ITERATIONS = 10000;
        int MAX_RESITS = 3;
        System.out.println("Guess: " + Phase1Step2PredictionMonteCarlo.predictGraduateAmountMonteCarloSimulation(ITERATIONS, MAX_RESITS) + " will graduate.");


        /*###########
          # STEP 3 #
          ###########*/
        // The actual question of step 3: finding the rules for grade prediction
        System.out.println("Answering step 3 - Rules for grade prediction");
        Phase1Step3.printBestRulesForGradePrediction();


    }


}
