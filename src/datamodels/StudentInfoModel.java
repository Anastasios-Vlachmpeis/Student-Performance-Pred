package datamodels;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * DataModel class for the student info dataset. Implements the following methods
 * to access feature data of students:
 *      - getFeature(int StudentId, int featureId)
 *      - getAllFeatures(int StudentId)
 *      - getAllStudentIds()
 */
public class StudentInfoModel {
    final static String pathToCSV = "src/datamodels/StudentInfo.csv";
    final static int studentCount = 1522 - 1;
    final static int featureCount = 5;

    // an unconventional double array that stores all features of every student
    // indexed like this: features[studentIndex][featureIndex]
    private static AbstractFeature[][] features = new AbstractFeature[studentCount][featureCount];
    // links student id to its index in features[][]
    static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);

    // just to store the name of the features as well
    static String[] featureNames = new String[featureCount];
    // maps feature names and abbreviation of their names onto internal index used by this class
    static HashMap<String, Integer> featureName2Index = new HashMap<>();

    // stores the ranges of possible values for each feature. internal index is used
    public static ArrayList<ArrayList> featureRanges = new ArrayList<>(5);

    // ensure .csv is loaded before DataModel class is accessed
    static {loadCSV();}
    private static void loadCSV() {

        try {
            System.out.println("Start reading file: " + pathToCSV);  // Debug
            System.out.println("This will take a while...");        // Debug

            File file = new File(pathToCSV);

            // This code uses two Scanners, one which scans the file line per line
            Scanner fileScanner = new Scanner(file);
            int linesDone = 0;

            // and one that scans the line entry per entry using the commas as delimiters
            Scanner lineScanner = new Scanner(fileScanner.nextLine());
            lineScanner.useDelimiter(",");

            // Since first line of GraduateGrades.csv is only the feature names, the code process it separately
            int featureCounter = 0;
            while (lineScanner.hasNext()) {
                String s = lineScanner.next().trim();
                // The entry "StudentID" is a placholder so it is skipped
                if (!s.equals("StudentID")) {
                    featureNames[featureCounter] = s;
                    // adds full featureName to mapping
                    featureName2Index.put(s, featureCounter);
                    // adds abbreviation of featureName to mapping
                    String sAbbreviated = "";
                    for (char c : s.toCharArray()) {
                        if (Character.isUpperCase(c)) {
                            sAbbreviated += c;
                        }
                    }
                    featureName2Index.put(sAbbreviated, featureCounter);
                    featureCounter++;
                }
            }
            linesDone++;

            // reset feature ranges
            featureRanges.clear();
            featureRanges.add(new ArrayList<String>());
            featureRanges.add(new ArrayList<String>());
            featureRanges.add(new ArrayList<Double>(Arrays.asList(0.0, 0.0)));
            featureRanges.add(new ArrayList<Double>(Arrays.asList(0.0, 0.0)));
            featureRanges.add(new ArrayList<String>());


            // Then, the code processes students line by line and load their various features
            // into the features "double array"
            int studentCounter = 0;
            while (fileScanner.hasNextLine()) {
                // Every line now starts with the student id, but that will be omitted.
                // This is because the first index in the 2D array serves as the

                // The second scanner is reused
                lineScanner = new Scanner(fileScanner.nextLine().trim());
                lineScanner.useDelimiter(",");


                // we know that there is the student id followed by exactly 5 features in every line
                int studentId;  // the global id of given student
                String QC;      // Quantum Coherence Threshold (Chaotic Coherent Fractured Resonant Stable)
                String SNC;     // Symbiotic Network Compatibility (Harmonized None)
                double ATDR;    // Astro-Temporal Drift Resistance (1 2 3)
                double PIT;     // Psionic Interference Tolerance (range between -1 and 1 inclusive/exclusive?)
                String BLT;     // Bio-Luminal Transmission (Crimson Silver Turquoise Violet White Blue)

                // map global student id to local feature representation's indexing
                studentId = lineScanner.nextInt();
                studentID2index.put(studentId, studentCounter);

                // this feature is just a string (category)
                QC = lineScanner.next();
                CategoricalFeature QCNew = new CategoricalFeature(0, QC);
                features[studentCounter][0] = QCNew;

                // this feature is just a string (category)
                SNC = lineScanner.next();
                CategoricalFeature SNCNew = new CategoricalFeature(1, SNC);
                features[studentCounter][1] = SNCNew;

                // this feature is in the form "[number] ns/ms"
                // we extract only the number
                String rawFeature = lineScanner.next();
                ATDR = Double.parseDouble(rawFeature.split(" ")[0]);
                NumericalFeature ATDRNew = new NumericalFeature(2, ATDR);
                features[studentCounter][2] = ATDRNew;

                // this feature is just a number
                PIT = lineScanner.nextDouble();
                NumericalFeature PITNew = new NumericalFeature(3, PIT);
                features[studentCounter][3] = PITNew;

                // this feature is just a string (category)
                // there is a trailing whitespace at the end which should be removed
                BLT = lineScanner.next().trim();
                CategoricalFeature BLTNew = new CategoricalFeature(3, BLT);
                features[studentCounter][4] = BLTNew;

                // update feature ranges if necessary
                // first the ENUM like categories that are stored as string
                if (!featureRanges.get(0).contains(QC)) {featureRanges.get(0).add(QC);}
                if (!featureRanges.get(1).contains(SNC)) {featureRanges.get(1).add(SNC);}
                if (!featureRanges.get(4).contains(BLT)) {featureRanges.get(4).add(BLT);}
                // then update real values properties
                if (ATDR < (double)featureRanges.get(2).getFirst()) {
                    featureRanges.get(2).set(0, ATDR);  // real value ranges have only 2 element
                } else if (ATDR > (double)featureRanges.get(2).getLast()) {
                    featureRanges.get(2).set(1, ATDR);  // real value ranges have only 2 element
                }
                if (PIT < (double)featureRanges.get(3).getFirst()) {
                    featureRanges.get(3).set(0, PIT);
                } else if (PIT > (double)featureRanges.get(3).getLast()) {
                    featureRanges.get(3).set(1, PIT);
                }

                // move to next student and terminate current line's scanning
                studentCounter++;
                linesDone++;
                lineScanner.close();
            }

            // Prevent memory leaks by closing fileScanner
            fileScanner.close();
            System.out.println("Finished reading: " + pathToCSV);    // DEBUG

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //====================//
    // DATA MODEL METHODS //
    //====================//
    public static AbstractFeature getFeature(int studentId, int featureId) {
        return features[studentID2index.get(studentId)][featureId];
    }
    public static AbstractFeature getFeature(int studentId, String featureName) {
        // do not check if feature name is valid, so misspelling is "caught" as a runtime error:3
        // (which should be fine as long as this code is used only
        // for analysing student data by group 28 in the mini project)
        int studentIndex = studentID2index.get(studentId);
        int featureIndex = featureName2Index.get(featureName);
        return features[studentIndex][featureIndex];
    }
    public static AbstractFeature[] getAllFeatures(int studentId) {
        // converting student id into local indexing
        return features[studentID2index.get(studentId)];
    }
    public static int[] getAllStudentIds() {
        int[] studentIds = new int[studentCount];
        int i = 0;
        for (int studentId : studentID2index.keySet()) {
            studentIds[i++] = studentId;
        }
        return studentIds;
    }
}
