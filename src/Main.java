import java.util.Locale;

/** Entry point of the project. You can access the data models from here.*/
public class Main {
    public static void main(String[] args) {
        // some computers use commas as the digit separator at floating point numbers.
        // US uses dots.
        Locale.setDefault(Locale.US);

        GraduateGradesModel.loadCSV();
        CurrentGradesModel.loadCSV();
        StudentInfoModel.loadCSV();

        // PUT CODE HERE //
        // You can invoke methods of Model Static Classes
        // or just use them in methods of this class
        /**
         * Q3: Are there courses that seem similar or related?
         * Find top 10 most similar course pairs.
         */
        int TOP_K = 10; // Take the top 10 course pairs with the highest correlation
        //GraduateGradesModel.printTopKCorrelatedCoursePairs(TOP_K);  //Q3
        //GraduateGradesModel.printBestAndWorstCourse();  //Q1
        //GraduateGradesModel.printCumLaudeStudents();  //Q2
        //CurrentGradesModel.printStudentNGcount();
        //CurrentGradesModel.printCourseNGcount();
        //CurrentGradesModel.printFailedCourses();
    }
}
