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
