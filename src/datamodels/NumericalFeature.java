package datamodels;

/**
 * Implements Numerical feature. (e.g. height, Psionic Interference Tolerance)
 * @author alicehamori
 */
public class NumericalFeature extends Feature {
    private final double value;   // the value of the feature (e.g. height of a student, or some other real valued feature)

    public NumericalFeature(int featureId, double value) {
        super(featureId);
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "NumericalFeature(id=" + this.getFeatureId() + ", value=" + String.format("%.2f", this.value) + ")";
    }
}
