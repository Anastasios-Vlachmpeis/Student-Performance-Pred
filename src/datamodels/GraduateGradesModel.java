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
    public static String[] courses = new String[courseCount];
    // Internal representation of student's grades as a table (2D array)
    // First index corresponds to studentID
    // Second index correspond to the courseID
    // Their combination tells a given student's grade at a given course
    public static double[][] grades = new double[studentCount][courseCount];
    // links student id to its index in grades[][]
    private static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);

    // ensure .csv is loaded before DataModel class is accessed
    static {loadCSV();}
    public static void loadCSV() {
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

}

