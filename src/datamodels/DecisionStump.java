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
     * @param studentId id of the student whose grade should be predicted
     * @return The grade predicted by the decision stump according to student's feature
     */
    public double predictGrade(int studentId) {
        Feature studentFeature = StudentInfoModel.getFeature(studentId, this.splittingFeature.getFeatureId());
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

    public String asRule() {
        // Print out the whole rule as specified in project manual and enter into new line
        return "If "+ this.splittingFeature.toString() + " grade " + String.format("%.2f", this.gradeAboveSplit) + ", else grade " + String.format("%.2f", gradeBelowSplit);
    }
}
