This from the Gitlab repository of team winlog (#28).


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

 - getFeature(int StudentId, int featureId) Returns a Feature object (categorical or numerical) representing a student feature based on student ID and feature ID
 - getAllFeatures(int StudentId) returns an array of Feature objects that has all the features of a single student.
 - getAllStudentIds() return an array of all the ids of the students stored in this data model class

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
- `NumericalFeature.getRange()` that returns the lowest and highest value a feature with that ID can take. (`double[2]`)
- `CategoricalFeature.getRange(featureId)` which returns an ArrayList of strings of all the categories this feature can take. (`ArrayList<String>`)

Using these you can try out all possible splitting criteria.

## HOW TO RUN
1. Find Main.java in src/
2. Run Main.java in terminal
3. If errors are thrown check that you use the most recent version of java and that you
   run the command from the right directory.
(We used Intellij for developing the code, the easies it to import the project and run it from there)

*This is an easter egg:)*
