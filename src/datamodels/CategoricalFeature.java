package datamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements categorical feature (e.g. colors, gender, Bio-Luminal transmission)
 * @author alicehamori
 */
public class CategoricalFeature extends Feature {
    private final String category;    // for example if a feature is about colors then category="red" or "green" etc
    private static HashMap<Integer, HashSet<String>> categoryRange = new HashMap<>();

    public CategoricalFeature(int featureId, String category) {
        super(featureId);
        this.category = category;

        // updates category range

        // make sure this feature id is represented and its range is initialized
        if (!categoryRange.containsKey(featureId)) {
           categoryRange.put(featureId, new HashSet<String>());
        }
        // update category of specific feature (since it is a set, if it is already contained it won't be added again)
        categoryRange.get(featureId).add(category);

    }

    public String getCategory() {return this.category;}

    @Override
    public String toString() {
        return "CategoricalFeature(id=" + this.getFeatureId() + ", category=" + this.category + ")";
    }

    /**
     * Used to get all possible strings this feature with this feature id can have as its .category variable.
     * @param featureId A valid id of a categorical feature
     * @return A list of all categories this feature can be.
     */
    public static String[] getRange(int featureId) {
        return categoryRange.get(featureId).toArray(new String[0]);
    }
}
