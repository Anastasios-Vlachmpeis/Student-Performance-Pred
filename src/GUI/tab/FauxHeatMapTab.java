package GUI.tab;

import datamodels.CurrentGradesModel;
import datamodels.GraduateGradesModel;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import tools.Color;
import tools.PearsonCorrelation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FauxHeatMapTab {
    final CategoryAxis xAxis = new CategoryAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final XYChart<String, String> sc = new ScatterChart<>(xAxis, yAxis);

    public BorderPane createPearsonCorrelationCurrentCourses() {
        xAxis.setLabel("Current Courses");
        yAxis.setLabel("Current Courses");
        sc.setTitle("Pearson Correlation between current courses based on current grades");

        // compute pearson correlation for every course pairs
        double[][] correlationMatrix = new double[CurrentGradesModel.courseCount][CurrentGradesModel.courseCount];
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                correlationMatrix[i][j] = PearsonCorrelation.betweenCurrentCourses(i, j);
            }
        }
        // range of the pearson coefficient is [-1, 1]
        // so I am creating 10 buckets of size 0.2 like such
        // [-1, -0.8], (-0.8, -0.6], (-0.6, -0.4], ..., (0.4, 0.6], (0.6, 0.8], (0.8, 1]
        XYChart.Series[] correlationBuckets = new XYChart.Series[10];
        // fill up the array
        for (int i = 0; i < correlationBuckets.length; i++) {
            correlationBuckets[i] = new XYChart.Series<String, String>();
            correlationBuckets[i].setName("r≤" + String.format("%.2f", (-0.2 + i * 0.2)));
        }
        // go through all the correlations and put them into respective bucket
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                double pearsonCorrelation = correlationMatrix[i][j];
                int bucket = Math.min(9, (int) ((pearsonCorrelation + 1.0) / 0.2));
                correlationBuckets[bucket].getData().add(new XYChart.Data<String, String>(CurrentGradesModel.getCourseName(i), CurrentGradesModel.getCourseName(j)));
            }
        }

        for (XYChart.Series series : correlationBuckets) {
            if (series.getData().isEmpty()) {
                continue;
            }
            sc.getData().add(series);
        }

        // set color of the symbols of the series
        for (Node x : sc.lookupAll(".series")) {
            x.setStyle("");
        }

        // put into a neat scrollpane that is put into a neat tabpane
        return new BorderPane(sc);
    }

    public Pane createPearsonCorrelationGraduateCourses() {
        xAxis.setLabel("Graduate Courses");
        yAxis.setLabel("Graduate Courses");
        sc.setTitle("Pearson Correlation between graduate courses based on graduate grades");



        // compute pearson correlation for every course pairs
        double[][] correlationMatrix = new double[CurrentGradesModel.courseCount][CurrentGradesModel.courseCount];
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                correlationMatrix[i][j] = PearsonCorrelation.betweenGraduateCourses(i, j);
            }
        }



        // range of the pearson coefficient is [-1, 1]
        // so I am creating 10 buckets of size 0.2 like such
        // [-1, -0.8], (-0.8, -0.6], (-0.6, -0.4], ..., (0.4, 0.6], (0.6, 0.8], (0.8, 1]
        XYChart.Series[] correlationBuckets = new XYChart.Series[10];
        // fill up the array
        for (int i = 0; i < correlationBuckets.length; i++) {
            correlationBuckets[i] = new XYChart.Series<String, String>();
            correlationBuckets[i].setName("r≤" + String.format("%.2f", -1 + (0.2 + i * 0.2)));
        }



        // go through all the correlations and put them into respective bucket
        for (int i = 0; i < correlationMatrix.length; i++) {
            for (int j = 0; j < correlationMatrix[i].length; j++) {
                double pearsonCorrelation = correlationMatrix[i][j];
                int bucket = (int) Math.max(0, Math.floor((pearsonCorrelation + 1.0) / 0.2)-1);
                correlationBuckets[bucket].getData().add(new XYChart.Data<String, String>(GraduateGradesModel.getCourseName(i), GraduateGradesModel.getCourseName(j)));
            }
        }


        for (int i = 0; i < correlationBuckets.length; i++) {
            XYChart.Series series = correlationBuckets[i];
            if (series.getData().isEmpty()) {
                continue;
            }

            sc.getData().add(series);
        }

        List<Integer> usedSeriesNumbers = new ArrayList<>();
        for (int seriesNumber = 0; seriesNumber < sc.getData().size(); seriesNumber++) {
            boolean flagIsFirst = true;
            for (Node seriesNode : sc.lookupAll(".series" + seriesNumber)) {
                seriesNode.setStyle("-fx-background-color: " + Color.heatmapRGBRedBoosted(-1, 1, -0.2 + seriesNumber * 0.2) + "; -fx-shape: \"M 10 10  v10.0 h 10.0  v-10  Z\";-fx-padding: 6px;");
                if (flagIsFirst) {
                    usedSeriesNumbers.add(seriesNumber);
                    flagIsFirst = false;
                }
            }

        }
        int counter = 0;
        Set<Node> legendItems = sc.lookupAll("Label.chart-legend-item");
        for (Node legendNode : legendItems) {
            Label label = (Label) legendNode;
            int seriesNumber = usedSeriesNumbers.get(counter);
            // removes the default icon from the legend
            label.setGraphic(null);
            // colors the legend entry matching the corresponding heatmap color
            label.setStyle("-fx-background-color: " + Color.heatmapRGBRedBoosted(-1, 1, -0.2 + seriesNumber * 0.2));
            counter++;
        }

        return new BorderPane(sc);
    }
}
