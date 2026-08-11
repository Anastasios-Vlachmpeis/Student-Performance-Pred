# Student Performance Prediction & Analysis

A Java data-science project that analyzes fictional university grade data and builds grade-prediction models from scratch. The project combines exploratory statistics, rule-based learning, regression trees, and random forests with an interactive JavaFX dashboard for visualization and model exploration.

> **Getting started:** See [HOW_TO_RUN.md](HOW_TO_RUN.md) for setup and run instructions.

---

## What This Project Does

The application works with three related datasets about students enrolled in 36 sci-fi themed courses (e.g. Cryogenic Physics, Warp Field Theory, Xenorobotics Systems). It supports two complementary workflows:

1. **Console analysis** — phased assignment solutions that print statistics, correlations, decision rules, and predictions.
2. **Interactive GUI** — a tabbed "Graph Generator" for exploring grades, student demographics, course relationships, and live random-forest predictions.

There is no external ML library. All models, statistics, and charts are implemented in plain Java with JavaFX for rendering.

---

## Tech Stack

| Component | Details |
|-----------|---------|
| Language | Java 21 LTS |
| UI | JavaFX 21.0.9 |
| Build | Plain Java (no Maven/Gradle); IDE-driven |
| Data | CSV files loaded at class initialization |
| Dependencies | JDK standard library + JavaFX only |

---

## Project Structure

```
src/
├── Main.java                 # Console entry — runs all phase solutions
├── datamodels/               # CSV loaders, features, decision stumps
├── solutions/                # Phase 1–2 assignment implementations
├── regressionTree/           # Regression trees, random forests, evaluation
├── tools/                    # Statistics, Pearson correlation, chart helpers
└── GUI/                      # JavaFX application
    ├── GUIdemo.java          # GUI entry point
    ├── tab/                  # One tab controller per visualization
    ├── chart/                # Chart generation logic
    └── style/                # Shared UI styling
```

---

## Data

Three CSV files live under `src/datamodels/` and are loaded automatically via static initializers when their model classes are first accessed.

| Dataset | Model Class | Scale | Notes |
|---------|-------------|-------|-------|
| `GraduateGrades.csv` | `GraduateGradesModel` | ~21,243 students × 36 courses | Complete historical grades (6.0–10.0) |
| `CurrentGrades.csv` | `CurrentGradesModel` | ~1,521 students × 36 courses | In-progress grades; missing values encoded as **NG** (`-1.0`) |
| `StudentInfo.csv` | `StudentInfoModel` | ~1,521 students × 5 features | Mixed categorical and numerical student attributes |

### Student Features

Each student has five sci-fi themed attributes used as predictors in ML models:

| ID | Feature | Type |
|----|---------|------|
| 0 | Quantum Coherence Threshold (QC) | Categorical |
| 1 | Symbiotic Network Compatibility (SNC) | Categorical |
| 2 | Astro-Temporal Drift Resistance (ATDR) | Numerical |
| 3 | Psionic Interference Tolerance (PIT) | Numerical |
| 4 | Bio-Luminal Transmission (BLT) | Categorical |

Features are represented as `CategoricalFeature` or `NumericalFeature` objects and are also used as splitting criteria in decision stumps.

---

## Project Phases

The work is organized into incremental phases, each building on the previous one.

### Phase 1 — Graduate Grade Analysis

Exploratory analysis on complete historical data (`GraduateGradesModel`):

- Best and worst courses by mean grade
- Cum laude students (mean grade > 8)
- Top correlated course pairs (Pearson correlation)
- Students who perform better in hard courses than easy ones

Implemented in `solutions/Phase1Step1Methods.java`.

### Phase 2 — Current Grade Analysis

Same style of analysis adapted for in-progress grades with missing values (`CurrentGradesModel`):

- Hardest and easiest courses (NG-aware scoring)
- Students close to graduating (< 5 NGs, no failing grades)
- Correlated course pairs ignoring NG values
- Hard vs. easy performance analysis (NG-aware)
- **Monte Carlo simulation** to predict how many current students will graduate

Implemented in `solutions/Phase1Step2Methods.java` and partially in `solutions/Phase2Step2Methods.java`.

### Phase 3 — Decision Stumps

Rule-based grade prediction using single-feature splits:

- Tabulate grades by student feature
- Find the best split per course via **variance reduction**
- Predict future grades for courses marked NG

Implemented in `solutions/Phase1Step3.java` using `datamodels/DecisionStump.java`.

### Phase 4 — Ensemble Methods

Multiple strategies for combining decision stumps and trees:

| Method | Class | Approach |
|--------|-------|----------|
| Greedy variance reduction forest | `Phase1Step4VarianceReduction` | Greedily selects up to 10 stumps minimizing prediction variance |
| R² random search | `Phase1Step4R2Evaluation` | Samples 300 random 10-stump forests, keeps top performers by R² |
| Gradient boosting | `Phase1Step4GradientBoosting` | Iterative residual correction with learning rate η = 0.1 |
| Regression tree | `RegressionTreeTrainer` | Recursive variance-reduction splits (max depth 3, min samples 10) |
| Random forest | `regressionForest` | Bootstrap aggregation (70% samples per tree), average predictions |

