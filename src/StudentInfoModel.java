import java.io.File;
import java.util.*;

/**
 * Reads CurrentGrades.csv, stores it internally, computes statistics
 * on itself via public methods.
 * As of now it is a static class, and meant to serve as an "interface"
 * for accessing student info data fields.
 */
public class StudentInfoModel {
    final static String pathToCSV = "src/StudentInfo.csv";
    final static int studentCount = 1522 - 1;
    final static int featureCount = 5;

    // an unconventional double array that stores all features of every student
    // indexed like this: features[studentIndex][featureIndex]
    static ArrayList<ArrayList> features = new ArrayList<>(studentCount);
    // links student id to its index in features[][]
    static HashMap<Integer, Integer> studentID2index = new HashMap<>(studentCount);

    // just to store the name of the features as well
    static String[] featureNames = new String[featureCount];

    // maps feature names and abbreviation of their names onto internal index used by this class
    static HashMap<String, Integer> featureName2Index = new HashMap<>();

    public static void loadCSV() {

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
            while (lineScanner.hasNext() && featureCounter < featureCount) {
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


            // if by mistake this method is run twice, this way we avoid redundant/double data
            features.clear();

            // Then, the code processes students line by line and load their various features
            // into the features "double array"
            int studentCounter = 0;
            while (fileScanner.hasNextLine()) {
                // Every line now starts with the student id, but that will be omitted.
                // This is because the first index in the 2D array serves as the

                // The second scanner is reused
                lineScanner = new Scanner(fileScanner.nextLine());
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

                // this feature is just a string (ENUM?)
                QC = lineScanner.next();

                // this feature is just a string (BOOLEAN?)
                SNC = lineScanner.next();

                // this feature is in the form "[number] ns/ms"
                // we extract only the number
                String rawFeature = lineScanner.next();
                ATDR = Double.parseDouble(rawFeature.split(" ")[0]);

                // this feature is just a number
                PIT = lineScanner.nextDouble();

                // this feature is just a string (ENUM?)
                // there is a trailing whitespace at the end which should be removed
                BLT = lineScanner.next().trim();

                // put student's features into the features "double array"
                features.add(new ArrayList<>(Arrays.asList(QC, SNC, ATDR, PIT, BLT)));


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

    public static Object getFeatureOfStudent(int studentId, int featureId) {
        return features.get(studentID2index.get(studentId)).get(featureId);
    }
    public static Object getFeatureOfStudent(int studentId, String featureName) {
        // do not check if feature name is valid, so misspelling is "caught" as a runtime error:3
        // (which should be fine as long as this code is used only
        // for analysing student data by group 28 in the mini project)
        return features.get(studentID2index.get(studentId)).get(featureName2Index.get(featureName));
    }

}
