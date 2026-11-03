package edu.uoc.correction.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Werner Nähle
 * @version 1.0
 */
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private String directoryStudentsPACs;
    private String directoryReportsPACs;
    private String directoryTestsPACs;
    private String name;
    TreeMap<String, ExerciceConfiguration> mapExercisesTests;
    private double scorePAC;

    public Configuration(){
        mapExercisesTests = new TreeMap<>();

        File configurationFile = new File(System.getProperty("user.dir")
                + "/src/main/resources/config.properties");
        try (Scanner scanner = new Scanner(configurationFile)) {
            parseConfiguration(scanner);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Could not open configuration file", e);
            throw new IllegalStateException("Could not open configuration file", e);
        }
    }

    private void parseConfiguration(Scanner scanner) {
        this.setDirectoryStudentsPACs(scanner.nextLine().split("=")[1].trim());
        this.setDirectoryReportsPACs(scanner.nextLine().split("=")[1].trim());
        this.setDirectoryTestsPACs(scanner.nextLine().split("=")[1].trim());
        this.setName(scanner.nextLine().split("=")[1].trim());

        double totalScore = parseExerciseConfigurations(scanner);
        this.setScorePAC(totalScore);
    }

    private double parseExerciseConfigurations(Scanner scanner) {
        String exerciseName = "";
        ScoreAccumulator totalScore = new ScoreAccumulator();
        Map<String, String> minimumRequirementProperties = new LinkedHashMap<>();
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.contains("minimumRequirements")) {
                parseMinimumRequirementProperty(line, minimumRequirementProperties);
                continue;
            }
            String[] testClassDefinitions = line.split(";");
            for(String testClassDefinition : testClassDefinitions){
                ParsedExerciseConfiguration parsedConfiguration =
                        parseExerciseConfiguration(testClassDefinition, exerciseName, totalScore);
                exerciseName = parsedConfiguration.exerciseName;
                ExerciceConfiguration existingConfiguration =
                        this.getMapExercisesTests().get(exerciseName);
                if (existingConfiguration == null) {
                    this.getMapExercisesTests().put(exerciseName, parsedConfiguration.configuration);
                } else {
                    existingConfiguration.getListTestNote()
                            .addAll(parsedConfiguration.configuration.getListTestNote());
                    existingConfiguration.setScoreByMethods(existingConfiguration.isScoreByMethods()
                            || parsedConfiguration.configuration.isScoreByMethods());
                }
            }
        }
        applyMinimumRequirementProperties(minimumRequirementProperties);
        return totalScore.value;
    }

    private void parseMinimumRequirementProperty(String line, Map<String, String> properties) {
        int assignment = line.indexOf('=');
        String key = assignment < 0 ? line.trim() : line.substring(0, assignment).trim();
        String value = assignment < 0 ? "" : line.substring(assignment + 1).trim();
        String suffix = ".minimumRequirements";
        if (assignment < 0 || !key.endsWith(suffix) || key.length() == suffix.length()) {
            throw invalidMinimumRequirements(key, value, "expected Exercise.minimumRequirements = Class#testName");
        }

        String exerciseName = key.substring(0, key.length() - suffix.length()).trim();
        if (properties.containsKey(exerciseName)) {
            throw invalidMinimumRequirements(exerciseName, value, "property is defined more than once");
        }
        properties.put(exerciseName, value);
    }

    private void applyMinimumRequirementProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String exerciseName = property.getKey();
            String value = property.getValue();
            ExerciceConfiguration exerciseConfiguration = mapExercisesTests.get(exerciseName);
            if (exerciseConfiguration == null) {
                throw invalidMinimumRequirements(exerciseName, value,
                        "exercise has no score definition");
            }

            List<MinimumRequirement> requirements = new ArrayList<>();
            if (!value.isEmpty()) {
                String[] items = value.split(",", -1);
                for (String item : items) {
                    String requirementText = item.trim();
                    int separator = requirementText.indexOf('#');
                    if (separator <= 0 || separator != requirementText.lastIndexOf('#')
                            || separator == requirementText.length() - 1) {
                        throw invalidMinimumRequirements(exerciseName, value,
                                "each test must use Class#testName");
                    }

                    String testClass = requirementText.substring(0, separator).trim();
                    String testName = requirementText.substring(separator + 1).trim();
                    if (testClass.isEmpty() || testName.isEmpty()) {
                        throw invalidMinimumRequirements(exerciseName, value,
                                "each test must include a class and test name");
                    }
                    if (!isScoredTest(exerciseConfiguration, testClass, testName)) {
                        throw invalidMinimumRequirements(exerciseName, value,
                                "test '" + requirementText
                                        + "' is not present in this exercise's score definition");
                    }
                    requirements.add(new MinimumRequirement(testClass, testName));
                }
            }
            exerciseConfiguration.setMinimumRequirements(requirements);
        }
    }

    private boolean isScoredTest(ExerciceConfiguration exerciseConfiguration,
                                 String testClass, String testName) {
        if (exerciseConfiguration.getListTestNote() == null) {
            return false;
        }
        for (TestNote testNote : exerciseConfiguration.getListTestNote()) {
            if (testClass.equals(testNote.getTestName())
                    && testNote.getMethodsNote() != null
                    && testNote.getMethodsNote().containsKey(testName)) {
                return true;
            }
        }
        return false;
    }

    private IllegalArgumentException invalidMinimumRequirements(String exerciseName,
                                                                 String value, String reason) {
        return new IllegalArgumentException("Invalid minimumRequirements for exercise "
                + exerciseName + " with value '" + value + "': " + reason);
    }

    private ParsedExerciseConfiguration parseExerciseConfiguration(String testClassDefinition,
                                                                     String currentExerciseName,
                                                                     ScoreAccumulator totalScore) {
        String[] testAndMethodDefinitions = testClassDefinition.split("#");
        String testDefinitionsText = testAndMethodDefinitions[0];
        String methodsText = testAndMethodDefinitions.length == 2
                ? testAndMethodDefinitions[1] : null;

        String exerciseName = currentExerciseName;
        String[] assignmentParts = testDefinitionsText.split("=");
        String[] testDefinitions;
        if(testDefinitionsText.contains("=")){
            exerciseName = assignmentParts[0].trim();
            testDefinitions = assignmentParts[1].split(",");
        }else{
            testDefinitions = assignmentParts[0].split(",");
        }

        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        List<TestNote> testNotes = new ArrayList<>();
        for(String testDefinition : testDefinitions){
            String[] testNoteParts = testDefinition.split(":");
            TestNote testNote = new TestNote();
            testNote.setTestName(testNoteParts[0].trim());
            testNote.setScore(Double.parseDouble(testNoteParts[1].trim()));

            if(methodsText != null){
                exerciseConfiguration.setScoreByMethods(true);
                String[] methodDefinitions = methodsText.split(",");
                for(String methodDefinition : methodDefinitions){
                    String methodName = methodDefinition.split(":")[0].trim();
                    Double methodScore = Double.parseDouble(methodDefinition.split(":")[1].trim());
                    testNote.getMethodsNote().put(methodName, methodScore);
                }
            }

            testNotes.add(testNote);
            totalScore.value += testNote.getScore();
        }
        exerciseConfiguration.setListTestNote(testNotes);
        return new ParsedExerciseConfiguration(exerciseName, exerciseConfiguration);
    }

    private static final class ScoreAccumulator {
        private double value;
    }

    private static final class ParsedExerciseConfiguration {
        private final String exerciseName;
        private final ExerciceConfiguration configuration;

        private ParsedExerciseConfiguration(String exerciseName, ExerciceConfiguration configuration) {
            this.exerciseName = exerciseName;
            this.configuration = configuration;
        }
    }

    public String getDirectoryStudentsPACs() {
        return directoryStudentsPACs;
    }

    public void setDirectoryStudentsPACs(String directoryStudentsPACs) {
        this.directoryStudentsPACs = directoryStudentsPACs;
    }

    public String getDirectoryReportsPACs() {
        return directoryReportsPACs;
    }

    public void setDirectoryReportsPACs(String directoryReportsPACs) {
        this.directoryReportsPACs = directoryReportsPACs;
    }

    public String getDirectoryTestsPACs() {
        return directoryTestsPACs;
    }

    public void setDirectoryTestsPACs(String directoryTestsPACs) {
        this.directoryTestsPACs = directoryTestsPACs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<String, ExerciceConfiguration> getMapExercisesTests() {
        return mapExercisesTests;
    }

    public double getScorePAC() {
        return scorePAC;
    }

    public void setScorePAC(double scorePAC) {
        this.scorePAC = scorePAC;
    }
}
