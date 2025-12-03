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
        /** 
         * We must use the subclass type to determine which isIdAllowed to call, rather than using directly the argument
         * because we want to validate the featureId based on the specific subclass being instantiated
         */
        if (this instanceof CategoricalFeature) {
            if (!CategoricalFeature.isIdAllowed(featureId)) {
                throw new IllegalArgumentException("featureId " + featureId + " is not allowed for categorical features.");
            }
        } else if (this instanceof NumericalFeature) {
            if (!NumericalFeature.isIdAllowed(featureId)) {
                throw new IllegalArgumentException("featureId " + featureId + " is not allowed for numerical features.");
            }
        }
        this.featureId = featureId;
    }

    public int getFeatureId() {
        return this.featureId;
    }
}
