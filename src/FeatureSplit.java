import java.lang.reflect.Field;

/**
 * Simple "struct" that encompasses data necessary for a split into 2 groups:
 *  - name of the feature the split is based on
 *  - the boundary is used to split
 * However, the splitting criteria might be a category (ENUM) like where the splitting is done such a way
 * that one group are with the objects that have the same property in that feature and the other group
 * has objects that have a different property in that feature.
 * If the splitting criteria is a real number then it is considered a threshold value.
 * One group has all the objects with the feature-property less or equal to the threshold,
 * while the other group has objects with feature-properties greater than the threshold.*/
public class FeatureSplit {
    String name;
    boolean isFeatureACategory;
    Double threshHoldValue;
    String selectionCategory;

    public FeatureSplit(String featureName, String selectionCategory) {
        this.name = featureName;
        this.isFeatureACategory = true;
        this.threshHoldValue = null;
        this.selectionCategory = selectionCategory;
    }

    public FeatureSplit(String featureName, double threshHoldValue) {
        this.name = featureName;
        this.isFeatureACategory = false;
        this.threshHoldValue = threshHoldValue;
        this.selectionCategory = null;
    }

    /**
     * Pythonic way of representing the split with its feature and boundary.
     */
    @Override
    public String toString() {
        String splitBoundaryRep = this.isFeatureACategory ? this.selectionCategory : this.threshHoldValue.toString();
        return getClass().getName() + "(Feature: " + this.name + ", Split Boundary: " + splitBoundaryRep +")";
    }

    /**
     * format's feature split into the first part of a rule sentence described in the project manual:
     * If X then grade Y, else grade Z
     *  where X is a thresholded student property and Y and Z are the mean scores of
     *  student with and without property X, respectively.
     * This method return the first part of the rule til the word then without a whitespace.
     */
    public String asRule() {
        if (this.isFeatureACategory) {
            return "If " + this.name + " is " + this.selectionCategory + " then";
        } else {
            return "If " + this.name + " > " + this.threshHoldValue + " then";
        }

    }
}
