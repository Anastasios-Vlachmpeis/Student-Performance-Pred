package GUI.tab;

import GUI.chart.HeatMapGenerator;
import javafx.scene.layout.BorderPane;

public class HeatMapTab {
    
    private HeatMapGenerator generator;

    public BorderPane createPearsonCorrelationCurrentCourses() {
        generator = new HeatMapGenerator();
        return generator.createCurrentCoursesHeatMap();
    }

    public BorderPane createPearsonCorrelationGraduateCourses() {
        generator = new HeatMapGenerator();
        return generator.createGraduateCoursesHeatMap();
    }
}
