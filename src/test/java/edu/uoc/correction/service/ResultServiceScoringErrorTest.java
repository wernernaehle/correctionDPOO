package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testsuite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultServiceScoringErrorTest {

    @Test
    void continuesReportGenerationWhenMethodScoreIsMissing(@TempDir Path temporaryDirectory) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());
        configuration.setName("PAC2");
        configuration.setScorePAC(1.0);

        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.getMethodsNote().put("differentMethod()", 1.0);
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Testcase testcase = new Testcase();
        testcase.setName("unmappedMethod()");
        Testsuite testSuite = new Testsuite();
        testSuite.setName("edu.uoc.pac2.ExampleTest");
        testSuite.setTests(1);
        testSuite.setFailures(0);
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

        assertTrue(Files.exists(studentReportDirectory.resolve("index.html")));
        String report = new String(Files.readAllBytes(studentReportDirectory.resolve("index.html")),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(report.contains(">0.0</span> sobre 1.0"));
    }

    @Test
    void incompleteTestCountsDoNotAbortReportGeneration(@TempDir Path temporaryDirectory) throws IOException {
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

        Testsuite testSuite = new Testsuite();
        testSuite.setName("edu.uoc.pac2.ExampleTest");
        testSuite.setScore(1.0);
        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Collections.singletonList(testSuite));
        Student student = new Student();
        student.setName("learner_PEC_incomplete");
        student.setExercises(new ArrayList<>(Collections.singletonList(exercise)));

        Path studentReportDirectory = temporaryDirectory.resolve(student.getName());
        Files.createDirectories(studentReportDirectory);
        new ResultService(configuration).html(Collections.singletonList(student));

        assertTrue(Files.exists(studentReportDirectory.resolve("index.html")));
        String report = new String(Files.readAllBytes(studentReportDirectory.resolve("index.html")),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(report.contains(">0.0</span> sobre 1.0"));
    }
}
