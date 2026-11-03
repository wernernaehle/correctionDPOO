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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

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
}