Model quality is measured with **MSE**, **MAE**, and **R²** via `regressionTree/ModelEvaluator.java`.

---

## GUI — Graph Generator

Running `GUIdemo.java` opens a 1000×840 window with nine visualization tabs. Each tab has a left control panel and a center chart area.

| Tab | What it shows |
|-----|---------------|
| **Bar Chart** | Course or student statistics (mean, median, mode, NG count) with range filtering |
| **Scatter Plot** | Grade relationship between two courses, with overlap density coloring |
| **Joint Plot** | Scatter plot with marginal histograms for both axes |
| **Pearson Correlation Current** | Heat map of course–course correlations (current grades) |
| **Pearson Correlation Graduate** | Heat map of course–course correlations (graduate grades) |
| **Pie Chart** | Cum laude and predicted graduation percentages |
| **Histogram** | Binned distributions of grade metrics, with feature filtering |
| **Swarm Plot** | Grade distributions grouped by categorical student features |
| **Regression Forest** | Train N trees for a student/course pair and visualize prediction distribution |

Most tabs support filtering by student features (categorical match or above/below median for numerical features). The Regression Forest tab lets you configure tree count (10–1000), select a course, and enter a student ID to compare predicted vs. actual grade.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  GUI (tabs + chart generators)                              │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│  tools/ — Statistics, PearsonCorrelation, ChartDataUtils    │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
  datamodels/      solutions/      regressionTree/
  (CSV data)       (phase answers)   (ML models)
```

- **Data models** use static loading and expose data only through getter methods (encapsulation enforced by project convention).
- **Chart generators** (`GUI/chart/`) are separate from tab UI (`GUI/tab/`), keeping visualization logic reusable.
- **Shared controls** are built via `tools/ChartControlFactory.java`; styling is centralized in `GUI/style/UIStyling.java`.

---

## Data Model API

Access student data through the static model classes. Do not expose class variables as public — use the methods below.

### GraduateGradesModel

- `getGrade(int studentId, int courseId)` — grade for one student in one course
- `getAllGradesStudent(int studentId)` — all grades for a student
- `getAllGradesCourse(int courseId)` — all grades in a course
- `getCourseName(int courseId)` — course name by ID
- `getAllStudentIds()` — all student IDs
- `getCourses()` — all course names

### CurrentGradesModel

Same methods as above, plus:

- `getAllValidGradesStudent(int studentId)` — grades excluding NG (`-1.0`)
- `getAllValidGradesCourse(int courseId)` — valid grades in a course

### StudentInfoModel

- `getFeature(int studentId, int featureId)` — returns a `Feature` object (categorical or numerical)
- `getAllFeatures(int studentId)` — all features for a student
- `getAllStudentIds()` — all student IDs
- `getAllFeatureIds()` — all feature IDs

### Feature Objects

Two types:

1. **NumericalFeature** — has a feature ID and a numeric value; use `getRangeMin()` / `getRangeMax()` for valid bounds.
2. **CategoricalFeature** — has a feature ID and a category string; use `CategoricalFeature.getRange(featureId)` for all possible categories.

Features represent both student attributes and splitting criteria in decision stumps (e.g. "Psionic Interference Tolerance > 0.7").

### Decision Stumps

`DecisionStump` takes a `Feature` split criterion and two grade predictions (above/below split). Call `.predictGrade(int studentId)` to get a prediction for a student.

### Regression Trees & Forests

| Class | Key Methods |
|-------|-------------|
| `RegressionTreeTrainer` | `train(studentIds, courseId)` — build tree with default params; overload accepts `maxDepth` and `minSamples` |
| `TreeNode` | `predict(studentId)` — traverse tree; `printTree(prefix)` — debug output |
| `regressionForest` | `createRegressionForest(treeNumber, courseId, studentId)` — bagged ensemble prediction |
| `ModelEvaluator` | `evaluateTree(...)`, `evaluateForest(...)` — returns MSE, MAE, R² |

---

## Entry Points Summary

| Class | Package | Purpose |
|-------|---------|---------|
| `GUIdemo` | `GUI` | Launch JavaFX visualization dashboard |
| `Main` | *(default)* | Run all console phase solutions sequentially |
| `ModelEvaluator` | `regressionTree` | Batch-evaluate trees and forests across courses |
| `main` | `regressionTree` | Single-student forest prediction demo |

See [HOW_TO_RUN.md](HOW_TO_RUN.md) for setup details.

---

## Testing

There is no formal test suite (no JUnit/TestNG). Validation is done manually by running `main` methods and inspecting console output or the GUI. Key demo entry points include `ModelEvaluator.main`, `Phase1Step4VarianceReduction.testVarianceReductionForest`, and `Phase1Step4GradientBoosting.main`.
