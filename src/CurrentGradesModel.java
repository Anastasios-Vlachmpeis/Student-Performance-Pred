import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/** Reads CurrentGrades.csv, stores it internally, computes statistics
    on itself via public methods
    As of now it is a static class, and meant to be run on its own.
*/
public class CurrentGradesModel {
    final static String pathToCSV = "src/CurrentGrades.csv";
    final static int studentCount = 1522 - 1;
    final static int courseCount = 36;


    // stores the grade table of all students for all courses
    // NG is encoded as -1
    static double[][] grades = new double[studentCount][courseCount];
    static String[] courses = new String[courseCount];
    // links student id to its index in grades[][]
    static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);


    public static void loadCSV() {

        try {
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

    /** Calculates mean of the grades of a student based on student id. Ignores No Grades.*/
    public static double getStudentMean(int studentId){
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

        // warning message when analysing data. so everyone getting zero is not identical to everyone having NG
        if (gradecounter == 0) {System.out.println("no grades for all courses for student: " + studentId);}

        return sum / (double)gradecounter;
    }

    /** Calculates median of the grades of a student based on student id. Ignores No Grades.*/
    public static double getStudentMedian(int studentId){
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

        // warning message when analysing data. so everyone getting zero is not identical to everyone having NG
        if (studentGrades.isEmpty()) {System.out.println("No grades for all courses for student: " + studentId);}

        // sorting to find median (middle value)
        studentGrades.sort(null);

        // median is defined depending on the parity of the length of the dataset
        double median;
        if (studentGrades.size() % 2 == 1) {
            median = studentGrades.get(studentGrades.size() / 2);
        } else {
            int middleLeft = studentGrades.size() / 2;
            int middleRight = (studentGrades.size() / 2) + 1;
            median = (studentGrades.get(middleLeft) + studentGrades.get(middleRight)) / 2.0;
        }

        return median;
    }

    /** Calculates mode of the grades of a student based on student id. Ignores No Grades.*/
    public static double getStudentMode(int studentId){
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

        // warning message when analysing data. no grades at all is an edge case we did not prepare for yet.
        if (indexMostFrequent == 0 && gradeFrequency[0] == 0) {
            System.out.println("no grades for all courses for student: " + studentId);
            return -1;
        }

        return indexMostFrequent;
    }


    /** Calculates mean of the grades of a course based on course id. Ignores No Grades.*/
    public static double getCourseMean(int courseId){
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

        // warning message when analysing data. so everyone getting zero is not identical to everyone having NG
        if (gradecounter == 0) {
            System.out.println("No grades for all student for course id: " + courseId);
            return -1;
        }

        return sum / (double)gradecounter;
    }

    /** Calculates median of the grades of a course based on course id. Ignores No Grades.*/
    public static double getCourseMedian(int courseId){
        // collect non no grade grades of the course
        ArrayList<Double> courseGrades = new ArrayList<>();
        for (int i = 0; i < grades.length; i++) {   // grades.length should be the same as studentCount
            double grade = grades[i][courseId];
            // ignore NGs (No grades)
            if (grade == -1) {continue;}
            courseGrades.add(grade);
        }

        // warning message when analysing data. no grades at all is an edge case we did not prepare for yet.
        if (courseGrades.isEmpty()) {
            System.out.println("WARNING: No grades for all students for course id: " + courseId);
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
    public static double getCourseMode(int courseId){
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

        // warning message when analysing data. no grades at all is an edge case we did not prepare for yet.
        if (indexMostFrequent == 0 && gradeFrequency[0] == 0) {
            System.out.println("WARNING: No grades for all students for course id: " + courseId);
        }

        return indexMostFrequent;
    }

    public static void printStudentNGcount() {
        //Prints the number of NG per student
        //This may be used to understand which students are their last year, by looking at their number of NG's
        int count = 0;
        for (int i = 0; i < grades.length - 1; i++) {

            for (int j = 0; j < grades[i].length; j++) {
                if (grades[i][j] == -1) {
                    count++;
                }
            }

            System.out.println("Number of NG for the student " + i +" : " + count);
            count = 0;
        }

    }

    public static void printCourseNGcount() {
        //Print the number of NG per course
        //This is to assume the order of taking the courses
        int count = 0;
        for (int i = 0; i < grades[0].length; i++) {

            for (int j = 0; j < grades.length; j++) {
                if (grades[j][i] == -1) {
                    count++;
                }
            }

            System.out.println("Number of NG for the course " +i + " : " + count);
            count = 0;
        }

    }

    public static void printFailedCourses() {
        //Prints the number of failed courses per student
        int count = 0;
        for (int i = 0; i < grades.length - 1; i++) {
            for (int j = 0; j < grades[i].length; j++) {
                //Doesn't take NG into consideration since we assume that they are not taken yet, hence not failed
                if (grades[i][j] < 6.0 && grades[i][j] != -1) {
                    count++;
                }
            }
            System.out.println("Student: " + i + " has failed " + count + " courses.");
            count = 0;


        }

    }

    public static double[] getAllGradesOfCourse(int courseId) {
        double[] courseGrades = new double[studentCount];
        for (int i = 0; i < studentCount; i++) {
            courseGrades[i] = grades[i][courseId];
        }
        return courseGrades;
    }

    public static int[] getAllStudentIdsOfCourse(int courseId) {
        int[] studentIds = new int[studentCount];
        int i = 0;
        for (int studentId : studentID2index.keySet()){
            studentIds[i] = studentId;
            i++;
        }
        return studentIds;
    }

    public static double getGrade(int studentId, int courseId) {
        return grades[studentID2index.get(studentId)][courseId];
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
            means[c] = getCourseMean(c);
            medians[c] = getCourseMedian(c);

            int ngCount = 0;
            for (int s = 0; s < studentCount; s++) {
                if (grades[s][c] == -1) ngCount++;
            }
            ngCounts[c] = ngCount;
        }

        //Compute effective score (mean or mean + median if >75% NGs)
        ArrayList<CourseMean> courseMeanList = new ArrayList<>();
        for (int c = 0; c < C; c++) {
            double ngRatio = (double) ngCounts[c] / studentCount;
            double effectiveScore;
            if (ngRatio > 0.75) { //If NGs are more than 75% of the course grades
                if (!Double.isNaN(medians[c])) { //if course mean is not NaN, make the mean be the average of mean + median
                    effectiveScore = (means[c] + medians[c]) / 2.0;
                } else { //else let it be, it will be filtered out later
                    effectiveScore = means[c];
                }
            } else {
                effectiveScore = means[c];
            }
            //Filter to add only courses with a positive mean to the list
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

    static void printTopKCorrelatedCoursePairsIgnoreNG(int k) {
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

}


