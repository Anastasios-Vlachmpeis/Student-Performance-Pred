# How to Run

This project is from the GitLab repository of team winlog (#28).

## Requirements

- **Java 21 LTS**
- **JavaFX 21.0.9** (download the SDK separately; it is not bundled in this repo)

## Entry Points

| Application | Class | Purpose |
|-------------|-------|---------|
| **GUI (recommended)** | `src/GUI/GUIdemo.java` | Opens the JavaFX "Graph Generator" with all visualization tabs |
| **Console analysis** | `src/Main.java` | Runs Phase 1–3 analysis and prints results to stdout |
| **Model evaluation** | `src/regressionTree/ModelEvaluator.java` | Evaluates regression trees and random forests (MSE, MAE, R²) |
| **Forest demo** | `src/regressionTree/main.java` | Single-student random forest prediction demo |

## Running the GUI

1. Open the project in your IDE (IntelliJ or VS Code).
2. Set the run configuration to `GUI.GUIdemo` and its `main` method.
3. Add JavaFX VM options if your IDE does not resolve JavaFX automatically:

```
--module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml
```

4. Run from the **project root** so relative CSV paths such as `src/datamodels/CurrentGrades.csv` resolve correctly.

## Running Console Programs

Classes under `src/solutions/` and `src/regressionTree/` (except GUI-related code) run without JavaFX. Run `Main.java` or any class with a `main` method directly from your IDE.

## Troubleshooting

- **JavaFX not found:** Ensure the JavaFX SDK is on the module path and `javafx.controls` is added as a module.
- **CSV load errors:** Confirm you are running from the project root and that all three CSV files exist under `src/datamodels/` (`CurrentGrades.csv`, `GraduateGrades.csv`, `StudentInfo.csv`).
- **Locale issues:** The project sets `Locale.US` in entry points so decimal numbers parse correctly on systems that use comma separators.
