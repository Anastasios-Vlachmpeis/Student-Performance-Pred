This from the Gitlab repository of team winlog (#28).

## HOW TO RUN
1. Use java 21 LTS
2. Have javafx 21.0.9 jdk ready
3. Find src/GUI/GUIdemo.java's main method as the entry point
4. Run. (you might need to add javafx parameters to the VM options)

## GUI and Visualizations
The application includes a JavaFX GUI for interactive data visualization and model exploration. When you run `GUIdemo.java`, it opens a window with multiple tabs for different visualizations:

 - **Bar Chart**: Visualize course or student statistics (mean, median, mode, number of NG's) with filtering options
 - **Scatter Plot**: Plot relationships between two courses with overlap visualization
 - **Joint Plot**: Combined scatter plot with marginal histograms showing grade distributions
 - **Pearson Correlation Current**: Heat map showing correlation matrix between current courses
 - **Pearson Correlation Graduate**: Heat map showing correlation matrix between graduate courses
 - **Pie Chart**: Distribution of grades across courses or students
 - **Histogram**: Grade distribution histograms with feature-based filtering
 - **Swarm Plot**: Grade distributions by feature categories
 - **Regression Forest**: Interactive tool to generate random forests and visualize prediction distributions for specific students

All visualizations include filtering controls and use consistent styling. The regression forest tab allows you to specify the number of trees, select a course, and input a student ID to see predictions.

# Mini documentation for data model classes
## How to access student data via code
The data we got via the .csv files is stored in internal models and representation inside the data model classes 
(`GraduateGradesModel`, `CurrentGradesModel`, `StudentInfoModel` named after the .csv). These classes are static and 
and accessible from anywhere inside the project as java static loading ensures that the student data is already loaded
before any of their methods is called.
### Graduate Grades
This class exposes the following three methods.

 - `getGrade(int StudentId, int courseId)` returns the grade of a student with a valid student ID.
 - `getAllGradesStudent(int StudentId)` returns an array of all grades of a student with a valid student ID.
 - `getAllGradesCourse(int courseId)` returns an array of all grades given in a course with a valid course ID.
 - `getCourseName(int courseId)` returns the name of the course associated with a course ID.
 - `getAllStudentIds()` returns an array of all the ids of the students stored in this data model class
 - `getCourses()` returns the array of the course names

**Everything else is meant to be a private member of the class. You should access everything via these methods!**
**Do not make class variables public!**

### Current Grades
This class exposes the following methods: **(No Grade is marked with -1.0)**

 - `getGrade(int StudentId, int courseId)` returns the grade of a student with a valid student ID. 
 - `getAllGradesStudent(int StudentId)` returns an array of all grades of a student with a valid student ID
 - `getAllGradesCourse(int courseId)` return an array of all grades given in a course with a valid course ID
 - `getAllValidGradesStudent(int studentId)` returns an ArrayList of all grades of a student that are not No Grades
 - `getAllValidGradesCourse(int courseId)` returns an ArrayList of all grades given in a course.
 - `getAllStudentIds()`returns an array of all the ids of the students stored in this data model class

**Everything else is meant to be a private member of the class. You should access everything via these methods!**
**Do not make class variables public!**

### Student Info
This class exposes the following methods:

 - `getFeature(int StudentId, int featureId)` Returns a Feature object (categorical or numerical) representing a student feature based on student ID and feature ID
 - `getAllFeatures(int StudentId)` returns an array of Feature objects that has all the features of a single student.
 - `getAllStudentIds()` returns an array of all the ids of the students stored in this data model class
 - `getAllFeatureIds()` returns an array of all the ids of the features represented in this data model class

**Everything else is meant to be a private member of the class. You should access everything via these methods!**
**Do not make class variables public!**

### Feature objects and how to use them
You can consider Feature objects as a new datatype. There are two types of them:

1. Numerical feature (has a featureId, and a value which IS the feature itself)
2. Categorical feature (has a featureId, and a category which IS the feature itself)

To see these in code, you can print a feature object using `System.out.println(myFeature.toString())` because it implements
the `toString()` method.

It is used in two different ways:

 - Stores the feature of student in `StudentInfoModel`
 - Represents splitting criteria at decision stumps. (e.g. if we have a rule that says everyone who has a *Psionic Interference Tolerance* greater than 0.7,
   then this splitting criteria is represented with a Feature Object NumericalFeature(id=3, value=0.7))

When we are tyring to find a good splitting criteria it is useful to know the *range* of the feature. For this reason
we have:
- `NumericalFeature.getRangeMax()` that returns the highest value a feature with that ID can take.
- `NumericalFeature.getRangeMin()` that returns the lowest value a feature with that ID can take.
- `CategoricalFeature.getRange(featureId)` which returns an array of strings of all the categories this feature can take.

## Decision Stumps
Decision stumps that predict the grade of a student are implemented through the `Decision Stump` class.
When initialized it asks for a Feature Object that serves as the splitting criteria, and the two grades it will predict 
whether the student's feature puts the student in the above or below split.
### How to use them for Phase 1 step 4:
You will create a method that takes an array of `DecisionStump` objects (you can assume this array contains all possible
decision stumps) and you will return a smaller array (say with length 10) that is somehow the best subset of them.
To get a prediction from it, you need to call the `.predictGrade(int studentId)` method so you can work with that data.

We will make a decision stump forest for each course. Or rather methods that can find the best decision forest for a given course.
**The Methods**: 
1. least variance (subset of decision stumps whose predictions produces the least variance)
2. noah's method
3. boosted trees algorithm (finding best stump first. find the next stump by maximizing combined results with first stump. repeat)

## Regression Trees and Random Forests
Regression trees predict student grades based on their features using variance reduction to find the best splits. Random forests combine multiple trees trained on bootstrap samples to improve prediction accuracy.

### RegressionTreeTrainer
This class builds a regression tree for a given course and list of student IDs. It exposes the following methods:

 - `train(List<Integer> studentIds, int courseId)` builds a regression tree with default parameters (max depth 3, min samples 10)
 - `train(List<Integer> studentIds, int courseId, int maxDepth, int minSamples)` builds a tree with custom parameters

The training process recursively finds the best decision stump at each node by maximizing variance reduction. If no good split exists or depth/sample limits are reached, it creates a leaf node with the mean grade of that subset.

### TreeNode
This class represents a node in the regression tree. It can be either:
- An internal node with a DecisionStump split rule and left/right children
- A leaf node with a constant prediction value

The following methods are available:

 - `predict(int studentId)` traverses the tree from root to leaf, evaluating split conditions on the student's features, and returns the prediction at the leaf
 - `printTree(String prefix)` prints the tree structure recursively with indentation

### regressionForest
This class creates a random forest using bootstrap aggregation (bagging). It exposes the following methods:

 - `createRegressionForest(int treeNumber, int courseId, int studentId)` trains multiple trees on random 70% samples and returns the rounded average prediction
 - `getRandom(ArrayList<Integer> students)` returns a random 70% sample of students for bootstrap sampling

### ModelEvaluator
This class evaluates regression trees and random forests using standard metrics. It exposes the following methods:

 - `evaluateTree(TreeNode tree, int courseId, List<Integer> evaluationStudentIds)` evaluates a single tree and returns MSE, MAE, and R² metrics
 - `evaluateForest(List<Integer> trainingStudents, int courseId, List<Integer> evaluationStudentIds, int numTrees)` trains a forest once and evaluates it, returning MSE, MAE, and R² metrics
 - `calculateMSE(double[] actual, double[] predicted)` calculates Mean Squared Error
 - `calculateMAE(double[] actual, double[] predicted)` calculates Mean Absolute Error
 - `calculateR2(double[] actual, double[] predicted)` calculates R-squared (coefficient of determination)

The main method runs evaluation across all courses with a 70/30 train-test split and prints average metrics for both single trees and random forests.

**Everything else is meant to be a private member of the class. You should access everything via these methods!**
**Do not make class variables public!**
