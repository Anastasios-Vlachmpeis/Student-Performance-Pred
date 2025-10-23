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
}
