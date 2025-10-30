package datamodels;

import java.io.File;
import java.util.*;

/**
 * DataModel class for the graduate grades dataset. Currently it is populated with
 * methods that perform analysis on its data,created in early phases of the development.
 * These methods will go to their designated classes, but till then beside them this class
 * implements the following methods that makes it a DataModel class:
 *      - getGrade(int StudentId, int courseId)
 *      - getAllGradesStudent(int StudentId)
 *      - getAllGradesCourse(int courseId)
 * Also, it must have a hashmaps that maps the global student ids to the local indexing. (not yet for course Ids)
 * All variables that are not final are private!
 */
public class GraduateGradesModel {
    /* This class was specifically made for the "GraduateGrades.csv" file.
     * Originally was a FileDisplayer provided by UM, but it got modified to specialize on previously mentioned .csv
     *
     * Changes will be needed so it can support the other .csv files we got for the project
     * that might include missing values and other data types.
     */

    final static String pathToCSV = "src/datamodels/GraduateGrades.csv";
    public final static int studentCount = 21243;
    public final static int courseCount = 36;


    // Contains name of the courses. courseID is equivalent to index in the array
    private static String[] courses = new String[courseCount];
    // Internal representation of student's grades as a table (2D array)
    // First index corresponds to studentID
    // Second index correspond to the courseID
    // Their combination tells a given student's grade at a given course
    private static double[][] grades = new double[studentCount][courseCount];
    // links student id to its index in grades[][]
    private static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);

    // ensure .csv is loaded before DataModel class is accessed
    static {loadCSV();}
    private static void loadCSV() {
        try {
            Locale.setDefault(Locale.US);
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
            while (lineScanner.hasNext() && courseCounter < courseCount) {
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
            while (fileScanner.hasNextLine() && linesDone < studentCount) {
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
                        // Save global student id so internal indexing is independent of that.
                        int studentId = lineScanner.nextInt();
                        studentID2index.put(studentId, studentCounter);
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


    //====================//
    // DATA MODEL METHODS //
    //====================//
    /**
     * Gives the list of the names of all courses.
     * @return array of the course names where index is the course's id.
     */
    public static String[] getCourses() {return courses;}

    /**
     * Gives all grades of a graduate student.
     * @param studentId global id of the student
     * @return array containing all grades of the student where index equals to course's id
     */
    public static double[] getAllGradesStudent(int studentId) {
        double[] studentGrades = new double[courseCount];
        // has to convert global student id into local representation
        int studentIndex = studentID2index.get(studentId);
        for (int i = 0; i < courseCount; i++) {
            studentGrades[i] = grades[studentIndex][i];
        }
        return studentGrades;
    }

    /**
     * Gives all grades of a graduate student.
     * @param courseId global id of the course
     * @return array containing all grades of the course (id of the students are not preserved)
     */
    public static double[] getAllGradesCourse(int courseId) {
        double[] courseGrades = new double[studentCount];
        // has to convert global student id into local representation
        int courseIndex = courseId; // course id and course index is the same
        for (int i = 0; i < courseCount; i++) {
            courseGrades[i] = grades[i][courseIndex];
        }
        return courseGrades;
    }

    /**
     * Gives all students' id that are contained in this DataModel class
     * @return an array containing all the ids of the students found in this dataset
     */
    public static int[] getAllStudentIds() {
        int[] studentIds = new int[studentCount];
        int i = 0;
        for (int studentId : studentID2index.keySet()) {
            studentIds[i++] = studentId;
        }
        return studentIds;
    }


    //=================================================//
    // LEFTOVER METHODS FROM PHASE 1                   //
    // SOON TO BE PLACED INTO THEIR RESPECTIVE CLASSES //
    //=================================================//
    public static double calcStudentMean(int studentId) {
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
    public static double calcStudentMode(int studentId) {
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
    public static double calcStudentMedian(int studentId) {
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

    public static double calcCourseMean(int courseId) {
        // Calculates mean of grades of a specific course
        double sum = 0;
        for (int studentId = 0; studentId < grades.length; studentId++) {
            sum += grades[studentId][courseId];
        }
        return sum / grades.length;

    }
    public static double calcCourseMedian(int courseId) {
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
    public static double calcCourseMode(int courseId) {
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
            means[c] = calcCourseMean(c);
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
            String nameA = (p.courseA >= 0 && p.courseA < courses.length) ? courses[p.courseA] : ("Course " + p.courseA);
            String nameB = (p.courseB >= 0 && p.courseB < courses.length) ? courses[p.courseB] : ("Course " + p.courseB);

            System.out.println((t + 1) + ") " + nameA + " and " + nameB + " have correlation r = " + p.r);
        }
    }

    public static void printBestAndWorstCourse() {
        //Set initials for loop to work
        double bestMean = 0.0;
        double worstMean = 10.0;
        int bestCourseId = -1;
        int worstCourseId = -1;

        //Go through the means for every course
        for (int i = 0; i < courses.length; i++) {
            double mean = calcCourseMean(i);

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
        System.out.println("Most difficult course: " + courses[bestCourseId] + " (mean grade = " + bestMean + ")");
        System.out.println("Easiest course: " + courses[worstCourseId] + " (mean grade = " + worstMean + ")");
    }

    public static void printCumLaudeStudents() {

        double sumCumLaud = 0;
        System.out.println("\nThe students graduated cum-laude (above 8 mean grade):");
        for (int i = 0; i < grades.length; i++) {
            if (calcStudentMean(i) > 8) {
                System.out.println("Student ID: " +i + " (mean grade = " + String.format("%.2f", calcStudentMean(i)) + ")");
                sumCumLaud++;
            }


        }
        double percentCumLaud = sumCumLaud / grades.length * 100;
        System.out.println("Total Cum Laude Students : " + (int)sumCumLaud);
        System.out.println("Percentage of Cum Laude Students relative to total graduates : " + String.format("%.2f", percentCumLaud) + "%");


    }

    /**
     * Q4: Which students perform significantly better in hard courses compared to easy ones?
     *
     * We analyze course difficulty and student performance, then we find the 5 easiest and 5 hardest
     * courses based on their mean grade, and we identify the top 10 students who performed
     * significantly better in hard courses, compared to their performance in the easy ones.
     */
    public static void analyzeStudentPerformanceHardVsEasy() {


        final int C = courses.length; //total number of courses
        final int S = grades.length; //total number of students


        /*
         * We compute the mean for all courses
         */
        double[] means = new double[C];
        for (int c = 0; c < C; c++) {
            means[c] = calcCourseMean(c);  //compute course mean
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
            System.out.println((i + 1) + ") " + courses[hardest[i]] +
                    " (mean = " + courseMeans[i].mean + ")");
        }


        // Print easiest courses
        System.out.println("\nEasiest 5 courses :");
        for (int i = 0; i < 5; i++) {
            System.out.println((i + 1) + ") " + courses[easiest[i]] +
                    " (mean = " + courseMeans[C - 1 - i].mean + ")");
        }


        /*
         * We compute the average difference in hard and easy courses, for each student
         */
        StudentPerformance[] studentResults = new StudentPerformance[S]; // store student performance


        for (int s = 0; s < S; s++) {
            double hardSum = 0.0; //sum of differences - hard courses
            double easySum = 0.0; //sum of differences - easy courses


            // Loop through selected courses
            for (int i = 0; i < 5; i++) {
                double gradeHard = grades[s][hardest[i]]; //student grade - hard courses
                double gradeEasy = grades[s][easiest[i]]; //student grade - easy courses


                hardSum += (gradeHard - means[hardest[i]]); //diff from mean - hard courses
                easySum += (gradeEasy - means[easiest[i]]); //diff from mean - easy courses
            }


            double hardAvg = hardSum / 5; //avg diff - hard courses
            double easyAvg = easySum / 5; //avg diff - easy courses
            double diff = hardAvg - easyAvg; //compare averages


            studentResults[s] = new StudentPerformance(s, diff); // store Δ for each student
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
            double meanA = calcStudentMean(a.studentId);
            double meanB = calcStudentMean(b.studentId);
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

