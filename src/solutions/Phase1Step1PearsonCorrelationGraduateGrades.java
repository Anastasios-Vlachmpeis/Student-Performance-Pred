package solutions;
import datamodels.GraduateGradesModel;
import java.util.Arrays;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;
public class Phase1Step1PearsonCorrelationGraduateGrades {

    //code testing
    public static void main(String[] args) {
        printTopKCorrelatedCoursePairs(10);
    }

    /**
     * A helper class to store pairs of courses and their correlation r
     * for Q3 "Are there courses that seem similar or related?
     */
    static class CoursePairCorrelation {
        int courseA;
        int courseB;
        double r;

        CoursePairCorrelation(int a, int b, double r) {
            this.courseA = a;
            this.courseB = b;
            this.r = r;
        }
    }

    /*
     * Methods to compute standard deviation and pearson correlation
     */
    /**
     * Sample standard deviation of a course’s grades
     */
    static double courseStd(int courseId, double mean) {
        double sumSq = 0.0;
        double[] courseGrades = GraduateGradesModel.getAllGradesCourse(courseId);
        int n = courseGrades.length;
        for (int s = 0; s < n; s++) {
            //Subtract the course mean from each student's grade
            double diff = courseGrades[s] - mean;
            //Add the square of the deviation to the sum
            sumSq += diff * diff;
        }
        //Can’t compute standard deviation with 0 or 1 data point
        if (n <= 1) return 0.0;
        return Math.sqrt(sumSq / (n - 1));
    }
    /**
     * Pearson correlation correlation between two course columns i and j
     */
    static double pearsonBetweenCourses(int i, double meanI, double stdI,
                                        int j, double meanJ, double stdJ) {
        double[] courseIGrades = GraduateGradesModel.getAllGradesCourse(i);
        double[] courseJGrades = GraduateGradesModel.getAllGradesCourse(j);
        int n = courseIGrades.length; //Get total number of students
        if (n <= 1) return Double.NaN; //Stop if there is less than 2
        if (stdI == 0.0 || stdJ == 0.0) return Double.NaN;

        double covSum = 0.0;
        for (int s = 0; s < n; s++) {
            /**
             * measure how 2 courses move together by getting the sum of
             * the multiplication of the difference between the student's
             * grades and the course means
             */
            covSum += (courseIGrades[s] - meanI) * (courseJGrades[s] - meanJ);
        }

        //Compute the sample covariance
        double cov = covSum / (n - 1);
        //Divide the covariance by the product of the standard deviations
        return cov / (stdI * stdJ);
    }
    /**
     * Build all pair correlations between courses
     */
    static CoursePairCorrelation[] computeAllCourseCorrelations() {
        String[] courses = GraduateGradesModel.getCourses();
        final int C = courses.length; //should be 36
        double[] means = new double[C];
        double[] stds  = new double[C];

        //Store per-course stats for each course
        for (int c = 0; c < C; c++) {
            means[c] = Phase1Step1CourseMean.calcCourseMean(c);
            stds[c]  = courseStd(c, means[c]);
        }

        //Create array of pairs to store all course-to-course correlations
        CoursePairCorrelation[] pairs = new CoursePairCorrelation[(C * (C - 1)) / 2];
        int idx = 0;

        //Go through every unique pair
        for (int i = 0; i < C; i++) {
            for (int j = i + 1; j < C; j++) {
                //Call method to measure how similar the two courses are
                double r = pearsonBetweenCourses(i, means[i], stds[i], j, means[j], stds[j]);
                //Build a list of all correlations
                pairs[idx++] = new CoursePairCorrelation(i, j, r);
            }
        }
        //Return list
        return pairs;
    }
    /**
     * Print the top-k(10) most similar course pairs
     */
    public static void printTopKCorrelatedCoursePairs(int k) {
        CoursePairCorrelation[] pairs = computeAllCourseCorrelations();

        // Keep only r > 0 (positive correlations)
        pairs = Arrays.stream(pairs)
                .filter(p -> !Double.isNaN(p.r) && p.r > 0)
                .toArray(CoursePairCorrelation[]::new);

        //Sort by descending r value
        Arrays.sort(pairs, (a, b) -> Double.compare(b.r, a.r));

        int limit = Math.min(k, pairs.length);
        System.out.println("\nTop " + limit + " most similar course pairs:");
        for (int t = 0; t < limit; t++) {
            CoursePairCorrelation p = pairs[t];
            String nameA = GraduateGradesModel.getCourseName(p.courseA);
            String nameB = GraduateGradesModel.getCourseName(p.courseB);

            System.out.println((t + 1) + ") " + nameA + " and " + nameB + " have correlation r = " + p.r);
        }
    }
}

