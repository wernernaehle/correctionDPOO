package edu.uoc.correction.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationCharacterizationTest {

    @Test
    void parsesExerciseScoresAndMethodScores(@TempDir Path temporaryDirectory) throws IOException {
        Configuration configuration = loadConfiguration(temporaryDirectory,
                "deliveries=/students\n"
                        + "reports=/reports\n"
                        + "tests=/tests\n"
                        + "name=PAC2\n"
                        + "PAC2Ex1=Alpha:1.0, Beta:2.5#methodA:0.25, methodB:0.75\n"
                        + "PAC2Ex2=Gamma:3.0\n"
                        + "PAC2Ex3=Delta:4.0\n");

        assertEquals("/students", configuration.getDirectoryStudentsPACs());
        assertEquals("/reports", configuration.getDirectoryReportsPACs());
        assertEquals("/tests", configuration.getDirectoryTestsPACs());
        assertEquals("PAC2", configuration.getName());
        assertEquals(10.5, configuration.getScorePAC());
        assertEquals(10.0, configuration.getCanvasGradeMultiplier());
        assertEquals(Arrays.asList("PAC2Ex1", "PAC2Ex2", "PAC2Ex3"),
                new ArrayList<>(configuration.getMapExercisesTests().keySet()));

        ExerciceConfiguration firstExercise = configuration.getMapExercisesTests().get("PAC2Ex1");
        assertEquals(Arrays.asList("Alpha", "Beta"),
                firstExercise.getListTestNote().stream().map(TestNote::getTestName).collect(Collectors.toList()));
        assertEquals(0.25, firstExercise.getListTestNote().get(0).getMethodsNote().get("methodA"));
        assertEquals(0.75, firstExercise.getListTestNote().get(1).getMethodsNote().get("methodB"));
        assertEquals(4.0,
                configuration.getMapExercisesTests().get("PAC2Ex3").getListTestNote().get(0).getScore());
        assertEquals("Gamma",
                configuration.getMapExercisesTests().get("PAC2Ex2").getListTestNote().get(0).getTestName());
    }

    @Test
    void parsesCanvasGradeMultiplierWithoutChangingPacScore(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = loadConfiguration(temporaryDirectory,
                "deliveries=/students\n"
                        + "reports=/reports\n"
                        + "tests=/tests\n"
                        + "name=PAC2\n"
                        + "PAC2Ex1=ExampleTest:10.0\n"
                        + "canvas.grade.multiplier = 20\n");

        assertEquals(20.0, configuration.getCanvasGradeMultiplier());
        assertEquals(10.0, configuration.getScorePAC());
    }

    @Test
    void mergesMultipleTestClassesConfiguredForOneExercise(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = loadConfiguration(temporaryDirectory,
                "deliveries=/students\n"
                        + "reports=/reports\n"
                        + "tests=/tests\n"
                        + "name=PAC2\n"
                        + "PAC2Ex1=FirstTest:1.0#first():1.0\n"
                        + "PAC2Ex1=SecondTest:2.0#second():2.0\n");

        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get("PAC2Ex1");
        assertEquals(2, exerciseConfiguration.getListTestNote().size());
        assertEquals(3.0, configuration.getScorePAC());
    }

    @Test
    void parsesMinimumRequirementsUsingConfiguredDisplayAndMethodNames(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = loadConfiguration(temporaryDirectory,
                "deliveries=/students\n"
                        + "reports=/reports\n"
                        + "tests=/tests\n"
                        + "name=PAC2\n"
                        + "PAC2Ex1=DisplayTest:1.0#Readable display name:0.5, parameterized():0.5\n"
                        + "PAC2Ex1.minimumRequirements = DisplayTest#Readable display name, "
                        + "DisplayTest#parameterized()\n");

        List<MinimumRequirement> requirements = configuration.getMapExercisesTests()
                .get("PAC2Ex1").getMinimumRequirements();
        assertEquals(2, requirements.size());
        assertEquals("DisplayTest#Readable display name", requirements.get(0).getDisplayName());
        assertEquals("DisplayTest#parameterized()", requirements.get(1).getDisplayName());
    }

    @Test
    void acceptsAnEmptyMinimumRequirementsProperty(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = loadConfiguration(temporaryDirectory,
                "deliveries=/students\n"
                        + "reports=/reports\n"
                        + "tests=/tests\n"
                        + "name=PAC2\n"
                        + "PAC2Ex1=ExampleTest:1.0\n"
                        + "PAC2Ex1.minimumRequirements = \n");

        assertTrue(configuration.getMapExercisesTests().get("PAC2Ex1")
                .getMinimumRequirements().isEmpty());
    }

    @Test
    void rejectsMinimumRequirementNotPresentInExerciseScoreDefinition(@TempDir Path temporaryDirectory)
            throws IOException {
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> loadConfiguration(temporaryDirectory,
                        "deliveries=/students\n"
                                + "reports=/reports\n"
                                + "tests=/tests\n"
                                + "name=PAC2\n"
                                + "PAC2Ex1=ExampleTest:1.0#configured():1.0\n"
                                + "PAC2Ex1.minimumRequirements = ExampleTest#missing()\n"));

        assertTrue(failure.getMessage().contains("PAC2Ex1"));
        assertTrue(failure.getMessage().contains("ExampleTest#missing()"));
        assertTrue(failure.getMessage().contains("not present"));
    }

    @Test
    void rejectsMalformedMinimumRequirement(@TempDir Path temporaryDirectory) throws IOException {
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> loadConfiguration(temporaryDirectory,
                        "deliveries=/students\n"
                                + "reports=/reports\n"
                                + "tests=/tests\n"
                                + "name=PAC2\n"
                                + "PAC2Ex1=ExampleTest:1.0#configured():1.0\n"
                                + "PAC2Ex1.minimumRequirements = ExampleTest#\n"));

        assertTrue(failure.getMessage().contains("PAC2Ex1"));
        assertTrue(failure.getMessage().contains("Class#testName"));
    }

    @Test
    void parsesJavadocPropertiesAndAddsTheirMaximumToThePacScore(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = loadConfiguration(temporaryDirectory,
                "deliveries=/students\n"
                        + "reports=/reports\n"
                        + "tests=/tests\n"
                        + "name=PAC2\n"
                        + "javadoc.exercise.title=PAC2Ex3\n"
                        + "PAC2Ex2=ExampleTest:1.0#configured():1.0\n"
                        + "PAC2Ex2.javadoc.enabled = true\n"
                        + "PAC2Ex2.javadoc.classes = MuseumArtifact, OtherArtifact\n"
                        + "PAC2Ex2.javadoc.presenceScore = 0.5\n"
                        + "PAC2Ex2.javadoc.qualityMaxScore = 0.5\n"
                        + "PAC2Ex2.javadoc.include = class, fields, publicMethods\n"
                        + "PAC2Ex2.javadoc.MuseumArtifact.presenceScore = 0.75\n");

        JavadocExerciseConfiguration javadoc = configuration.getMapExercisesTests()
                .get("PAC2Ex2").getJavadocConfiguration();
        assertTrue(javadoc.isEnabled());
        assertEquals(2, javadoc.getClasses().size());
        assertEquals(0.75, javadoc.getClasses().get(0).getPresenceScore());
        assertEquals(0.5, javadoc.getClasses().get(1).getPresenceScore());
        assertEquals(0.5, javadoc.getClasses().get(0).getQualityMaxScore());
        assertEquals(3, javadoc.getClasses().get(0).getInclude().size());
        assertEquals(3.25, configuration.getScorePAC());
        assertEquals("PAC2Ex3", configuration.getJavadocExerciseTitle());
    }

    @Test
    void rejectsNonNumericJavadocScoreWithPropertyAndValue(@TempDir Path temporaryDirectory)
            throws IOException {
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> loadConfiguration(temporaryDirectory,
                        "deliveries=/students\n"
                                + "reports=/reports\n"
                                + "tests=/tests\n"
                                + "name=PAC2\n"
                                + "PAC2Ex2=ExampleTest:1.0\n"
                                + "PAC2Ex2.javadoc.enabled=true\n"
                                + "PAC2Ex2.javadoc.classes=Example\n"
                                + "PAC2Ex2.javadoc.presenceScore=not-a-number\n"
                                + "PAC2Ex2.javadoc.qualityMaxScore=0.5\n"));

        assertTrue(failure.getMessage().contains("PAC2Ex2.javadoc.presenceScore"));
        assertTrue(failure.getMessage().contains("not-a-number"));
    }

    @Test
    void rejectsJavadocScoreWithoutListedClass(@TempDir Path temporaryDirectory) throws IOException {
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> loadConfiguration(temporaryDirectory,
                        "deliveries=/students\n"
                                + "reports=/reports\n"
                                + "tests=/tests\n"
                                + "name=PAC2\n"
                                + "PAC2Ex2=ExampleTest:1.0\n"
                                + "PAC2Ex2.javadoc.enabled=true\n"
                                + "PAC2Ex2.javadoc.classes=Example\n"
                                + "PAC2Ex2.javadoc.presenceScore=0.5\n"
                                + "PAC2Ex2.javadoc.qualityMaxScore=0.5\n"
                                + "PAC2Ex2.javadoc.Other.presenceScore=1.0\n"));

        assertTrue(failure.getMessage().contains("PAC2Ex2.javadoc.Other.presenceScore"));
        assertTrue(failure.getMessage().contains("not listed"));
    }

    @Test
    void rejectsInvalidJavadocBooleanEmptyClassListNegativeScoreAndUnknownInclude(
            @TempDir Path temporaryDirectory) throws IOException {
        String header = "deliveries=/students\n"
                + "reports=/reports\n"
                + "tests=/tests\n"
                + "name=PAC2\n"
                + "PAC2Ex2=ExampleTest:1.0\n";
        String valid = "PAC2Ex2.javadoc.enabled=true\n"
                + "PAC2Ex2.javadoc.classes=Example\n"
                + "PAC2Ex2.javadoc.presenceScore=0.5\n"
                + "PAC2Ex2.javadoc.qualityMaxScore=0.5\n";
        String[][] invalidConfigurations = {
                {"PAC2Ex2.javadoc.enabled=maybe\n" + valid.substring(valid.indexOf('\n') + 1),
                        "PAC2Ex2.javadoc.enabled"},
                {"PAC2Ex2.javadoc.enabled=true\nPAC2Ex2.javadoc.classes= \n"
                        + "PAC2Ex2.javadoc.presenceScore=0.5\n"
                        + "PAC2Ex2.javadoc.qualityMaxScore=0.5\n", "PAC2Ex2.javadoc.classes"},
                {valid.replace("qualityMaxScore=0.5", "qualityMaxScore=-0.1"),
                        "PAC2Ex2.javadoc.qualityMaxScore"},
                {valid + "PAC2Ex2.javadoc.include=methods\n", "PAC2Ex2.javadoc.include"}
        };

        for (String[] invalidConfiguration : invalidConfigurations) {
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> loadConfiguration(temporaryDirectory, header + invalidConfiguration[0]));
            assertTrue(failure.getMessage().contains(invalidConfiguration[1]));
        }
    }

    @Test
    void parsesQualityConfigurationAndKeepsItsPenaltyOutsideMaximumScore(
                @TempDir Path temporaryDirectory) throws IOException {
            Path reference = temporaryDirectory.resolve("reference");
            Files.createDirectories(reference);
            Files.write(reference.resolve("Sample.java"), "class Sample {}".getBytes(StandardCharsets.UTF_8));

            Configuration configuration = loadConfiguration(temporaryDirectory,
                    "deliveries=/students\nreports=/reports\ntests=/tests\nname=PAC2\n"
                            + "PAC2Ex1=ExampleTest:3.0\n"
                            + "quality.weights=duplication:1,deadCode:1,complexity:1,javaIdioms:1,naming:1\n"
                            + "quality.violationsForFullCategoryPenalty=4\n"
                            + "PAC2Ex1.quality.enabled=true\n"
                            + "PAC2Ex1.quality.maxPenalty=1.5\n"
                            + "PAC2Ex1.quality.classes=Sample\n"
                            + "PAC2Ex1.quality.referencePath=" + reference + "\n"
                            + "PAC2Ex1.quality.disabledRules=UNUSED_IMPORT\n");

            QualityExerciseConfiguration quality = configuration.getMapExercisesTests().get("PAC2Ex1")
                    .getQualityConfiguration();
            assertEquals(1.5, quality.getMaxPenalty());
            assertEquals(4, quality.getViolationsForFullCategoryPenalty());
            assertEquals(1.0, quality.getWeights().get(QualityCategory.JAVA_IDIOMS));
            assertTrue(quality.getDisabledRules().contains(QualityRule.UNUSED_IMPORT));
            assertEquals(3.0, configuration.getScorePAC());
        }

    @Test
    void rejectsInvalidQualitySettingsWithPropertyAndValue(@TempDir Path temporaryDirectory)
            throws IOException {
            Path reference = temporaryDirectory.resolve("reference");
            Files.createDirectories(reference);
            Files.write(reference.resolve("Sample.java"), "class Sample {}".getBytes(StandardCharsets.UTF_8));
            String header = "deliveries=/students\nreports=/reports\ntests=/tests\nname=PAC2\n"
                    + "PAC2Ex1=ExampleTest:1.0\n";
            String valid = "PAC2Ex1.quality.enabled=true\nPAC2Ex1.quality.maxPenalty=1\n"
                    + "PAC2Ex1.quality.classes=Sample\nPAC2Ex1.quality.referencePath=" + reference + "\n";
            String[][] invalid = {
                    {valid.replace("enabled=true", "enabled=yes"), "PAC2Ex1.quality.enabled"},
                    {valid.replace("maxPenalty=1", "maxPenalty=0"), "PAC2Ex1.quality.maxPenalty"},
                    {valid.replace("classes=Sample\n", "classes=Missing\n"), "PAC2Ex1.quality.classes"},
                    {valid.replace(reference.toString(), temporaryDirectory.resolve("missing").toString()),
                            "PAC2Ex1.quality.referencePath"},
                    {valid + "PAC2Ex1.quality.disabledRules=UNKNOWN_RULE\n",
                            "PAC2Ex1.quality.disabledRules"},
                    {valid + "quality.weights=duplication:nope\n", "quality.weights"}
            };
            for (String[] entry : invalid) {
                IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                        () -> loadConfiguration(temporaryDirectory, header + entry[0]));
                assertTrue(failure.getMessage().contains(entry[1]), failure.getMessage());
        }
    }

    @Test
    void emptyConfigurationRetainsScannerFailure(@TempDir Path temporaryDirectory) throws IOException {
        NoSuchElementException failure = assertThrows(NoSuchElementException.class,
                () -> loadConfiguration(temporaryDirectory, ""));

        assertEquals("No line found", failure.getMessage());
    }

    @Test
    void missingConfigurationFailsWithAUsefulCause(@TempDir Path temporaryDirectory) {
        String originalUserDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", temporaryDirectory.toString());
            IllegalStateException failure = assertThrows(IllegalStateException.class, Configuration::new);
            assertEquals("Could not open configuration file", failure.getMessage());
            assertTrue(failure.getCause() instanceof java.io.FileNotFoundException);
        } finally {
            System.setProperty("user.dir", originalUserDirectory);
        }
    }

    @Test
    void invalidScoreRetainsNumberFormatFailure(@TempDir Path temporaryDirectory) throws IOException {
        NumberFormatException failure = assertThrows(NumberFormatException.class,
                () -> loadConfiguration(temporaryDirectory,
                        "deliveries=/students\nreports=/reports\ntests=/tests\nname=PAC2\n"
                                + "PAC2Ex1=Alpha:invalid\n"));

        assertEquals("For input string: \"invalid\"", failure.getMessage());
    }

    private Configuration loadConfiguration(Path temporaryDirectory, String contents) throws IOException {
        Path resourceDirectory = temporaryDirectory.resolve("src/main/resources");
        Files.createDirectories(resourceDirectory);
        Files.write(resourceDirectory.resolve("config.properties"),
                contents.getBytes(StandardCharsets.UTF_8));

        String originalUserDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", temporaryDirectory.toString());
            return new Configuration();
        } finally {
            System.setProperty("user.dir", originalUserDirectory);
        }
    }
}
