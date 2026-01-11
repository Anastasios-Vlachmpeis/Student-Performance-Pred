package GUI;

import GUI.tab.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GUIdemo extends Application {

    // Root container for the application UI
    private BorderPane root;

    public void start(Stage primaryStage) {
        root = new BorderPane();

        // Create TabPane
        TabPane tabPane = new TabPane();

        // Tab 0: Bar Chart
        Tab barTab = new Tab("Bar Chart");
        barTab.setClosable(false);
        BorderPane barPane = new BarChartTab().createBarChartTab();
        barTab.setContent(barPane);
        tabPane.getTabs().add(barTab);
        
        // Tab 1: Scatter Plot
        Tab scatterPlotTab = new Tab("Scatter Plot");
        scatterPlotTab.setClosable(false);
        BorderPane scatterPlotPane = new ScatterPlotTab().createScatterPlotTab();
        scatterPlotTab.setContent(scatterPlotPane);
        tabPane.getTabs().add(scatterPlotTab);

        // Tab 2: Joint Plot
        Tab jointPlotTab = new Tab("Joint Plot");
        jointPlotTab.setClosable(false);
        BorderPane jointPlotPane = new JointPlotTab().createJointPlotTab();
        jointPlotTab.setContent(jointPlotPane);
        tabPane.getTabs().add(jointPlotTab);

        // Tab 3: Faux Heat Map
        Tab fauxHeatMapTab = new Tab("Pearson Correlation Current");
        fauxHeatMapTab.setClosable(false);
        BorderPane fauxHeatMapCurrentGrades = new HeatMapTab().createPearsonCorrelationCurrentCourses();
        fauxHeatMapTab.setContent(fauxHeatMapCurrentGrades);
        tabPane.getTabs().add(fauxHeatMapTab);

        // Tab 3.5: Faux Heat Map
        Tab fauxHeatMapTab2 = new Tab("Pearson Correlation Graduate");
        fauxHeatMapTab2.setClosable(false);
        BorderPane fauxHeatMapGraduateGrades = new HeatMapTab().createPearsonCorrelationGraduateCourses();
        fauxHeatMapTab2.setContent(fauxHeatMapGraduateGrades);
        tabPane.getTabs().add(fauxHeatMapTab2);

        // Tab 4: Pie Chart
        Tab PieChartTab = new Tab("Pie Chart");
        PieChartTab.setClosable(false);
        BorderPane PieChartPane = new PieChartTab().createPieChart();
        PieChartTab.setContent(PieChartPane);
        tabPane.getTabs().add(PieChartTab);

        // Tab 5: Histogram
        Tab histogramTab = new Tab("Histogram");
        histogramTab.setClosable(false);
        BorderPane histogramPane = new HistogramTab().createHistogramTab();
        histogramTab.setContent(histogramPane);
        tabPane.getTabs().add(histogramTab);

        // Tab 6: Swarm plot
        Tab swarmPlotTab = new Tab("Swarm Plot");
        swarmPlotTab.setClosable(false);
        BorderPane swarmPlotPane = new SwarmPlotTab().createTab();
        swarmPlotTab.setContent(swarmPlotPane);
        tabPane.getTabs().add(swarmPlotTab);

        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1000, 840);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graph Generator");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
