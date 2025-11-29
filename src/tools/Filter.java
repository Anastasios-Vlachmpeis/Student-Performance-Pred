package tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter object (immutable)
 * see property javadocs for more
 */
public class Filter {
    /**
     * operator is a string that shows if it is a less than equal, equal, etc...
     * all operators are made of 2 chars so strictly less is "<<" and strictly greater is ">>".
     * The full list of allowed operators is in the "recognizedOperators[]" static array of this class.
     * Also, on the GUI whatever is on the left side of the operator is the data filtered, and whatever
     * is on the right side of the operator is the filter value given by the user. (e.g. feature1 !="Stable" where
     * feature1 will be displayed by the GUI and not written by the user ofc)
     * @link Filter
     */
    final private String  operator;

    /**
     * This string contains everything that is after the operator from a user input.
     * It is up to you to validate and interpret it in the context of the associated operator:D
     */
    final private String argument;

    Filter(String  operator, String argument) {
       this.operator = operator;
       this.argument = argument;
    }
    Filter( Filter other) {
        this.operator = other.operator;
        this.argument = other.argument;
    }

    public String  getOperator() {
        return operator;
    }

    public String getArgument() {
        return argument;
    }

//    public enum OPERATOR {
//        /* - for numerical features, but equal and not_equal can be used for categorical features as well ----- */
//        STRICTLY_LESS,      // forall X select X if X < const
//        LESS_OR_EQUAL,      // forall X select X if X <= const
//        EQUAL,              // forall X select X if X == const (can be used with categorical features as well)
//        GREATER_OR_EQUAL,   // forall X select X if X >= const
//        STRICTLY_GREATER,   // forall X select X if X > const
//        NOT_EQUAL,          // forall X select X if X != const (can be used with categorical features as well)
//    }

    public static String[] recognizedOperators = new String[] {"<<", "<=", "=", ">=", ">>", "!="};


    /**
     * Parses the text given by the user for filtering data.
     * (e.g. ">=5" or "=1.2" or "<0.5")
     * Notice that the constant given by the user is always on the right side
     * @author Alice
     */
    public static Filter parseUserFilter(String userInput) throws IOException {
        if (userInput == null) {
            throw new IllegalArgumentException("User's input must not be null!");
        }

        // clean up input
        userInput = userInput.trim().toLowerCase().replace(" ", "");

        // start parsing from left to right.
        //  find first operator and constant associated with the operator
        //  if there is more, throw exception
        //  if there is no valid operator then also throw exception
        Map<Character, Integer> operationCounter = new HashMap<>();
        for (char c : userInput.toCharArray()) {
            operationCounter.put(c, operationCounter.getOrDefault(c, 0) + 1);
        }

        // check if starts with a recognized operator
        boolean correctStartCheck = false;
        String selectedOperator = "";
        for (String recognizedOperator : recognizedOperators) {
            if (userInput.startsWith(recognizedOperator)) {
                correctStartCheck = true;
                selectedOperator = recognizedOperator;
            }
        }
        if (!correctStartCheck) {
            throw new IOException("User's input must start with a valid operator " + Arrays.toString(recognizedOperators) + "but this one did not do so: " + userInput);
        }

        // check it only contains one operator
        boolean onlyOneOperatorCheck = true;
        for (String recognizedOperator : recognizedOperators) {
            if (recognizedOperator.equals(selectedOperator)) {
                if (userInput.indexOf(selectedOperator) != 0) {
                    onlyOneOperatorCheck = false;
                }
                continue;
            }

            if (userInput.indexOf(recognizedOperator) != -1) {
                onlyOneOperatorCheck = false;
            }
        }
        if (!onlyOneOperatorCheck) {
            throw new IOException("User's input must contain only one valid operator + " + Arrays.toString(recognizedOperators) + "but this one did not do so: " + userInput);
        }

        // the thing behind the operator
        String argument = userInput.substring(2);


        // packages into a nice filter.
        return new Filter(selectedOperator, argument);
    }
}
