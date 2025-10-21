import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/** Reads CurrentGrades.csv, stores it internally, computes statistics
    on itself via public methods
    As of now it is a static class, and meant to be run on its own.
*/
public class CurrentGradesModel {
    final static String pathToCSV = "CurrentGrades.csv";
    final static int studentCount = 1522;
    final static int courseCount = 36;


    // stores the grade table of all students for all courses
    // NG is encoded as -1
    static double[][] grades = new double[studentCount][courseCount];
    static String[] courses = new String[courseCount];
    // links student id to its index in grades[][]
    static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);


    public static void main(String[] args) {
        loadCSV();

        System.out.println(Arrays.deepToString(grades));
    }

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
        return 0.0;
    }

    /** Calculates mode of the grades of a student based on student id. Ignores No Grades.*/
    public static double getStudentMode(int studentId){
        return 0.0;
    }

    /** Calculates median of the grades of a student based on student id. Ignores No Grades.*/
    public static double getStudentMedian(int studentId){
        return 0.0;
    }


    /** Calculates mean of the grades of a course based on course id. Ignores No Grades.*/
    public static double getCourseMean(int courseId){
        return 0.0;
    }

    /** Calculates median of the grades of a course based on course id. Ignores No Grades.*/
    public static double getCourseMedian(int courseId){
        return 0.0;
    }

    /** Calculates mode of the grades of a course based on course id. Ignores No Grades.*/
    public static double getCourseMode(int courseId){
        return 0.0;
    }
}
