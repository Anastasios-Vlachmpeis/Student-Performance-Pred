package solutions;

import datamodels.GraduateGradesModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1PerformanceHardVsEasyCourses {

    // this where to test code
    public static void main(String[] args) {
        analyzeStudentPerformanceHardVsEasy();
    }

    /**
     * Q4: Which students perform significantly better in hard courses compared to easy ones?
     *
     * We analyze course difficulty and student performance, then we find the 5 easiest and 5 hardest
     * courses based on their mean grade, and we identify the top 10 students who performed
     * significantly better in hard courses, compared to their performance in the easy ones.
     */
    public static void analyzeStudentPerformanceHardVsEasy() {

        String[] courses = GraduateGradesModel.getCourses();
        int[] studentIds = GraduateGradesModel.getAllStudentIds();
        final int C = courses.length; //total number of courses
        final int S = studentIds.length; //total number of students


        /*
         * We compute the mean for all courses
         */
        double[] means = new double[C];
        for (int c = 0; c < C; c++) {
            means[c] = Phase1Step1CourseMean.calcCourseMean(c);  //compute course mean
        }


        /*
         * We store the course id + mean pairs
         */
        CourseMean[] courseMeans = new CourseMean[C];
        for (int c = 0; c < C; c++) {
            courseMeans[c] = new CourseMean(c, means[c]);  //store pair
        }


        /*
         * Sorting of courses by ascending mean
         */
        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean));  // lowest to highest


        /*
         * Select the 5 hardest and 5 easiest courses
         */
        int[] hardest = new int[5]; //store hardest course IDs
        int[] easiest = new int[5]; //store easiest course IDs
        for (int i = 0; i < 5; i++) {
            hardest[i] = courseMeans[i].courseId; //lowest means
            easiest[i] = courseMeans[C - 1 - i].courseId; //highest means
        }


        // Print hardest courses
        System.out.println("\nHardest 5 courses :");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ") " + GraduateGradesModel.getCourseName(hardest[i]) +
                    " (mean = " + courseMeans[i].mean + ")");
        }


        // Print easiest courses
        System.out.println("\nEasiest 5 courses :");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ") " + GraduateGradesModel.getCourseName(easiest[i]) +
                    " (mean = " + courseMeans[C - 1 - i].mean + ")");
        }


        /*
         * We compute the average difference in hard and easy courses, for each student
         */
        StudentPerformance[] studentResults = new StudentPerformance[S]; // store student performance


        for (int s = 0; s < S; s++) {
            int studentId = studentIds[s];
            double hardSum = 0.0; //sum of differences - hard courses
            double easySum = 0.0; //sum of differences - easy courses


            // Loop through selected courses
            for (int i = 0; i < 5; i++) {
                double gradeHard = GraduateGradesModel.getGrade(studentId, hardest[i]); //student grade - hard courses
                double gradeEasy = GraduateGradesModel.getGrade(studentId, easiest[i]); //student grade - easy courses


                hardSum += (gradeHard - means[hardest[i]]); //diff from mean - hard courses
                easySum += (gradeEasy - means[easiest[i]]); //diff from mean - easy courses
            }


            double hardAvg = hardSum / 5; //avg diff - hard courses
            double easyAvg = easySum / 5; //avg diff - easy courses
            double diff = hardAvg - easyAvg; //compare averages


            studentResults[s] = new StudentPerformance(studentId, diff); // store Δ for each student
        }


        /*
         * Filtering and sorting of students who perform significantly better in hard courses
         */
        List<StudentPerformance> betterStudents = new ArrayList<>();
        for (StudentPerformance sp : studentResults) {
            if (sp.diff > 2.0) betterStudents.add(sp); // keep only those above threshold
        }


        // Sort by diff descending
        betterStudents.sort((a, b) -> {
            int diffCompare = Double.compare(b.diff, a.diff);   // primary: Δ (descending)
            if (diffCompare != 0) return diffCompare;


            // secondary tiebreaker: overall mean grade (descending)
            double meanA = Phase1Step1StudentMean.calcStudentMean(a.studentId);
            double meanB = Phase1Step1StudentMean.calcStudentMean(b.studentId);
            return Double.compare(meanB, meanA);
        });




        /*
         * We print our results (top 10 students)
         */
        System.out.println("\nTop 10 students performing significantly better in hard courses:");
        int limit = Math.min(10, betterStudents.size());
        for (int i = 0; i < limit; i++) {
            StudentPerformance sp = betterStudents.get(i);
            System.out.println((i + 1) + ") Student " + sp.studentId + " (Δ = " + sp.diff + ")");
        }
    }

    /**
     * Helper class to store courseId + mean pair
     */
    static class CourseMean {
        int courseId; // ID of course
        double mean; // mean value


        CourseMean(int id, double mean) {
            this.courseId = id; // assign ID
            this.mean = mean; // assign mean
        }
    }


    /**
     * Helper class to store student performance difference
     */
    static class StudentPerformance {
        int studentId; //student id
        double diff; //performance difference


        StudentPerformance(int id, double d) {
            this.studentId = id; // id assignment
            this.diff = d;        //difference assignment
        }
    }
}