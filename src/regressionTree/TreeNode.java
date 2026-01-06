package regressionTree;

import datamodels.*;


 // Represents a node in the regression tree.
// Can be either an internal node with a split rule or a leaf node with a prediction value.

public class TreeNode {

    public DecisionStump splitRule;
    public TreeNode left;
    public TreeNode right;
    public double prediction;


     //Creates a leaf node with a fixed prediction.

    public static TreeNode leaf(double prediction) {
        TreeNode node = new TreeNode();
        node.prediction = prediction;
        node.splitRule = null;
        return node;
    }


     //Creates an internal node with a decision stump.

    public static TreeNode internal(DecisionStump stump) {
        TreeNode node = new TreeNode();
        node.splitRule = stump;
        return node;
    }


     // Recursively predicts the grade for a student by following decision rules down the tree.

    public double predict(int studentId) {
        // if this is a leaf, return stored prediction
        if (splitRule == null) {
            return prediction;
        }

        // otherwise evaluate split condition and recurse, i used phase 1 method 3 logic here. i hope its correct
        Feature f = StudentInfoModel.getFeature(
                studentId,
                splitRule.getSplittingFeature().getFeatureId()
        );

        if (SplitCondition.evaluate(f, splitRule.getSplittingFeature())) {
            return right.predict(studentId);
        } else {
            return left.predict(studentId);
        }
    }
}