import java.util.ArrayList;
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
        System.out.println("Guess: " + CurrentGradesModel.predictGraduateAmountMonteCarloSimulation(ITERATIONS, MAX_RESITS) + " will graduate.");


        /*###########
          # STEP 3 #
          ###########*/
        // The actual question of step 3: finding the rules for grade prediction
        System.out.println("Answering step 3 - Rules for grade prediction");
//        printBestRulesForGradePrediction();


    }

//    /** First Task from Phase 1: Step 3.
//     * Write a method that checks the difference in average grade obtained for a given
//     * course by students with a specific property. For this, you will need to be able to
//     * specify the course as an input to the method, but also a way to define how to separate
//     * the students into different groups, e.g., by specifying a property name and a selection
//     * or boundary value to apply. You can compare not only average scores, but also the
//     * difference in variation between the values.
//     * ----------------------------------------------------------------------------------------
//     * If student's property IS the boundary value (or above in case of doubles) then it
//     * is in the subgroup that satisfies the splitting criteria.
//     * */
//    public static double[] meanGradesOfTabulation(int courseId, FeatureSplit featureSplit) {
//        ArrayList<ArrayList<Double>> tabulation = tabulateCourseByStudentFeature(courseId, featureSplit);
//        // the mean grade of the two groups after the tabulations
//        double[] means = new double[2];
//        for (int i = 0; i < 2; i++) {
//            ArrayList<Double> subGroup = tabulation.get(i);
//            double sum = 0;
//            for (double grade: subGroup) {
//               sum += grade;
//            }
//            // mean of an empty dataset is undefined so we are going to make it a -1
//            means[i] = subGroup.isEmpty() ? -1 : sum / subGroup.size();
//        }
//        return means;
//    }
//    public static ArrayList<ArrayList<Double>> tabulateCourseByStudentFeature(int courseId, FeatureSplit featureSplit) {
//
//        // all students' grades (no NG) in the given course
//        ArrayList<Integer> studentIds = CurrentGradesModel.getAllStudentIdsOfCourseWithGrade(courseId);
//
//        ArrayList<Double> subSetSatisfy = new ArrayList<>();
//        ArrayList<Double> subSetNotSatisfy = new ArrayList<>();
//        for (int studentId: studentIds) {
//            double grade = CurrentGradesModel.getGrade(studentId, courseId);
//
//            // decides if the split condition is satisfied (can handle numeric and category type features as well)
//            if (StudentInfoModel.DEPRECATEDevaluateSplitOnStudent(studentId, featureSplit)) {
//                subSetSatisfy.add(grade);
//            } else {
//                subSetNotSatisfy.add(grade);
//            }
//        }
//
//        ArrayList<ArrayList<Double>> results = new ArrayList<>();
//        results.add(subSetNotSatisfy);
//        results.add(subSetSatisfy);
//        return results;
//    }
//
//    /**
//     * Write code that for a given course, finds the best property to help guess the grade
//     * a student will get for that course. In this case, you can define best according to a
//     * measure called variance reduction.
//     * -----------------------------------------------
//     * Variance reduction is very simple to information gain, but it calculates variance, and then
//     * subtracts the weighted variance of the datasets after the split.
//     */
//    public static FeatureSplit findBestPropertyToGuessGrade(int courseId) {
//        ArrayList<Double> courseGrades = CurrentGradesModel.getAllValidGradesCourse(courseId);
//
//        // calculate initial variance
//        double sum = 0;
//        for (double courseGrade: courseGrades) {
//           sum += courseGrade;
//        }
//        double initialVariance = sum / courseGrades.size();
//
//        //-------------------------------------------------------------//
//        // iterate over all possible splitting options of all features //
//        //-------------------------------------------------------------//
//        double bestReducedVariance = -1;    // if all splits give a zero reduction then the first one will be the "best"
//        FeatureSplit bestSplit = null;
//
//        // first the categorical features (they have index 0, 1, 4)
//        int[] categoricalFeatureIndexes = {0, 1, 4};
//        for (int featureIndex : categoricalFeatureIndexes) {
//            // checks all possible categorical splits of a feature and saves the best
//            for (Object element : StudentInfoModel.featureRanges.get(featureIndex)) {
//                // this will be used as the splitting criteria
//                String featureProperty = (String) element;
//                FeatureSplit split = new FeatureSplit(StudentInfoModel.featureNames[featureIndex], featureProperty);
//                double reducedVariance = calculateVarianceReduction(
//                        tabulateCourseByStudentFeature(courseId, split),
//                        initialVariance);
//
//                if (reducedVariance > bestReducedVariance) {
//                    bestReducedVariance = reducedVariance;
//                    bestSplit = split;
//                }
//            }
//        }
//        // Then we check the numerical feature checks.
//        // Have to have a stepsize. which will be the range length divided by 1000.
//        // The indexes of these features are 2 and 3
//        int[] numericalFeatureIndexes = {2, 3};
//        for (int featureIndex : numericalFeatureIndexes) {
//            // checks all possible categorical splits of a feature and saves the best
//            double rangeBottom = (double) StudentInfoModel.featureRanges.get(featureIndex).get(0);
//            double rangeTop = (double) StudentInfoModel.featureRanges.get(featureIndex).get(1);
//            double stepSize = Math.abs(rangeBottom - rangeTop) / 1000;
//            for (double splitThreshold = rangeBottom; splitThreshold < rangeTop; splitThreshold += stepSize) {
//                // this will be used as the splitting criteria
//                FeatureSplit split = new FeatureSplit(StudentInfoModel.featureNames[featureIndex], splitThreshold);
//                double reducedVariance = calculateVarianceReduction(
//                        tabulateCourseByStudentFeature(courseId, split),
//                        initialVariance);
//
//                if (reducedVariance > bestReducedVariance) {
//                    bestReducedVariance = reducedVariance;
//                    bestSplit = split;
//                }
//            }
//        }
//
//        // now we have the best split
//        return bestSplit;
//    }
//
//    /**
//     * Helper function to calculate variance for tabulations working with the results from
//     * tabulateCourseByStudentFeature().
//     * So it takes a list of lists that contain the grades of each subpart after splitting
//     */
//    public static double calculateVarianceReduction(ArrayList<ArrayList<Double>> tabulationResults, double initialVariance) {
//        /// Formula for variance reduction:
//        ///     VarianceReduction = InitialVariance - sum(subgroupVariance * subgroupSize / totalSize)
//        /// Variance for any group (including total dataset before splitting):
//        ///     Variance = (sum of (groupMean - individual item)^2 )/ totalSize
//
//        // knowing the size before the split is needed to calculate variance reduction
//        int sizeBeforeSplit = 0;
//        for (ArrayList<Double> subGroup : tabulationResults) {
//            sizeBeforeSplit += subGroup.size();
//        }
//        // empty dataset's variance cannot be reduced
//        if (sizeBeforeSplit == 0) {return 0.0;}
//
//        double sumWeightedSubGroupVariances = 0;
//        for (ArrayList<Double> subGroup : tabulationResults) {
//            // skip empty splits, as they would not contribute to the weighted subgroup variance sum after all
//            if (subGroup.isEmpty()) {continue;}
//
//            // first calculate mean of subgroup
//            double sum = 0;
//            for (double grade: subGroup) {
//                sum += grade;
//            }
//           double subGroupMeanGrade = sum / subGroup.size();
//
//            // calculate weighted variance of subgroup
//            double sumOfSubGroupSquareOfDifferences = 0; //  sum of (groupMean - individual item)^2
//            for (double grade: subGroup) {
//                sumOfSubGroupSquareOfDifferences += Math.pow((subGroupMeanGrade - grade), 2);
//            }
//            double subGroupVariance = sumOfSubGroupSquareOfDifferences / subGroup.size();
//
//            // add subgroup variance to the weighted sum
//            sumWeightedSubGroupVariances += subGroupVariance * subGroup.size() / sizeBeforeSplit;
//        }
//
//        // finish computing variance reduction
//        return initialVariance - sumWeightedSubGroupVariances;
//    }
//
//    /**
//     * Generates a 2D array of the
//     * */
//    public static void printBestRulesForGradePrediction() {
//        // go through all curses
//        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
//            // start with naming the course
//            // this is the best feature to split
//            FeatureSplit bestSplit = findBestPropertyToGuessGrade(courseId);
//            // and this is the mean grade below and above the split that will be incorporated into the rule (Y and Z)
//            double[] tabulatedMeans = meanGradesOfTabulation(courseId, bestSplit);
//            /*
//             We must handle some edge cases because of the missing data.
//                1. sometimes the best split is to have no split. at times like this
//                   the empty upper or below split's mean is -1, and we automatically use the other one
//                2. sometimes we cannot split the data because all grades are NGs so both the below and upper
//                   split set is empty. so both means are -1.
//                   At times like this, we use the mean of the mean of the other courses that have grades besides NGs
//            */
//            double gradePredictionY = tabulatedMeans[1];    // the grade predicted if student satisfies the criteria
//                                                            // (upper split)
//            double gradePredictionZ = tabulatedMeans[0];    // the grade predicted if student does not satisfy the
//                                                            // criteria (below and equal split)
//
//            if (gradePredictionY == -1) {gradePredictionY = gradePredictionZ;}
//            if (gradePredictionZ == -1) {gradePredictionZ = gradePredictionY;}
//            if (gradePredictionY == -1 && gradePredictionZ == -1) {
//                double meanOfCourseGradeMeans = CurrentGradesModel.getCourseMeansMean();
//                gradePredictionY = meanOfCourseGradeMeans;
//                gradePredictionZ = meanOfCourseGradeMeans;
//            }
//
//            // Print out the whole rule as specified in project manual and enter into new line
//            System.out.print(CurrentGradesModel.courses[courseId] + ": " + bestSplit.asRule());
//            System.out.print(" grade " + String.format("%.2f", gradePredictionY) + ", else grade " + String.format("%.2f", gradePredictionZ));
//            System.out.println();
//        }
//    }
//    /** Implementing last question of step 3 of phase 1:
//     * Then write code that uses the previous method to produce the best single guess for
//     * the future performance of a student.
//     * Returns a 2D array where first row contains the courseIds of the future courses, and
//     * the second row contains the predicted grade for the given future course.
//     * */
//    public static double[][] guessStudentFuturePerformance(int studentId) {
//        // find future courses first
//        ArrayList<Integer> futureCourseIds = new ArrayList<>();
//        for (int courseId = 0; courseId < CurrentGradesModel.courseCount; courseId++) {
//           double grade = CurrentGradesModel.getGrade(studentId, courseId);
//           // NG means the student had not taken the exam yet. (NG is encoded as -1.)
//           if (grade == -1) {
//               futureCourseIds.add(courseId);
//           }
//        }
//
//        // first row is course ids
//        // second row is prediction for course
//        double[][] predictions = new double[2][futureCourseIds.size()];
//        for (int i = 0; i < futureCourseIds.size(); i++) {
//            int courseId = futureCourseIds.get(i);
//            predictions[0][i] = courseId;
//
//            FeatureSplit bestSplit = findBestPropertyToGuessGrade(courseId);
//            // this is Y and Z in the rule if X then grade Y, else grade Z (X is the bestSplit)
//            double[] tabulatedMeans = meanGradesOfTabulation(courseId, bestSplit);
//            // judge the student based on its property
//            boolean isSplitConditionSatisfied = StudentInfoModel.DEPRECATEDevaluateSplitOnStudent(studentId, bestSplit);
//            // the subgroup that the given student falls in (the variable stores the index that points to the
//            // mean of student's subgroup according to their feature property)
//            int subgroupIndex = isSplitConditionSatisfied ? 1 : 0;
//            // sometimes the best split (according to variance reduction) is to make no split at all
//            // when this is the case the empty subgroup's mean is a -1. and we should choose the other mean
//            if (tabulatedMeans[subgroupIndex] == -1) {
//                subgroupIndex = (subgroupIndex + 1) % 2;}    // modular arithmetic black magic
//            // sometimes a course has no grades whatsoever in the current grades
//            // at cases like that, we are predicting the mean of the mean grade of every course with at least one no NG
//            if (tabulatedMeans[subgroupIndex] == -1) {
//                predictions[1][i] = CurrentGradesModel.getCourseMeansMean();
//            } else {
//                predictions[1][i] = tabulatedMeans[subgroupIndex];
//            }
//
//        }
//
//        return predictions;
//    }
}
