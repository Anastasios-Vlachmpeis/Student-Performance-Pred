package datamodels;

/**
 * A new datatype that holds features for datasets. (Categorical, numerical, etc. feature classes are derived from this)
 * It can also be used to be the splitting "boundary" for splitting up a dataset by
 * using this feature to decide if an element of the dataset is in the "upper subset" or not.
 * @see SplitCondition
 * @author alicehamori
 */
public abstract class AbstractFeature {
    private final int featureId;  // global feature id datamodels.StudentInfoModel can be used to get the feature name

    public AbstractFeature(int featureId) {
        this.featureId = featureId;
    }

    public int getFeatureId() {
        return this.featureId;
    }
}
