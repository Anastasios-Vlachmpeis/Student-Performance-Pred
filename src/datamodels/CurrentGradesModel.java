package datamodels;

import java.io.File;
import java.util.*;

/**
 * DataModel class for the current grades' dataset. Currently, it is populated with
 * methods that perform analysis on its data,created in early phases of the development.
 * These methods will go to their designated classes, but till then beside them this class
 * implements the following methods that makes it a DataModel class:
 *      - getGrade(int StudentId, int courseId)
 *      - getAllGradesStudent(int StudentId)
 *      - getAllGradesCourse(int courseId)
 *      - getAllValidGradesStudent(int studentId)
 *      - getAllValidGradesCourse(int courseId)
 *      - getAllStudentIds()
 * Also, it must have a hashmaps that maps the global student ids to the local indexing. (not yet for course Ids)
 * All variables that are not final are private!ic class, and meant to be run on its own.
 * @implNote NGs (no grades) are encoded as -1. getAllValidGrades methods only return grades that are not NGs
*/
public class CurrentGradesModel {
    final static String pathToCSV = "src/datamodels/CurrentGrades.csv";
    public final static int studentCount = 1522 - 1;
    public final static int courseCount = 36;


    // stores the grade table of all students for all courses
    // NG is encoded as -1
    private static double[][] grades = new double[studentCount][courseCount];
    public static String[] courses = new String[courseCount];
    // links student id to its index in grades[][]
    private static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);

    // ensure .csv is loaded before DataModel class is accessed
    static {loadCSV();}
    private static void loadCSV() {

        try {
            Locale.setDefault(Locale.US);
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
            while (fileScanner.hasNextLine()) {
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
                        studentID2index.put(lineScanner.nextInt(), studentCounter);
                    } else if (lineScanner.hasNextDouble()) {
                        double grade = lineScanner.nextDouble();
                        grades[studentCounter][courseCounter] = grade;
                        courseCounter++;
                    } else {
                        String misc = lineScanner.next();
                        if (misc.equals("NG")) {
                            grades[studentCounter][courseCounter] = -1;
                            courseCounter++;
                        } else {
                            System.out.println("Unexpected: the next string given by the scanner is: " + misc);
                        }
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
    public static double getGrade(int studentId, int courseId) {
        return grades[studentID2index.get(studentId)][courseId];
    }
    public static double[] getAllGradesStudent(int studentId) {
        double[] studentGrades = new double[courseCount];
        // has to convert global student id into local representation
        int studentIndex = studentID2index.get(studentId);
        for (int i = 0; i < courseCount; i++) {
            studentGrades[i] = grades[studentIndex][i];
        }
        return studentGrades;
    }
    public static double[] getAllGradesCourse(int courseId) {
        double[] courseGrades = new double[studentCount];
        // has to convert global student id into local representation
        int courseIndex = courseId; // course id and course index is the same
        for (int i = 0; i < courseCount; i++) {
            courseGrades[i] = grades[i][courseIndex];
        }
        return courseGrades;
    }
    public static ArrayList<Double> getAllValidGradesStudent(int studentId) {
        ArrayList<Double> courseGrades = new ArrayList<>();
        // first convert global student id into local student index
        int studentIndex = studentID2index.get(studentId);
        for (int i = 0; i < courseCount; i++) {
            // skip no grades
            if (grades[studentId][i] == -1) {continue;}
            courseGrades.add(grades[studentIndex][i]);
        }
        return courseGrades;
    }
    public static ArrayList<Double> getAllValidGradesCourse(int courseId) {
        ArrayList<Double> courseGrades = new ArrayList<>();
        for (int i = 0; i < studentCount; i++) {
            // skip no grades
            if (grades[i][courseId] == -1) {continue;}
            courseGrades.add(grades[i][courseId]);
        }
        return courseGrades;
    }
    public static int[] getAllStudentIds() {
        int[] studentIds = new int[studentCount];
        int i = 0;
        for (int studentId : studentID2index.keySet()) {
            studentIds[i++] = studentId;
        };
        return studentIds;
    }
    public static ArrayList<Integer> getAllStudentIdsOfCourseWithGrade(int courseId) {
        ArrayList<Integer> studentIds = new ArrayList<>();
        for (int studentId : studentID2index.keySet()){
            // ignore students with NoGrade
            if (grades[studentID2index.get(studentId)][courseId] == -1) {continue;}
            studentIds.add(studentId);
        }
        return studentIds;
    }

    //=================================================//
    // LEFTOVER METHODS FROM PHASE 1                   //
    // SOON TO BE PLACED INTO THEIR RESPECTIVE CLASSES //
    //=================================================//
    /** Calculates mean of the grades of a student based on student id. Ignores No Grades.*/
    public static double calcStudentMean(int studentId){
        // convert global student id into local grades[][] index
        int studentIndex = studentID2index.get(studentId);

        double sum = 0;
        int gradecounter = 0;
        for (int i = 0; i < grades[studentIndex].length; i++){
            double grade = grades[studentIndex][i];
            // ignore NGs (No grades)
            if (grade == -1) {continue;}
            sum += grade;
            gradecounter++;
        }

        // mean is not defined for empty dataset
        if (gradecounter == 0) {return -1;}

        return sum / (double)gradecounter;
    }

    /** Calculates median of the grades of a student based on student id. Ignores No Grades.*/
    public static double calcStudentMedian(int studentId){
        // convert global student id into local grades[][] index
        int studentIndex = studentID2index.get(studentId);

        // collect non no grade grades of the course
        ArrayList<Double> studentGrades = new ArrayList<>();
        for (int i = 0; i < grades[studentIndex].length; i++) {   // grades.length should be the same as studentCount
            double grade = grades[studentIndex][i];
            // ignore NGs (No grades)
            if (grade == -1) {continue;}
            studentGrades.add(grade);
        }

        // median is not defined for empty dataset
        if (studentGrades.isEmpty()) {return -1;}

        // sorting to find median (middle value)
        studentGrades.sort(null);

        // median is defined depending on the parity of the length of the dataset
        double median;
        if (studentGrades.size() % 2 == 1) {
            median = studentGrades.get(studentGrades.size() / 2);
        } else {
            int middleRight = studentGrades.size() / 2;  // Changed from this, as it  -->  int middleLeft = studentGrades.size() / 2;
            int middleLeft = middleRight - 1;            // could read out of bounds       int middleRight = (studentGrades.size() / 2) + 1;
            median = (studentGrades.get(middleLeft) + studentGrades.get(middleRight)) / 2.0;
        }

        return median;
    }

    /** Calculates mode of the grades of a student based on student id. Ignores No Grades.*/
    public static double calcStudentMode(int studentId){
        // convert global student id into local grades[][] index
        int studentIndex = studentID2index.get(studentId);

        // stores frequency (how many times it occurred) of each grade.
        // grade N has the index of N in the array
        int[] gradeFrequency = new int[11]; // allows for 0 grade
        for (int i = 0; i < grades[studentIndex].length; i++){
            double grade = grades[studentIndex][i];
            // ignore NGs (No grades)
            if (grade == -1) {continue;}
            gradeFrequency[(int)grade - 1] += 1;
        }

        // find most frequent lowest grade
        // TODO: ask the group about this
        int indexMostFrequent = 0;
        for (int i = 0; i < gradeFrequency.length; i++) {
            if (gradeFrequency[i] > gradeFrequency[indexMostFrequent]) {
                indexMostFrequent = i;
            }
        }

        // mode is not defined for empty dataset
        if (indexMostFrequent == 0 && gradeFrequency[0] == 0) {
            return -1;
        }

        return indexMostFrequent;
    }


    /** Calculates mean of the grades of a course based on course id. Ignores No Grades.*/
    public static double calcCourseMean(int courseId){
        double grade, sum;
        int gradecounter;

        sum = 0;
        gradecounter = 0;
        for (int i = 0; i < grades.length; i++){
           grade = grades[i][courseId];
           // ignore NGs (No grades)
           if (grade == -1) {continue;}
           sum += grade;
           gradecounter++;
        }

        // mean is not defined for empty dataset
        if (gradecounter == 0) {
            return -1;
        }

        return sum / (double)gradecounter;
    }

    /** Calculates median of the grades of a course based on course id. Ignores No Grades.*/
    public static double calcCourseMedian(int courseId){
        // collect non no grade grades of the course
        ArrayList<Double> courseGrades = new ArrayList<>();
        for (int i = 0; i < grades.length; i++) {   // grades.length should be the same as studentCount
            double grade = grades[i][courseId];
            // ignore NGs (No grades)
            if (grade == -1) {continue;}
            courseGrades.add(grade);
        }

        // median is not defined for empty dataset
        if (courseGrades.isEmpty()) {
            return -1;
        }

        // sorting to find median (middle value)
        courseGrades.sort(null);

        // median is defined depending on the parity of the length of the dataset
        double median;
        if (courseGrades.size() % 2 == 1) {
            median = courseGrades.get(courseGrades.size() / 2);
        } else {
            int middleRight = courseGrades.size() / 2;
            int middleLeft = middleRight - 1;
            median = (courseGrades.get(middleLeft) + courseGrades.get(middleRight)) / 2.0;
        }

        return median;
    }

    /** Calculates mode of the grades of a course based on course id. Ignores No Grades.*/
    public static double calcCourseMode(int courseId){
        // stores frequency (how many times it occurred) of each grade.
        // grade N has the index of N in the array
        int[] gradeFrequency = new int[11]; // allows for 0 grade
        for (int i = 0; i < grades.length; i++){
            double grade = grades[i][courseId];
            // ignore NGs (No grades)
            if (grade == -1) {continue;}
            gradeFrequency[(int)grade - 1] += 1;
        }

        // find most frequent lowest grade
        // TODO: ask the group about this
        int indexMostFrequent = 0;
        for (int i = 0; i < gradeFrequency.length; i++) {
            if (gradeFrequency[i] > gradeFrequency[indexMostFrequent]) {
                indexMostFrequent = i;
            }
        }

        // mode is not defined for empty dataset
        if (indexMostFrequent == 0 && gradeFrequency[0] == 0) {
            return -1;
        }

        return indexMostFrequent;
    }

    public static int getCourseNG(int courseID) {
        //Get the number of NG per course
        //This is to assume the order of taking the courses
        int count = 0;


            for (int j = 0; j < grades.length; j++) {
                if (grades[j][courseID] == -1) {
                    count++;
                }
            }

       return count;
    }

    /**
     * Q1 : Which courses are the most difficult/easy?
     * Prints the 5 hardest and 5 easiest courses based on mean grades.
     * If more than 75% of students have NGs for a course,
     * it uses the average of mean and median instead.
     */
    public static void printHardestAndEasiestCourses() {

        int C = courseCount;  // total number of courses
        double[] means = new double[C];
        double[] medians = new double[C];
        int[] ngCounts = new int[C];

        // Compute mean, median, and NG count for all courses
        for (int c = 0; c < C; c++) {
            means[c] = calcCourseMean(c);
            medians[c] = calcCourseMedian(c);

            int ngCount = 0;
            for (int s = 0; s < studentCount; s++) {
                if (grades[s][c] == -1) ngCount++;
            }
            ngCounts[c] = ngCount;
        }

        // Compute effective score (mean or mean+median if >75% NGs)
        ArrayList<CourseMean> courseMeanList = new ArrayList<>();
        for (int c = 0; c < C; c++) {
            double ngRatio = (double) ngCounts[c] / studentCount;
            double effectiveScore;
            if (ngRatio > 0.75) {
                if (!Double.isNaN(medians[c])) {
                    effectiveScore = (means[c] + medians[c]) / 2.0;
                } else {
                    effectiveScore = means[c];
                }
            } else {
                effectiveScore = means[c];
            }
            if (effectiveScore > 0) {
                courseMeanList.add(new CourseMean(c, effectiveScore));
            }
        }
        CourseMean[] courseMeans = courseMeanList.toArray(new CourseMean[0]);

        // Sort by ascending score (hardest first)
        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean));

        // Only output up to 5, or as many as we have
        int outputCount = Math.min(5, courseMeans.length);

        // Print hardest courses
        System.out.println("\nHardest " + outputCount + " courses for current students:");
        for (int i = 0; i < outputCount; i++) {
            int id = courseMeans[i].courseId;
            System.out.println((i + 1) + ") " + courses[id]
                    + " (mean = " + String.format("%.2f", courseMeans[i].mean) + ")");
        }

        // Print easiest courses
        System.out.println("\nEasiest " + outputCount + " courses for current grades:");
        for (int i = 0; i < outputCount; i++) {
            int idx = courseMeans.length - 1 - i;
            int id = courseMeans[idx].courseId;
            System.out.println((i + 1) + ") " + courses[id]
                    + " (mean = " + String.format("%.2f", courseMeans[idx].mean) + ")");
        }
    }

    //Helper class that stores a course's mean and id
    static class CourseMean {
        int courseId;
        double mean;
        CourseMean(int courseId, double mean) {
            this.courseId = courseId;
            this.mean = mean;
        }
    }

    /**
     * Q3: "Are there courses that seem similar or related?"
     * Computes Pearson correlation between all pairs of courses,
     * but only considers students who have valid (non-NG) grades
     * in both courses. Displays the top positively correlated course
     * pairs as the most "similar" courses, while skipping NG entries.
     */

    // Compute Pearson correlation for a pair of courses, considering only students with grades in both
    static double pearsonBetweenCoursesIgnoreNG(int i, int j) {
        // Gathers pairs of grades only when both courses have a grade for the same student
        ArrayList<Double> gradesA = new ArrayList<>();
        ArrayList<Double> gradesB = new ArrayList<>();
        for (int s = 0; s < grades.length; s++) {
            double gradeA = grades[s][i];
            double gradeB = grades[s][j];
            if (gradeA != -1 && gradeB != -1) {
                gradesA.add(gradeA);
                gradesB.add(gradeB);
            }
        }
        int n = gradesA.size();
        if (n <= 1) return Double.NaN;

        // calculate means
        double sumA = 0, sumB = 0;
        for (int k = 0; k < n; k++) {
            sumA += gradesA.get(k);
            sumB += gradesB.get(k);
        }
        double meanA = sumA / n;
        double meanB = sumB / n;

        // calculate std deviations
        double sumSqA = 0, sumSqB = 0;
        for (int k = 0; k < n; k++) {
            double diffA = gradesA.get(k) - meanA;
            double diffB = gradesB.get(k) - meanB;
            sumSqA += diffA * diffA;
            sumSqB += diffB * diffB;
        }
        double stdA = Math.sqrt(sumSqA / (n - 1));
        double stdB = Math.sqrt(sumSqB / (n - 1));

        // avoid division by zero or missing variability
        if (stdA == 0.0 || stdB == 0.0) return Double.NaN;

        // calculate pearson correlation
        double covSum = 0;
        for (int k = 0; k < n; k++) {
            covSum += (gradesA.get(k) - meanA) * (gradesB.get(k) - meanB);
        }
        double cov = covSum / (n - 1);
        return cov / (stdA * stdB);
    }

    static CoursePairCorrelation[] computeAllCourseCorrelationsIgnoreNG() {
        final int C = courses.length; //should be courseCount
        ArrayList<CoursePairCorrelation> pairList = new ArrayList<>();

        //Go through every unique unordered pair
        for (int i = 0; i < C; i++) {
            for (int j = i + 1; j < C; j++) {
                double r = pearsonBetweenCoursesIgnoreNG(i, j);
                pairList.add(new CoursePairCorrelation(i, j, r));
            }
        }
        // Convert to array for sorting/printing
        return pairList.toArray(new CoursePairCorrelation[0]);
    }

    public static void printTopKCorrelatedCoursePairsIgnoreNG(int k) {
        CoursePairCorrelation[] pairs = computeAllCourseCorrelationsIgnoreNG();

        // Keep only r > 0 (positive correlations)
        pairs = Arrays.stream(pairs)
                .filter(p -> !Double.isNaN(p.r) && p.r > 0)
                .toArray(CoursePairCorrelation[]::new);

        //Sort by descending r value
        Arrays.sort(pairs, (a, b) -> Double.compare(b.r, a.r));

        int limit = Math.min(k, pairs.length);
        System.out.println("\nTop " + limit + " most similar course pairs for current grades:");
        for (int t = 0; t < limit; t++) {
            CoursePairCorrelation p = pairs[t];
            String nameA = (p.courseA >= 0 && p.courseA < courses.length) ? courses[p.courseA] : ("Course " + p.courseA);
            String nameB = (p.courseB >= 0 && p.courseB < courses.length) ? courses[p.courseB] : ("Course " + p.courseB);

            System.out.println((t + 1) + ") " + nameA + " and " + nameB + " have correlation r = " + String.format("%.3f", p.r));
        }
    }

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
     * Q4: Which students perform significantly better in hard courses compared to easy ones?
     *
     * We use the same logic for determining the 5 hardest and 5 easiest courses (based on
     * course mean/median and NG handling). We only consider students who have valid grades
     * for all 5 hardest and 5 easiest courses.
     */
    public static void analyzeStudentPerformanceHardVsEasyNG() {

        final int C = courseCount;  // total number of courses
        final int S = studentCount; // total number of students

        //We compute course means and medians, accounting for NG ratio
        double[] means = new double[C];
        double[] medians = new double[C];
        int[] ngCounts = new int[C];

        for (int c = 0; c < C; c++) {
            means[c] = calcCourseMean(c);
            medians[c] = calcCourseMedian(c);
            for (int s = 0; s < S; s++) {
                if (grades[s][c] == -1) ngCounts[c]++;
            }
        }

        //Adjusted difficulty score for courses with high NG ratios
        ArrayList<CourseMean> courseMeanList = new ArrayList<>();
        for (int c = 0; c < C; c++) {
            double ngRatio = (double) ngCounts[c] / S;
            double effectiveScore = means[c];
            if (ngRatio > 0.75 && !Double.isNaN(medians[c])) {
                effectiveScore = (means[c] + medians[c]) / 2.0;
            }
            if (effectiveScore > 0) {
                courseMeanList.add(new CourseMean(c, effectiveScore));
            }
        }

        CourseMean[] courseMeans = courseMeanList.toArray(new CourseMean[0]);
        Arrays.sort(courseMeans, (a, b) -> Double.compare(a.mean, b.mean)); // ascending (hardest first)

        int hardCount = Math.min(5, courseMeans.length / 2);
        int easyCount = Math.min(5, courseMeans.length / 2);

        int[] hardest = new int[hardCount];
        int[] easiest = new int[easyCount];
        for (int i = 0; i < hardCount; i++) hardest[i] = courseMeans[i].courseId;
        for (int i = 0; i < easyCount; i++) easiest[i] = courseMeans[courseMeans.length - 1 - i].courseId;

        //We evaluate each student's relative performance in hard vs easy courses
        ArrayList<StudentPerformanceNG> studentResults = new ArrayList<>();

        for (int s = 0; s < S; s++) {
            double hardSum = 0.0, easySum = 0.0;
            int hardCountValid = 0, easyCountValid = 0;

            //Check hard courses
            for (int i = 0; i < hardCount; i++) {
                double grade = grades[s][hardest[i]];
                if (grade != -1) {
                    hardSum += (grade - means[hardest[i]]);
                    hardCountValid++;
                }
            }

            //check easy courses
            for (int i = 0; i < easyCount; i++) {
                double grade = grades[s][easiest[i]];
                if (grade != -1) {
                    easySum += (grade - means[easiest[i]]);
                    easyCountValid++;
                }
            }

            //We only include students who have grades for all 5 hardest & 5 easiest
            if (hardCountValid < hardCount || easyCountValid < easyCount) {
                continue;
            }

            double hardAvg = hardSum / hardCount;
            double easyAvg = easySum / easyCount;
            double diff = hardAvg - easyAvg;

            /**
             * We convert the internal index s to the actual StudentID to prevent NullPointerException.
             */
            int realStudentId = -1;
            for (var entry : studentID2index.entrySet()) {
                if (entry.getValue() == s) {
                    realStudentId = entry.getKey();
                    break;
                }
            }
            studentResults.add(new StudentPerformanceNG(realStudentId, diff));
        }

        /**
         * Filtering and sorting of students who perform significantly better in hard courses
         */
        List<StudentPerformanceNG> betterStudents = new ArrayList<>();
        for (StudentPerformanceNG sp : studentResults) {
            if (sp.diff > 1) betterStudents.add(sp); // keep only those above threshold
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

        /**
         * Print results (top 10)
         */
        System.out.println("\nTop students performing significantly better in hard courses (Δ > 1.2):");
        int limit = Math.min(10, betterStudents.size());
        for (int i = 0; i < limit; i++) {
            StudentPerformanceNG sp = betterStudents.get(i);
            System.out.println((i + 1) + ") Student " + sp.studentId +
                    " (Δ = " + String.format("%.2f", sp.diff) +
                    ", Mean = " + String.format("%.2f", calcStudentMean(sp.studentId)) + ")");
        }


        if (studentResults.isEmpty()) {
            System.out.println("No students have complete grades in all 10 courses.");
        }
    }

    /** Helper class for course averages (reused) */
    static class CourseMeanNG {
        int courseId;
        double mean;

        CourseMeanNG(int courseId, double mean) {
            this.courseId = courseId;
            this.mean = mean;
        }
    }

    /** Helper class for storing student performance difference */
    static class StudentPerformanceNG {
        int studentId;
        double diff;

        StudentPerformanceNG(int studentId, double diff) {
            this.studentId = studentId;
            this.diff = diff;
        }
    }


    /**Calculates the mean of the mean of the courses. Ignoring NGs and ignoring courses that have only NGs*/
    public static double getCourseMeansMean () {
        double sumMean = 0;     // sum of the means
        int counterMean = 0;    // number of means summed
        for (int i = 0; i < courseCount; i++){
            double mean = calcCourseMean(i);
            if (mean == -1) {continue;}
            sumMean += mean;
            counterMean += 1;
        }
        return sumMean / counterMean;   // division by zero if and only if all courses have only NGs
    }

    public static int getStudentNGCount(int studentId) {
        //This method gives the number of NG for the given student
        int count = 0;

        if (studentId < 0 || studentId >= grades.length) {
            System.out.println("Invalid student ID.");
            return -1;
        }

        for (int i = 0; i < grades[studentId].length; i++) {
            if (grades[studentId][i] == -1) {
                count++;
            }
        }

        return count;
    }


    public static int getFailedCourses(int studentId) {
        //This method is used to get the number of failed courses for the given student.
        int count = 0;

        if (studentId < 0 || studentId >= grades.length) {
            System.out.println("Invalid studentID");
            return -1;
        }

        for (int i = 0; i < grades[studentId].length; i++) {
            if (grades[studentId][i] < 6.0 && grades[studentId][i] != -1) {
                count++;
            }
        }

        return count;
    }


    public static void getGraduatingStudents() {
        //Q2
        //This method finds the students that are close to graduating by checking if they have any failed courses and their number of NG.
        int count = 0;
        for (int i = 0; i < grades.length; i++) {
            int fails = getFailedCourses(i);
            int ngs = getStudentNGCount(i);

            if (fails == 0 && ngs < 5) {
                // System.out.println("Possible graduation of the student: " + i);  // DEBUG
                count++;
            }
        }
        System.out.println("Number of expected students to graduate this year: " + count);


    }

    public static double getPassingRate(int courseID) {
        //This gives us the passing rate for the given course id
        //The method divides the number of passing values to the number of values that are not NG
        if (courseID < 0 || courseID >= grades.length) {
            System.out.println("Invalid courseID");
            return -1;
        }
        int numberOfAllGrades = 0;
        double numberOfPassingGrades = 0;
        for (int i = 0; i < grades.length; i++) {
            if (grades[i][courseID] != -1) {
                numberOfAllGrades++;
                if (grades[i][courseID] >= 6) {
                    numberOfPassingGrades++;
                }

            }


        }

        return numberOfPassingGrades / numberOfAllGrades;

    }

    public static double passingCorrelationValue() {
        //We find the passing correlation value by dividing passing rate of the courses to the course mean from graduate grades.
        //This value is used to determine passing rate of the courses without any data.
        double correlationValue = 0;
        for (int i = 0; i < grades[0].length; i++) {
            correlationValue = correlationValue + getPassingRate(i)/ GraduateGradesModel.calcCourseMean(i);
        }
        return correlationValue/36;
    }



    public static double meanPassingRate() {

        //Calculates mean passing rate of the courses based on the correlation value and course mean data from graduate grades.
        double sum = 0;
        for (int i = 0; i < grades[0].length; i++) {
            sum = sum + passingCorrelationValue()* GraduateGradesModel.calcCourseMean(i);
        }
        return sum / grades[0].length;
    }


}


