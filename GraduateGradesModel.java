import java.io.File;
import java.lang.reflect.Array;
import java.util.Scanner;
import java.util.Arrays;

public class GraduateGradesModel {
    /* This class was specifically made for the "GraduateGrades.csv" file.
     * Originally was a FileDisplayer provided by UM, but it got modified to specialize on previously mentioned .csv
     *
     * Changes will be needed so it can support the other .csv files we got for the project
     * that might include missing values and other data types.
     */


    // Contains name of the courses. courseID is equivalent to index in the array
    static String[] courses = new String[36];
    // Internal representation of student's grades as a table (2D array)
    // First index corresponds to studentID
    // Second index correspond to the courseID
    // Their combination tells a given student's grade at a given course
    static double[][] grades = new double[21243][36];

    public static void main(String[] args) {

		try {
			// Adapt this when you want to read and display a different file.
			String fileName = "GraduateGrades.csv";
			System.out.println("Start reading file: " + fileName);  // Debug
			System.out.println("This will take a while...");        // Debug

			File file=new File(fileName);
			
			// This code uses two Scanners, one which scans the file line per line
			Scanner fileScanner = new Scanner(file);
			int linesDone = 0;

			String line = fileScanner.nextLine();
			linesDone++;
			// and one that scans the line entry per entry using the commas as delimiters
			Scanner lineScanner = new Scanner(line);
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

			// Then, the code processes students line by line and load their grades into
			// grades that is a 2D array and the internal representation of the grade table.
			int studentCounter = 0;
			while (fileScanner.hasNextLine() && linesDone < 212245) {
				// Every line now starts with the student id, but that will be omitted.
				// This is because the first index in the 2D array serves as the 

				// The second scanner is reused
				line = fileScanner.nextLine();
				lineScanner = new Scanner(line);
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
			System.out.println("Finished reading: " + fileName);    // DEBUG

			} catch (Exception ex) {
				ex.printStackTrace();
		}	

		//==============================//
		// POST reading: Put code below //
		//==============================//

		// Example: Printing the grade of studentID 42 at Evolutionary Dynamics (courseID 1)
		System.out.println("The grade of student with ID 42 at Evolutionary Dynamics is " + grades[42][1]);
		System.out.println("The course with courseID 25 is " + courses[25]);

        performDescriptiveStatisticOnStudent(2);
    }

    public static void performDescriptiveStatisticOnStudent(int studentId) {
        if (studentId < 0 || studentId > 21243) {
            System.out.println("Student ID doesn't exist");
            return;
        }

        double[] studentGrades = grades[studentId];

        //MEAN
        double sum = 0;
        for (int i = 0; i < studentGrades.length; i++) {
            double grade = studentGrades[i];
            sum += grade;
        }
        double mean = sum / studentGrades.length;


        //MODE
        sum = 0;
        double mode = studentGrades[0];
        int maxCount = 0;

        for (int i = 0; i < studentGrades.length; i++) {
            double current = studentGrades[i];
            int count = 0;

            for (int j = 0; j < studentGrades.length; j++) {
                if (studentGrades[j] == current) {
                    count++;
                }
            }

            if (count > maxCount) {
                maxCount = count;
                mode = current;
            }
        }

        //MEDIAN
        // Arrays.sort() is in place method => make a copy of student's grades so grades 2D indexing is not messed up
        // Reuse studentGrades from MODE
        Arrays.sort(studentGrades);

        // MEDIAN is calculated differently depening if there are even or odd number of elements
        double median;
        if (studentGrades.length % 2 == 1) {
            // When odd, it is exactly the middle element
            median = studentGrades[studentGrades.length / 2];
        } else {
            // Average of the middle two value
            int middleLeft, middleRight;
            middleRight = studentGrades.length / 2;
            middleLeft = (studentGrades.length / 2) - 1;
            median = (middleLeft + middleRight) / 2.0;
        }

        System.out.println("For the student with the studentId " + studentId + ":");
        System.out.println("Mean: " + mean);
        System.out.println("Mode: " + mode);
        System.out.println(("Median: " + median));
    }

}

