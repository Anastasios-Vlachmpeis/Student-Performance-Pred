
// This file shows you how to add your code to the solutions folder (java calls folders packages)
// See Main.java's main method's last section Step 3 how these types of classes are incorporated into the
// codebase.

// make this line as the first line. this is just letting java know that your class is inside the solutions folder
package solutions;

// import the datamodel classes you need like so
// (you do not need to bother with reading the .csv, alice made them to read them automatically:))
import datamodels.GraduateGradesModel;
// import datamodels.CurrentGradesModel;
// import datamodels.StudentInfoModel;

// Class's name should follow this convention:
//     Phase[insert phase number here]Step[insert step number here][Question 1 or subtask]
// If you are not sure see how the already existing files are named in the solutions folder
public class TemplateClassForAnswers {

    // this where to test code
    public static void main(String[] args) {
        methodToAnswerSomeQuestion();
    }

    public static void methodToAnswerSomeQuestion() {
        double[] gradesOfStudent3 = GraduateGradesModel.getAllGradesStudent(3);
        double sum = 0;
        for (int i = 0; i < gradesOfStudent3.length; i++) {
            sum += gradesOfStudent3[i];
        }
        System.out.println("Mean grade of graduate student with id 3 is: " + sum / gradesOfStudent3.length);
    }

    public static void someHelperMethod() {}
}
