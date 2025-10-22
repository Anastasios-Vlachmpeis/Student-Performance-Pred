import java.io.File;
import java.util.Scanner;
import java.util.Arrays;

public class GraduateGradesModel {
    /* This class was specifically made for the "GraduateGrades.csv" file.
     * Originally was a FileDisplayer provided by UM, but it got modified to specialize on previously mentioned .csv
     *
     * Changes will be needed so it can support the other .csv files we got for the project
     * that might include missing values and other data types.
     */

    final static String pathToCSV = "src/GraduateGrades.csv";


    // Contains name of the courses. courseID is equivalent to index in the array
    static String[] courses = new String[36];
    // Internal representation of student's grades as a table (2D array)
    // First index corresponds to studentID
    // Second index correspond to the courseID
    // Their combination tells a given student's grade at a given course
    static double[][] grades = new double[21243][36];

    public static void oldCode() {

		//==============================//
		// POST reading: Put code below //
		//==============================//

		// Example: Printing the grade of studentID 42 at Evolutionary Dynamics (courseID 1)
		System.out.println("The grade of student with ID 42 at Evolutionary Dynamics is " + grades[42][1]);
		System.out.println("The course with courseID 25 is " + courses[25]);

    }

    public static void loadCSV() {
        try {
            // Adapt this when you want to read and display a different file.
            System.out.println("Start reading file: " + pathToCSV);  // Debug
            System.out.println("This will take a while...");        // Debug

            File file=new File(pathToCSV);

            // This code uses two Scanners, one which scans the file line per line
            Scanner fileScanner = new Scanner(file);
            int linesDone = 0;

            // and one that scans the line entry per entry using the commas as delimiters
            Scanner lineScanner = new Scanner(fileScanner.nextLine());
            lineScanner.useDelimiter(",");

            // Since first line of GraduateGrades.csv is only the courses, the code process it separately
            // It is stored in the internal representation for course names where courseID is the course's respective index in the array
            int courseCounter = 0;
            while (lineScanner.hasNext() && courseCounter < 36) {
                String s = lineScanner.next();
                // The entry "StudentID" is a placholder so it is skipped
                if (!s.equals("StudentID")) {
                    courses[courseCounter] = s;
                    courseCounter++;
                }
            }
            linesDone++;

            // Then, the code processes students line by line and load their grades into
            // grades that is a 2D array and the internal representation of the grade table.
            int studentCounter = 0;
            while (fileScanner.hasNextLine() && linesDone < 212245) {
                // Every line now starts with the student id, but that will be omitted.
                // This is because the first index in the 2D array serves as the

                // The second scanner is reused
                lineScanner = new Scanner(fileScanner.nextLine());
                lineScanner.useDelimiter(",");

                courseCounter = 0;

                while (lineScanner.hasNext()) {
                    // Separate entries based on dataype. integer is ID, double is grade.
                    // THERE SHOULD BE NO OTHER DATA TYPES
                    if (lineScanner.hasNextInt()) {
                        // Do nothing since studentID is ignored
                        lineScanner.next();
                    } else if (lineScanner.hasNextDouble()) {
                        double grade = lineScanner.nextDouble();
                        grades[studentCounter][courseCounter] = grade;
                        courseCounter++;
                    } else {
                        System.out.println("something very strange happened the next string given by the scanner is: " + lineScanner.next());   // DEBUG
                    }
                }

                studentCounter++;
                linesDone++;
                lineScanner.close();
            }

            // Prevent memory leaks by closing fileScanner
            fileScanner.close();
            System.out.println("Finished reading: " + pathToCSV);    // DEBUG

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static double getStudentMean(int studentId) {
        // Calculates average of grades for a specific student
        double[] studentGrades = grades[studentId];
        double sum = 0;

        // Sum all grades of the student
        for (int i = 0; i < studentGrades.length; i++) {
            double grade = studentGrades[i];
            sum += grade;
        }

        // Divide total sum by number of grades to get mean
        double mean = sum / studentGrades.length;

        return mean;
    }


    public static double getStudentMode(int studentId) {
        // Calculates the most frequent for a specific student
        double[] studentGrades = grades[studentId];
        double mode = studentGrades[0];
        int maxCount = 0;

        // Compare each grade with every other grade and increase the count by one for every similar grade
        for (int i = 0; i < studentGrades.length; i++) {
            double current = studentGrades[i];
            int count = 0;

            for (int j = 0; j < studentGrades.length; j++) {
                if (studentGrades[j] == current) {
                    count++;
                }
            }

            // Update mode if this grade appears more
            if (count > maxCount) {
                maxCount = count;
                mode = current;
            }
        }
        return mode;
    }


    public static double getStudentMedian(int studentId) {
        // Calculates middle value of grades for a specific student
        double[] studentGrades = grades[studentId].clone();

        // Sort the array from the smallest number to the largest one
        Arrays.sort(studentGrades);

        double median;

        if (studentGrades.length % 2 == 1) {
            // If odd number of grades, take the middle one
            median = studentGrades[studentGrades.length / 2];
        } else {
            // If even, average of the two middle grades
            double middleLeft, middleRight;
            middleLeft = studentGrades[(studentGrades.length / 2) - 1];
            middleRight = studentGrades[studentGrades.length / 2];
            median = (middleLeft + middleRight) / 2.0;
        }

        return median;
    }

    public static double getCourseMean(int courseId) {
        // Calculates mean of grades of a specific course
        double sum = 0;
        for (int studentId = 0; studentId < grades.length; studentId++) {
            sum += grades[studentId][courseId];
        }
        return sum / grades.length;

    }

    public static double getCourseMedian(int courseId) {
        // Calculate median of grades of a specific course

        // Since grades stores arrays of grades of student's. The array of grades of courses is vertical
        // Let's reconstruct it locally in order to sort, which is necessary for getting the median
        double[] gradesByCourse = new double[grades.length];    // one course the same number of grades as number of students
        for (int studentId = 0; studentId < grades.length; studentId++) {
            gradesByCourse[studentId] = grades[studentId][courseId];
        }
        // Now that we have a local copy sorting it does not mess up grades 2D array
        Arrays.sort(gradesByCourse);

        // Now finding the median branches based on parity of the number of elements
        double median;
        if (gradesByCourse.length % 2 == 1) {
            // When odd, it is exactly the middle element
            median = gradesByCourse[gradesByCourse.length / 2];
        } else {
            // Average of the middle two value
            double middleLeft, middleRight;
            middleRight = gradesByCourse[gradesByCourse.length / 2];
            middleLeft = gradesByCourse[(gradesByCourse.length / 2) - 1];
            median = (middleLeft + middleRight) / 2.0;
        }

        return median;
    }

    public static double getCourseMode(int courseId) {
        // Calculate mode of grades of a specific course
        double mode;
        // Count the frequencies of all grades (6.0, 7.0, 8.0, 9.0, 10.0)
        // index 0 is 6.0, index 1 is 7.0, etc.
        // So we need to subtract 6 from the grade to get its corresponding index in the
        int[] gradeFrequencies = new int[5];
        for (int studentId = 0; studentId < grades.length; studentId++) {
            gradeFrequencies[(int)grades[studentId][courseId] - 6] += 1;
        }

        // Searches highest frequency
        int indexHighest = 0;
        for (int i = 0; i < gradeFrequencies.length; i++) {
            if (gradeFrequencies[indexHighest] < gradeFrequencies[i]) {
                indexHighest = i;
            }
        }
        mode = indexHighest + 6;    // automatically cast as double

        return mode;
    }

    /**
     * Q3: "Are there courses that seem similar or related?"
     * We compute Pearson correlation between courses, and
     * display the positively correlated course pairs - these are the
     * most "similar" courses
     */

    /**
     * A helper class to store pairs of courses and their correlation r
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

    /**
     * Methods to compute standard deviation and pearson correlation
     */

    /**
     * Sample standard deviation of a course’s grades
     */
    static double courseStd(int courseId, double mean) {
        double sumSq = 0.0;
        int n = grades.length;
        for (int s = 0; s < n; s++) {
            //Subtract the course mean from each student’s grade
            double diff = grades[s][courseId] - mean;
            //Add the square of the deviation to the sum
            sumSq += diff * diff;
        }
        //Can’t compute standard deviation with 0 or 1 data point
        if (n <= 1) return 0.0;
        return Math.sqrt(sumSq / (n - 1));
    }


    /**
     * Pearson correlation between two course columns i and j
     */
    static double pearsonBetweenCourses(int i, double meanI, double stdI,
                                        int j, double meanJ, double stdJ) {
        int n = grades.length; //Get total number of students
        if (n <= 1) return Double.NaN; //Stop if there is less than 2
        if (stdI == 0.0 || stdJ == 0.0) return Double.NaN;

        double covSum = 0.0;
        for (int s = 0; s < n; s++) {
            /**
             * measure how 2 courses move together by getting the sum of
             * the multiplication of the difference between the student's
             * grades and the course means
             */
            covSum += (grades[s][i] - meanI) * (grades[s][j] - meanJ);
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
        final int C = courses.length; //should be 36
        double[] means = new double[C];
        double[] stds  = new double[C];

        //Store per-course stats for each course
        for (int c = 0; c < C; c++) {
            means[c] = getCourseMean(c);
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
    static void printTopKCorrelatedCoursePairs(int k) {
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
            String nameA = (p.courseA >= 0 && p.courseA < courses.length) ? courses[p.courseA] : ("Course " + p.courseA);
            String nameB = (p.courseB >= 0 && p.courseB < courses.length) ? courses[p.courseB] : ("Course " + p.courseB);

            System.out.println((t + 1) + ") " + nameA + " and " + nameB + " have correlation r = " + p.r);
        }
    }
}

