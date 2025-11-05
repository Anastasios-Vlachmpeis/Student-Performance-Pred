package solutions;
import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

public class Phase1Step1HardestEasiestCourses {

    //code testing
    public static void main(String[] args) {
        printBestAndWorstCourse();
    }

    public static void printBestAndWorstCourse() {
        //Set initials for loop to work
        double bestMean = 0.0;
        double worstMean = 10.0;
        int bestCourseId = -1;
        int worstCourseId = -1;

        //Go through the means for every course
        String[] courses = GraduateGradesModel.getCourses();
        for (int i = 0; i < courses.length; i++) {
            double mean = Phase1Step1CourseMean.calcCourseMean(i);

            //Detect and update the most difficult and easiest courses on every step of the loop
            if (mean > bestMean) {
                bestMean = mean;
                bestCourseId = i;
            }
            if (mean < worstMean) {
                worstMean = mean;
                worstCourseId = i;
            }
        }

        //Print the most difficult and the easiest course with their means
        System.out.println("\nMost difficult and easiest courses:");
        System.out.println("Most difficult course: " + GraduateGradesModel.getCourseName(bestCourseId) + " (mean grade = " + bestMean + ")");
        System.out.println("Easiest course: " + GraduateGradesModel.getCourseName(worstCourseId) + " (mean grade = " + worstMean + ")");
    }
}
