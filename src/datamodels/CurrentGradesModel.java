package datamodels;

import solutions.Phase1Step1CourseMean;

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
    private static String[] courses = new String[courseCount];
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
        Integer studentIndex = studentID2index.get(studentId);
        // Null check implementation prevents NullPointerException in case an invalid studentId is passed
        if (studentIndex == null) {
            throw new IllegalArgumentException("Student ID " + studentId + " not found");
        }
        return grades[studentIndex][courseId];
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
        for (int i = 0; i < studentCount; i++) {
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

    /**
     * Gives the list of the names of all courses.
     * @return array of the course names where index is the course's id.
     */
    public static String[] getCourses() {
        return courses;
    }

    /**
     * Gives the name of the course assigned to an id.
     * @param courseId valid course id (0-35)
     * @return name of the course
     */
    public static String getCourseName(int courseId) {
        return courses[courseId];
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

    // moved Q1 printing method to solutions.Phase1Step2HardestEasiestCourses

    //Helper class that stores a course's mean and id
     static class CourseMean {
        int courseId;
        double mean;
        CourseMean(int courseId, double mean) {
            this.courseId = courseId;
            this.mean = mean;
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
            correlationValue = correlationValue + getPassingRate(i)/ Phase1Step1CourseMean.calcCourseMean(i);
        }
        return correlationValue/36;
    }



    public static double meanPassingRate() {

        //Calculates mean passing rate of the courses based on the correlation value and course mean data from graduate grades.
        double sum = 0;
        for (int i = 0; i < grades[0].length; i++) {
            sum = sum + passingCorrelationValue()* Phase1Step1CourseMean.calcCourseMean(i);
        }
        return sum / grades[0].length;
    }


}


