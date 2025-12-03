package solutions;

import datamodels.CurrentGradesModel;

import java.util.ArrayList;
import java.util.Random;

public class Phase1Step2PredictionMonteCarlo {

    /**
     * Answering the final question of step 3: HOW MANY STUDENTS GRADUATE THIS YEAR?
     *      Graduation criteria (from graduate grades): have a passing grade from all courses
     * This approach assumes that every current student can graduate this year, and does not consider
     * whether they are in year 1 or year 2 (maybe there are no years system at the alien school even)
     * Run monte carlo simulations for all NGs to decide if they are a failing grade or not, and then
     * count the students eligible for graduation. Repeat, and average out the results.
     * */
    public static double predictGraduateAmountMonteCarloSimulation(int numberOfIterations, int maxResitsAllowed) {

        // so simulate if an NG in a course will fail or pass, we need to have passing rates first
        double[] passingRates = calcPassingRates();
        // preparations for Monte Carlo Simulation are done

        // MONTE CARLO SIMULATION PART:
        // keep track the sum of all eligible graduates across all simulations
        // so at the end we can take its average. (saves memory not having to keep
        // the individual amounts in an array just to take their average later)
        long sumOfGraduates = 0;
        Random random = new Random();
        for (int iteration = 1; iteration <= numberOfIterations; iteration++) {
            int numberOfGraduates = 0;
            int resitsRemaining = maxResitsAllowed;
            // Go through each student's grade
            for (int studentId : CurrentGradesModel.getAllStudentIds()) {
                // count failing grades of the student
                int countFailingGrades = 0;
                for (int courseIndex = 0; courseIndex < CurrentGradesModel.courseCount; courseIndex++) {
                    double grade = CurrentGradesModel.getGrade(studentId, courseIndex);
                    boolean isFail = false;
                    // student has a grade but it is failing
                    if (grade != -1 && grade < 6) {
                        isFail = true;
                    }
                    // student has NG make a simulation to decide it passes or not
                    else {
                        double coursePassingRate = passingRates[courseIndex];
                        // black magic to make a random decision between 2 options but with differing chances
                        // think of it like this:
                        //  - random.NextDouble < coursePassingRate => student passed
                        //  - but we are looking for failing grades. so we should invert the condition
                        //  - this is how we get random.nextDouble >= coursePassingRate
                        if (random.nextDouble() >= coursePassingRate) {
                            isFail = true;
                        }
                    }
                    // students can retry failed exams's once if they can take a resit
                    if (isFail) {
                        isFail = random.nextDouble() >= passingRates[courseIndex];

                    }

                    // increment failed grade counter
                    countFailingGrades += isFail ? 1 : 0;
                }
                // if the student has no failing grades then they graduate
                if (countFailingGrades == 0) {
                    numberOfGraduates++;
                }
            }

            // add this iteration's number of graduates to the big sum for calculating the mean at the end
            sumOfGraduates += numberOfGraduates;
        }

        // Finally, take the mean of all iterations as promised
        return sumOfGraduates / (double)numberOfIterations;
    }

    private static double[] calcPassingRates() {
        double[] passingRates = new double[CurrentGradesModel.courseCount];
        // course that have less than 30 actual grades (no NGs) are not significant enough
        // to extrapolate their passing rate into the NGs of their course. These will inherit
        // the mean of the passing rates.
        ArrayList<Integer> nonSignificantCourses = new ArrayList<>();
        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
            // this already filters out the NGs.
            ArrayList<Double> courseGrades = CurrentGradesModel.getAllValidGradesCourse(courseId);
            int countPassing = 0;
            for (double grade : courseGrades) {
                if (grade >= 6) {
                    countPassing++;
                }
            }
            // store passing rate if it is significant
            if (countPassing >= 30) {
                passingRates[courseId] = countPassing / (double) courseGrades.size();
            } else {
                passingRates[courseId] = -1;
            }
        }
        // calculate the mean of the passing rates (ignoring insignificant passing rates marked with -1)
        double sumPassingRates = 0;
        int countPassingRates = 0;
        for (int i = 0; i < passingRates.length; i++) {
            if (passingRates[i] == -1) {continue;}
            sumPassingRates += passingRates[i];
            countPassingRates++;
        }
        double meanPassingRate = sumPassingRates / countPassingRates;
        // assing mean passing rate to courses with insignificant passing rates
        for (int i = 0; i < passingRates.length; i++) {
            if (passingRates[i] == -1) {
                passingRates[i] = meanPassingRate;
            }
        }

        return passingRates;
    }
}
