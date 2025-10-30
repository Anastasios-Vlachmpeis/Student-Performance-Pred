package datamodels;

/**
 * Manages splitting student datasets based on features.
 * @author alicehamori
*/
public class SplitCondition {

    /**
     * Evaluates if a student is in the upper or lower split of this split condition.
     * @param studentFeature is a feature object holding a specific feature of the given student.
     * @param splitFeature is a feature object representing the category to be used for splitting
     * @return true if student's feature matches split condition's category, otherwise false
     */
    public static boolean evaluate(CategoricalFeature studentFeature, CategoricalFeature splitFeature) {
        return splitFeature.getCategory().equals(studentFeature.getCategory());
    }

    /**
     * Evaluates if a student is in the upper or lower split of this split condition.
     * @param studentFeature is a feature object holding a specific feature of the given student.
     * @param splitFeature is a feature object representing the numerical threshold to be used for splitting
     * @return true if student's feature is above the numerical threshold, false if it is equal to or smaller
     */
    public static boolean evaluate(NumericalFeature studentFeature, NumericalFeature splitFeature) {
        return splitFeature.getValue() < studentFeature.getValue();
    }

    public static boolean evaluate(AbstractFeature studentFeature, AbstractFeature splitFeature) {
        // we don't know the type of the feature so we try all we know
        if (studentFeature instanceof CategoricalFeature && splitFeature instanceof CategoricalFeature) {
            return evaluate((CategoricalFeature) studentFeature, (CategoricalFeature) splitFeature);
        }
        else if (studentFeature instanceof NumericalFeature && splitFeature instanceof NumericalFeature) {
            return evaluate((NumericalFeature) studentFeature, (NumericalFeature) splitFeature);
        }
        else {
            throw new IllegalArgumentException("The two features must have the same type!");
        }

    }
}
