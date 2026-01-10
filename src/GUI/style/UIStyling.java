package GUI.style;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import java.util.Base64;

/**
 * Centralized styling utilities for UI components.
 * Provides simple style methods that modify existing components.
 */
public class UIStyling {
    
    // Color constants
    private static final String PRIMARY_COLOR_START = "#52b5aa";
    private static final String PRIMARY_COLOR_END = "#3d8a80";
    private static final String SECONDARY_COLOR_START = "#95a5a6";
    private static final String SECONDARY_COLOR_END = "#7f8c8d";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String BACKGROUND_COLOR = "#ecf0f1";
    
    // Font sizes
    private static final String TITLE_FONT_SIZE = "18px";
    private static final String HEADING_FONT_SIZE = "14px";
    private static final String BODY_FONT_SIZE = "14px";
    
    // Spacing
    private static final double DEFAULT_SPACING = 15.0;
    private static final Insets DEFAULT_PADDING = new Insets(15);
    private static final double CONTROL_PANEL_WIDTH = 300.0;
    
    /**
     * Styles a primary button with gradient teal background.
     */
    public static void stylePrimaryButton(Button btn) {
        btn.setStyle(String.format("""
            -fx-background-color: linear-gradient(to bottom, %s, %s);
            -fx-text-fill: white;
            -fx-font-size: %s;
            -fx-font-weight: bold;
            -fx-padding: 8 18;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """, PRIMARY_COLOR_START, PRIMARY_COLOR_END, BODY_FONT_SIZE));
    }
    
    /**
     * Styles a secondary button with gradient gray background.
     */
    public static void styleSecondaryButton(Button btn) {
        btn.setStyle(String.format("""
            -fx-background-color: linear-gradient(to bottom, %s, %s);
            -fx-text-fill: white;
            -fx-font-size: %s;
            -fx-font-weight: bold;
            -fx-padding: 8 18;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """, SECONDARY_COLOR_START, SECONDARY_COLOR_END, BODY_FONT_SIZE));
    }
    
    /**
     * Styles a title label (bold, larger font).
     */
    public static void styleTitleLabel(Label label) {
        label.setStyle(String.format("""
            -fx-font-weight: bold;
            -fx-font-size: %s;
            -fx-text-fill: %s;
        """, TITLE_FONT_SIZE, TEXT_COLOR));
    }
    
    /**
     * Styles a heading label.
     */
    public static void styleHeadingLabel(Label label) {
        label.setStyle(String.format("""
            -fx-font-weight: bold;
            -fx-font-size: %s;
            -fx-text-fill: %s;
        """, HEADING_FONT_SIZE, TEXT_COLOR));
    }
    
    /**
     * Styles a body label.
     */
    public static void styleBodyLabel(Label label) {
        label.setStyle(String.format("""
            -fx-font-size: %s;
            -fx-text-fill: %s;
        """, BODY_FONT_SIZE, TEXT_COLOR));
    }
    
    /**
     * Styles an info label.
     */
    public static void styleInfoLabel(Label label) {
        label.setStyle(String.format("""
            -fx-font-size: %s;
            -fx-text-fill: gray;
        """, BODY_FONT_SIZE));
    }
    
    /**
     * Styles a ComboBox with white background and border.
     */
    public static void styleComboBox(ComboBox<?> comboBox) {
        String css = String.format(
            ".styled-combobox {" +
            "    -fx-font-size: %1$s;" +
            "    -fx-background-color: white;" +
            "    -fx-border-color: %2$s;" +
            "    -fx-border-width: 1px;" +
            "    -fx-border-radius: 4px;" +
            "    -fx-background-radius: 4px;" +
            "}" +
            ".styled-combobox:hover {" +
            "    -fx-border-color: derive(%2$s, 20%%);" +
            "}" +
            ".styled-combobox:pressed {" +
            "    -fx-border-color: derive(%2$s, -10%%);" +
            "    -fx-background-color: derive(white, -5%%);" +
            "}",
            BODY_FONT_SIZE, PRIMARY_COLOR_START
        );
        
        String dataUri = "data:text/css;base64," + 
            Base64.getEncoder().encodeToString(css.getBytes());
        
        comboBox.getStylesheets().add(dataUri);
        comboBox.getStyleClass().add("styled-combobox");
    }
    
    /**
     * Styles a control panel VBox with background color, padding, and spacing.
     */
    public static void styleControlPanel(VBox panel) {
        panel.setSpacing(DEFAULT_SPACING);
        panel.setPadding(DEFAULT_PADDING);
        panel.setPrefWidth(CONTROL_PANEL_WIDTH);
        panel.setStyle(String.format("""
            -fx-background-color: %s;
        """, BACKGROUND_COLOR));
    }
    
    /**
     * Styles a Slider with modern minimal appearance.
     */
    public static void styleSlider(Slider slider) {
        String accentColor = "#E0E0E0";
        
        String css = String.format(
            ".minimal-slider .track {" +
            "    -fx-background-color: #E0E0E0;" +
            "    -fx-background-radius: 5px;" +
            "    -fx-pref-height: 4px;" +
            "}" +
            ".minimal-slider .track-before {" +
            "    -fx-background-color: %1$s;" +
            "    -fx-background-radius: 5px;" +
            "    -fx-pref-height: 4px;" +
            "}" +
            ".minimal-slider .thumb {" +
            "    -fx-background-color: %1$s;" +
            "    -fx-background-radius: 10px;" +
            "    -fx-pref-width: 16px;" +
            "    -fx-pref-height: 16px;" +
            "    -fx-border-color: %2$s;" +
            "    -fx-border-width: 1px;" +
            "    -fx-border-radius: 10px;" +
            "    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);" +
            "}" +
            ".minimal-slider .thumb:hover {" +
            "    -fx-background-color: derive(%1$s, 10%%);" +
            "}" +
            ".minimal-slider .thumb:pressed {" +
            "    -fx-background-color: derive(%1$s, -10%%);" +
            "    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);" +
            "}",
            accentColor, PRIMARY_COLOR_START
        );
        
        String dataUri = "data:text/css;base64," + 
            Base64.getEncoder().encodeToString(css.getBytes());
        
        slider.getStylesheets().add(dataUri);
        slider.getStyleClass().add("minimal-slider");
    }
    
    /**
     * Calculates bar color style based on value and max value.
     * Uses the same coloring logic as scatter plot overlap visualization.
     * Higher values result in darker teal colors, lower values in lighter teal.
     * 
     * @param value The current value to color
     * @param maxValue The maximum value for normalization
     * @return CSS style string for -fx-background-color
     */
    public static String calculateBarColorStyle(double value, double maxValue) {
        if (maxValue <= 0) {
            maxValue = 1.0; // Avoid division by zero
        }
        
        // Normalize value
        double normalizedValue = Math.min(value, maxValue) / maxValue;
        
        // Calculate brightness (inverse: higher value = darker)
        double brightness = 1.0 - normalizedValue;
        brightness = Math.max(0.2, Math.min(1.0, brightness));
        
        // Convert to RGB (using #52b5aa as base color, adjust brightness)
        int r = (int) (brightness * 82);
        int g = (int) (brightness * 181);
        int b = (int) (brightness * 170);
        
        return String.format("-fx-background-color: rgb(%d, %d, %d);", r, g, b);
    }
}
