package datamodels;

/**
 * A new datatype that holds features for datasets. (Categorical, numerical, etc. feature classes are derived from this)
 * It can also be used to be the splitting "boundary" for splitting up a dataset by
 * using this feature to decide if an element of the dataset is in the "upper subset" or not.
 * @see SplitCondition
 * @author alicehamori
 */
public abstract class Feature {
    private final int featureId;  // global feature id datamodels.StudentInfoModel can be used to get the feature name

    // tells whether this index is the actual type of the feature
    public static boolean isIdAllowed(int featureId) {
        // pass
        return false;
    };

    public Feature(int featureId) {
//        if (!isIdAllowed(featureId)) {
//            throw new IllegalArgumentException("featureId " + featureId + " does not align with the type of the feature you are trying to create. Look into the allowed ids private field in the class of the object you are trying to create!");
//        }
        this.featureId = featureId;
    }

    public int getFeatureId() {
        return this.featureId;
    }
}
