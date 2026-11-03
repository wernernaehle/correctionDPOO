package edu.uoc.correction.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
        while(scanner.hasNext()) {
            String[] testClassDefinitions = scanner.nextLine().split(";");
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
        return totalScore.value;
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
