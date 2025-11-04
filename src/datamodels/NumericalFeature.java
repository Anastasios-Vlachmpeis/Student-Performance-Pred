package datamodels;

import java.util.HashMap;

/**
 * Implements Numerical feature. (e.g. height, Psionic Interference Tolerance)
 * @author alicehamori
 */
public class NumericalFeature extends Feature {
    private final double value;   // the value of the feature (e.g. height of a student, or some other real valued feature)
    private static HashMap<Integer, double[]> valueRange = new HashMap<>();

    public NumericalFeature(int featureId, double value) {
        super(featureId);
        this.value = value;


        // make sure this feature id is represented and its range is initialized
        if (!valueRange.containsKey(featureId)) {
            valueRange.put(featureId, new double[] {value, value});
        }
        // otherwise just update the range
        else {
            // first index is max, second index is min hence hte name maxmin
            double[] maxMin = valueRange.get(featureId);
            maxMin[0] = Math.max(maxMin[0], value);
            maxMin[1] = Math.min(maxMin[1], value);

        }
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "NumericalFeature(id=" + this.getFeatureId() + ", value=" + String.format("%.2f", this.value) + ")";
    }

    public double getRangeMax(int featureId) {return valueRange.get(featureId)[0];}
    public double getRangeMin(int featureId) {return valueRange.get(featureId)[1];}
}
