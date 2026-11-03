package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.Failure;
import edu.uoc.correction.model.MinimumRequirement;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testsuite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultServiceHtmlCharacterizationTest {

    @Test
    void rendersTheExpectedScoreAndExerciseTotals(@TempDir Path temporaryDirectory) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());
        configuration.setName("PAC2");
        configuration.setScorePAC(1.0);

        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(1.0);
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Testcase testcase = new Testcase();
        testcase.setName("testExample()");
        Testsuite testSuite = new Testsuite();
        testSuite.setName("edu.uoc.pac2.ExampleTest");
        testSuite.setTests(4);
        testSuite.setFailures(1);
        testSuite.setErrors(0);
        testSuite.setScore(1.0);
        testSuite.setTestcase(Collections.singletonList(testcase));

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Collections.singletonList(testSuite));
        Student student = new Student();
        student.setName("learner_PEC_example");
        student.setExercises(new ArrayList<>(Collections.singletonList(exercise)));

        Path studentReportDirectory = temporaryDirectory.resolve(student.getName());
        Files.createDirectories(studentReportDirectory);
        new ResultService(configuration).html(Collections.singletonList(student));

        String report = new String(Files.readAllBytes(studentReportDirectory.resolve("index.html")),
                StandardCharsets.UTF_8);
        assertTrue(report.contains("learner"));
        assertTrue(report.contains(">0.75</span> sobre 1.0"));
        assertTrue(report.contains("<th scope=\"col\">4</th>"));
        assertTrue(report.contains("<th scope=\"col\">1</th>"));
    }

    @Test
    void showsFailedMinimumRequirementsAndZerosOnlyTheExerciseTotal(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());
        configuration.setName("PAC2");
        configuration.setScorePAC(1.0);

        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(1.0);
        HashMap<String, Double> methodNotes = new HashMap<>();
        methodNotes.put("ordinary()", 0.5);
        methodNotes.put("Required C", 0.5);
        methodNotes.put("Required D", 0.0);
        testNote.setMethodsNote(methodNotes);
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        exerciseConfiguration.setMinimumRequirements(java.util.Arrays.asList(
                new MinimumRequirement("ExampleTest", "Required C"),
                new MinimumRequirement("ExampleTest", "Required D")));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Testcase ordinary = new Testcase();
        ordinary.setName("ordinary()");
        Testcase required = new Testcase();
        required.setName("Required C");
        required.setFailure(new Failure("org.opentest4j.AssertionFailedError: failed",
                "org.opentest4j.AssertionFailedError"));
        Testcase otherRequired = new Testcase();
        otherRequired.setName("Required D");
        Testsuite testSuite = new Testsuite();
        testSuite.setName("edu.uoc.pac2.ExampleTest");
        testSuite.setTests(3);
        testSuite.setFailures(1);
        testSuite.setErrors(0);
        testSuite.setScore(1.0);
        testSuite.setTestcase(java.util.Arrays.asList(ordinary, required, otherRequired));

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Collections.singletonList(testSuite));
        Student student = new Student();
        student.setName("learner_PEC_minimum");
        student.setExercises(Collections.singletonList(exercise));
        Path studentReportDirectory = temporaryDirectory.resolve(student.getName());
        Files.createDirectories(studentReportDirectory);

        new ResultService(configuration).html(Collections.singletonList(student));

        String report = new String(Files.readAllBytes(studentReportDirectory.resolve("index.html")),
                StandardCharsets.UTF_8);
        assertTrue(report.contains("class=\"text-danger\">No se cumplen los requisitos mínimos "
                + "del ejercicio PAC2Ex1."));
        assertTrue(report.contains("Tests obligatorios no superados:"));
        assertTrue(report.contains("ExampleTest#Required C"));
        int failuresMessageStart = report.indexOf("Tests obligatorios no superados:");
        int failuresMessageEnd = report.indexOf("</span>", failuresMessageStart);
        String failuresMessage = report.substring(failuresMessageStart, failuresMessageEnd);
        assertTrue(!failuresMessage.contains("ExampleTest#Required D"));
        assertTrue(report.contains("ordinary()"));
        assertTrue(report.contains("Required C"));
        assertTrue(report.contains(">0.0</th>"));
    }

    @Test
    void compileFailureShowsOnlyTheExistingCompileMessage(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());
        configuration.setName("PAC2");
        configuration.setScorePAC(1.0);
        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.getMethodsNote().put("required()", 1.0);
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        exerciseConfiguration.setMinimumRequirements(Collections.singletonList(
                new MinimumRequirement("ExampleTest", "required()")));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        Student student = new Student();
        student.setName("learner_PEC_compile");
        student.setExercises(Collections.singletonList(exercise));
        Path studentReportDirectory = temporaryDirectory.resolve(student.getName());
        Files.createDirectories(studentReportDirectory);

        new ResultService(configuration).html(Collections.singletonList(student));

        String report = new String(Files.readAllBytes(studentReportDirectory.resolve("index.html")),
                StandardCharsets.UTF_8);
        assertTrue(report.contains("Ejercicio no compila o No Presentado"));
        assertTrue(!report.contains("No se cumplen los requisitos mínimos"));
    }
}
