package regressionTree;

import datamodels.*;


 //wrapper class for a trained regression tree.

public class RegressionTree {

    //root node of the regression tree
    private final TreeNode root;


     //create a regression tree from a trained root node.

    public RegressionTree(TreeNode root) {
        this.root = root;
    }


    public double predict(int studentId) {
        return root.predict(studentId);
    }
}