package datamodels;

/**
 * Represents a decision stump that predicts a grade. This object does not know
 * what course's grade it is predicting. Its sole purpose is to store the necessary information
 * to perform a prediction.
 */
public class DecisionStump {
    private final Feature splittingFeature;
    private final double gradeAboveSplit;
    private final double gradeBelowSplit;

    public DecisionStump(Feature splittingFeature, double gradeAboveSplit, double gradeBelowSplit) {
        this.splittingFeature = splittingFeature;
        this.gradeAboveSplit = gradeAboveSplit;
        this.gradeBelowSplit = gradeBelowSplit;
    }

    /**
     * Predicts a grade for a student based on a feature of the student.
     * @param studentFeature MUST MATCH THE ID OF THE FEATURE USED IN THE PREDICTION
     * @return The grade predicted by the decision stump according to student's feature
     */
    public double doPrediction(Feature studentFeature) {
        if (studentFeature.getFeatureId() != this.splittingFeature.getFeatureId()) {
            throw new IllegalArgumentException(
                    "studentFeature's ID MUST MATCH THE ID OF THE FEATURE USED IN THE PREDICTION\n" +
                    "studentFeature's ID was " + studentFeature.getFeatureId() + " while decision stump used id " + this.splittingFeature.getFeatureId()
            );
        }
        boolean isAboveSplit = SplitCondition.evaluate(studentFeature, this.splittingFeature);
        if (isAboveSplit) {
            return this.gradeAboveSplit;
        }
        else {
           return this.gradeBelowSplit;
        }
    }

    public Feature getSplittingFeature() {
        return splittingFeature;
    }

    public double getGradeAboveSplit() {
        return gradeAboveSplit;
    }

    public double getGradeBelowSplit() {
        return gradeBelowSplit;
    }
}
