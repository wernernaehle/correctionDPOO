package edu.uoc.correction.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Locale;
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
    private String javadocExerciseTitle;
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
        Map<String, Map<String, String>> javadocProperties = new LinkedHashMap<>();
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("javadoc.exercise.title")) {
                parseJavadocExerciseTitle(line);
                continue;
            }
            if (line.contains(".javadoc.")) {
                parseJavadocProperty(line, javadocProperties);
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
        totalScore.value += applyJavadocProperties(javadocProperties);
        boolean javadocEnabled = mapExercisesTests.values().stream()
                .map(ExerciceConfiguration::getJavadocConfiguration)
                .anyMatch(javadoc -> javadoc != null && javadoc.isEnabled());
        if (javadocEnabled && (javadocExerciseTitle == null || javadocExerciseTitle.trim().isEmpty())) {
            throw invalidJavadoc("javadoc.exercise.title", "(missing)",
                    "a title is required when Javadoc grading is enabled");
        }
        if (javadocExerciseTitle != null && mapExercisesTests.containsKey(javadocExerciseTitle)) {
            throw invalidJavadoc("javadoc.exercise.title", javadocExerciseTitle,
                    "the title must not duplicate a configured exercise");
        }
        return totalScore.value;
    }

    private void parseJavadocExerciseTitle(String line) {
        int assignment = line.indexOf('=');
        String key = assignment < 0 ? line.trim() : line.substring(0, assignment).trim();
        String value = assignment < 0 ? "" : line.substring(assignment + 1).trim();
        if (!"javadoc.exercise.title".equals(key) || assignment < 0) {
            throw invalidJavadoc(key, value, "expected javadoc.exercise.title = exercise name");
        }
        if (javadocExerciseTitle != null) {
            throw invalidJavadoc(key, value, "property is defined more than once");
        }
        if (value.isEmpty()) {
            throw invalidJavadoc(key, value, "title must not be empty");
        }
        javadocExerciseTitle = value;
    }

    private void parseJavadocProperty(String line, Map<String, Map<String, String>> properties) {
        int assignment = line.indexOf('=');
        String key = assignment < 0 ? line.trim() : line.substring(0, assignment).trim();
        String value = assignment < 0 ? "" : line.substring(assignment + 1).trim();
        int marker = key.indexOf(".javadoc.");
        if (assignment < 0 || marker <= 0) {
            throw invalidJavadoc(key, value, "expected Exercise.javadoc.property = value");
        }

        String exerciseName = key.substring(0, marker);
        String propertyName = key.substring(marker + ".javadoc.".length());
        if (propertyName.isEmpty()) {
            throw invalidJavadoc(key, value, "property name is missing");
        }
        Map<String, String> exerciseProperties =
                properties.computeIfAbsent(exerciseName, ignored -> new LinkedHashMap<>());
        if (exerciseProperties.putIfAbsent(propertyName, value) != null) {
            throw invalidJavadoc(exerciseName + ".javadoc." + propertyName, value,
                    "property is defined more than once");
        }
    }

    private double applyJavadocProperties(Map<String, Map<String, String>> properties) {
        double maximumScore = 0;
        for (Map.Entry<String, Map<String, String>> entry : properties.entrySet()) {
            String exerciseName = entry.getKey();
            Map<String, String> values = entry.getValue();
            ExerciceConfiguration exerciseConfiguration = mapExercisesTests.get(exerciseName);
            if (exerciseConfiguration == null) {
                throw invalidJavadoc(exerciseName + ".javadoc.enabled",
                        values.getOrDefault("enabled", "false"), "exercise has no score definition");
            }

            String enabledValue = values.get("enabled");
            boolean enabled = false;
            if (enabledValue != null) {
                if (!"true".equalsIgnoreCase(enabledValue) && !"false".equalsIgnoreCase(enabledValue)) {
                    throw invalidJavadoc(exerciseName + ".javadoc.enabled", enabledValue,
                            "expected true or false");
                }
                enabled = Boolean.parseBoolean(enabledValue.toLowerCase(Locale.ROOT));
            }
            validateJavadocPropertyNames(exerciseName, values);
            if (!enabled) {
                continue;
            }

            String classesValue = values.get("classes");
            if (classesValue == null || classesValue.trim().isEmpty()) {
                throw invalidJavadoc(exerciseName + ".javadoc.classes",
                        classesValue == null ? "(missing)" : classesValue, "class list must not be empty");
            }
            EnumSet<JavadocElementType> include = parseJavadocInclude(exerciseName, values.get("include"));
            String referencePath = values.get("referencePath");
            if (referencePath != null && referencePath.trim().isEmpty()) {
                throw invalidJavadoc(exerciseName + ".javadoc.referencePath", referencePath,
                        "reference path must not be empty");
            }

            Double defaultPresence = parseJavadocScore(exerciseName + ".javadoc.presenceScore",
                    values.get("presenceScore"));
            Double defaultQuality = parseJavadocScore(exerciseName + ".javadoc.qualityMaxScore",
                    values.get("qualityMaxScore"));
            List<JavadocClassConfiguration> classConfigurations = new ArrayList<>();
            String[] classNames = classesValue.split(",", -1);
            List<String> configuredClassNames = new ArrayList<>();
            for (String value : classNames) {
                String className = value.trim();
                if (className.isEmpty()) {
                    throw invalidJavadoc(exerciseName + ".javadoc.classes", classesValue,
                            "class list contains an empty class name");
                }
                Double presence = parseJavadocScore(exerciseName + ".javadoc." + className + ".presenceScore",
                        values.get(className + ".presenceScore"));
                Double quality = parseJavadocScore(exerciseName + ".javadoc." + className + ".qualityMaxScore",
                        values.get(className + ".qualityMaxScore"));
                if (presence == null) {
                    presence = requireJavadocScore(exerciseName + ".javadoc.presenceScore", defaultPresence);
                }
                if (quality == null) {
                    quality = requireJavadocScore(exerciseName + ".javadoc.qualityMaxScore", defaultQuality);
                }
                if (classConfigurations.stream().anyMatch(item -> item.getClassName().equals(className))) {
                    throw invalidJavadoc(exerciseName + ".javadoc.classes", classesValue,
                            "class '" + className + "' is listed more than once");
                }
                configuredClassNames.add(className);
                classConfigurations.add(new JavadocClassConfiguration(
                        className, presence, quality, include, referencePath));
            }
            validateJavadocClassOverrides(exerciseName, values, configuredClassNames);
            JavadocExerciseConfiguration javadocConfiguration =
                    new JavadocExerciseConfiguration(true, classConfigurations);
            exerciseConfiguration.setJavadocConfiguration(javadocConfiguration);
            maximumScore += javadocConfiguration.getMaximumScore();
        }
        return maximumScore;
    }

    private void validateJavadocClassOverrides(String exerciseName, Map<String, String> properties,
                                               List<String> configuredClasses) {
        for (String propertyName : properties.keySet()) {
            if (!propertyName.endsWith(".presenceScore")
                    && !propertyName.endsWith(".qualityMaxScore")) {
                continue;
            }
            String className = propertyName.substring(0, propertyName.lastIndexOf('.'));
            if (!configuredClasses.contains(className)) {
                throw invalidJavadoc(exerciseName + ".javadoc." + propertyName,
                        properties.get(propertyName), "class-specific score is not listed in javadoc.classes");
            }
        }
    }

    private void validateJavadocPropertyNames(String exerciseName, Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String name = property.getKey();
            if ("enabled".equals(name) || "classes".equals(name) || "presenceScore".equals(name)
                    || "qualityMaxScore".equals(name) || "include".equals(name)
                    || "referencePath".equals(name)) {
                continue;
            }
            if (name.endsWith(".presenceScore") || name.endsWith(".qualityMaxScore")) {
                String className = name.substring(0, name.lastIndexOf('.'));
                String baseProperty = name.substring(name.lastIndexOf('.') + 1);
                if (!className.isEmpty() && ("presenceScore".equals(baseProperty)
                        || "qualityMaxScore".equals(baseProperty))) {
                    continue;
                }
            }
            throw invalidJavadoc(exerciseName + ".javadoc." + name, property.getValue(),
                    "unknown property");
        }
    }

    private EnumSet<JavadocElementType> parseJavadocInclude(String exerciseName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return EnumSet.allOf(JavadocElementType.class);
        }
        EnumSet<JavadocElementType> include = EnumSet.noneOf(JavadocElementType.class);
        for (String item : value.split(",", -1)) {
            String type = item.trim().toLowerCase(Locale.ROOT);
            JavadocElementType elementType;
            switch (type) {
                case "class":
                    elementType = JavadocElementType.CLASS;
                    break;
                case "fields":
                    elementType = JavadocElementType.FIELDS;
                    break;
                case "constructors":
                    elementType = JavadocElementType.CONSTRUCTORS;
                    break;
                case "publicmethods":
                    elementType = JavadocElementType.PUBLIC_METHODS;
                    break;
                case "privatemethods":
                    elementType = JavadocElementType.PRIVATE_METHODS;
                    break;
                default:
                    throw invalidJavadoc(exerciseName + ".javadoc.include", value,
                            "unknown element type '" + item.trim() + "'");
            }
            if (!include.add(elementType)) {
                throw invalidJavadoc(exerciseName + ".javadoc.include", value,
                        "element type '" + item.trim() + "' is listed more than once");
            }
        }
        return include;
    }

    private Double parseJavadocScore(String property, String value) {
        if (value == null) {
            return null;
        }
        try {
            double score = Double.parseDouble(value);
            if (!Double.isFinite(score) || score < 0) {
                throw invalidJavadoc(property, value, "score must be a finite number greater than or equal to 0");
            }
            return score;
        } catch (NumberFormatException e) {
            throw invalidJavadoc(property, value, "score must be numeric");
        }
    }

    private double requireJavadocScore(String property, Double value) {
        if (value == null) {
            throw invalidJavadoc(property, "(missing)", "a score is required for every configured class");
        }
        return value;
    }

    private IllegalArgumentException invalidJavadoc(String property, String value, String reason) {
        return new IllegalArgumentException("Invalid Javadoc configuration for property '" + property
                + "' with value '" + value + "': " + reason);
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

    public String getJavadocExerciseTitle() {
        return javadocExerciseTitle;
    }

    public void setJavadocExerciseTitle(String javadocExerciseTitle) {
        this.javadocExerciseTitle = javadocExerciseTitle;
    }
}
