package datamodels;

/**
 * Implements categorical feature (e.g. colors, gender, Bio-Luminal transmission)
 * @author alicehamori
 */
public class CategoricalFeature extends AbstractFeature {
    private final String category;    // for example if a feature is about colors then category="red" or "green" etc

    public CategoricalFeature(int featureId, String category) {
        super(featureId);
        this.category = category;
    }

    public String getCategory() {return this.category;}

    @Override
    public String toString() {
        return "datamodels.CategoricalFeature(featureId=" + this.getFeatureId() + ", category=" + this.category + ")";
    }
}
