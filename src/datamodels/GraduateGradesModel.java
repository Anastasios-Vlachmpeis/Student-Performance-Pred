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
 *      - getAllStudentIds()
 *      - getCourseName(int courseId)
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
     * This was missing but is necessa
     * Gives a specific grade of a student for a course.
     * @param studentId global id of the student
     * @param courseId global id of the course
     * @return the grade of the student in the course
     */
    public static double getGrade(int studentId, int courseId) {
        int studentIndex = studentID2index.get(studentId);
        return grades[studentIndex][courseId];
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
        // Fixed bug where we would iterate over courseCount instead of studentCount to get all grades of a course
        for (int i = 0; i < studentCount; i++) {
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

    /**
     * Gives the name of the course assigned to an id.
     * @param courseId valid course id (0-36)
     * @return name of the course
     */
    public static String getCourseName(int courseId) {
        return courses[courseId];
    }
}

