package GUI.tab;

import GUI.chart.RegressionForestGenerator;
import GUI.style.UIStyling;
import datamodels.CurrentGradesModel;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import regressionTree.regressionForest;
import tools.ChartDataUtils;
import GUI.chart.RegressionForestResult;

import java.util.Arrays;

public class RegressionForestTab {

    private ComboBox<String> CourseCombo;
    private Label CourseLabel;
    private Label TreeCountLabel;
    private Label studentIdLabel;
    private TextField TreeCount;
    private TextField studentId;
    private Label predictedInfo;
    private Label actualInfo;
    private Button generate;
    private BorderPane root;
    private VBox box;
    private RegressionForestGenerator generator;

    public BorderPane createRegressionForestTab() {
        generator = new RegressionForestGenerator();
        root = new BorderPane();

        ScrollPane scrollPane = new ScrollPane(createControls());
        scrollPane.setFitToWidth(true);
        root.setLeft(scrollPane);


        return root;
    }

    private VBox createControls() {
        box = new VBox();
        UIStyling.styleControlPanel(box);

        // set x axis combobox
        CourseLabel = new Label("Select Course:");
        UIStyling.styleHeadingLabel(CourseLabel);
        CourseCombo = new ComboBox<>();
        UIStyling.styleComboBox(CourseCombo);
        // add all the course names to the combobox
        String[] courses = CurrentGradesModel.getCourses();
        for (int i = 0; i < courses.length; i++) {
            CourseCombo.getItems().add(courses[i]);
            }

        CourseCombo.setValue(courses[0]);

        TreeCountLabel = new Label("Number of trees to be trained:");
        UIStyling.styleHeadingLabel(TreeCountLabel);
        TreeCount = new TextField();

        studentIdLabel = new Label("Id of the student to predict:");
        UIStyling.styleHeadingLabel(studentIdLabel);
        studentId = new TextField();

        generate = new Button("Generate");
        UIStyling.stylePrimaryButton(generate);
        generate.setOnAction(e -> {
            generateForest();
        });
        // i added this many spacers because otherwise the control pane was looking too compact and small, now looks better
        Region spacer0 = new Region();
        spacer0.setMinHeight(20);
        Region spacer1 = new Region();
        spacer1.setMinHeight(50);
        Region spacer2 = new Region();
        spacer2.setMinHeight(50);
        Region spacer3 = new Region();
        spacer3.setMinHeight(50);

        box.getChildren().addAll(spacer0, CourseLabel, CourseCombo, spacer1, TreeCountLabel, TreeCount, spacer2, studentIdLabel, studentId, spacer3, generate);

        return box;
    }

    private void generateForest() {

        // reset every output at the start so they dont use memory / take space
        root.setCenter(null);
        box.getChildren().removeAll(predictedInfo, actualInfo);

        // i made the regression forest to work only between 10 and 1000 trees because other-wise it would
        //be too small(overfit) or too big(takes too long to generate)
        int treeCount = Integer.parseInt(TreeCount.getText());
        if (treeCount < 10 || treeCount > 1000) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid input");
            alert.setContentText("\"Please enter a number between 10 and 1000.");
            alert.showAndWait();
            return;
        }

        //we get the courseid by inputting course name thanks to the findCourseId method
        String Course = CourseCombo.getValue();
        int CourseId = ChartDataUtils.findCourseId(Course);

        //we parse the textfield as an integer because otherwise it takes it as string
        int[] students = CurrentGradesModel.getAllStudentIds();
        int StudentId = Integer.parseInt(studentId.getText());
        //check if the student id that the user wrote exists by using arrays stream. (thanks stack overflow)
        boolean exists = Arrays.stream(students)
                .anyMatch(id -> id == StudentId);

        if (!exists) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid input");
            alert.setContentText("\"Please enter a correct student id.");
            alert.showAndWait();
            return;
        }

        //create the forest and save it to the result class
        RegressionForestResult result =
                generator.createChart(treeCount, CourseId, StudentId);

        //use getter to get the chart from result class and display it
        BarChart<String, Number> chart = result.getChart();
        root.setCenter(chart);

        //compare the predicted and actual grades
        double predicted = Math.round(result.getPredictedGrade());
        double actual = CurrentGradesModel.getGrade(StudentId, CourseId);
        predictedInfo = new Label("Predicted Grade: " + predicted);
        actualInfo = new Label("Actual Grade: " + actual);
        UIStyling.styleHeadingLabel(predictedInfo);
        UIStyling.styleHeadingLabel(actualInfo);
        box.getChildren().addAll(predictedInfo, actualInfo);
    }
}