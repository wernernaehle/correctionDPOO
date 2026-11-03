package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.Failure;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testcase;
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

class ParameterizedTestReportTest {

    @Test
    void showsTheFailingInvocationAndAwardsNoPoints(@TempDir Path temporaryDirectory) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());
        configuration.setName("PAC2");
        configuration.setScorePAC(0.06);

        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(0.06);
        HashMap<String, Double> methodNotes = new HashMap<>();
        methodNotes.put("loanRequest()", 0.06);
        testNote.setMethodsNote(methodNotes);
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Testcase failedInvocation = new Testcase();
        failedInvocation.setName("[2] failing invocation");
        failedInvocation.setFailure(new Failure("expected true but was false",
                "org.opentest4j.AssertionFailedError"));
        Testcase groupedTestcase = new Testcase();
        groupedTestcase.setName("loanRequest()");
        groupedTestcase.setGrouped(true);
        groupedTestcase.setFailure(new Failure("One or more invocations did not pass.",
                "InvocationFailure"));
        groupedTestcase.setInvocations(Collections.singletonList(failedInvocation));

        Testsuite testsuite = new Testsuite();
        testsuite.setName("edu.uoc.pac2.ExampleTest");
        testsuite.setTests(2);
        testsuite.setFailures(1);
        testsuite.setErrors(0);
        testsuite.setScore(0.06);
        testsuite.setTestcase(Collections.singletonList(groupedTestcase));

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Collections.singletonList(testsuite));
        Student student = new Student();
        student.setName("student_PEC_example");
        student.setExercises(new ArrayList<>(Collections.singletonList(exercise)));
        Path studentReportDirectory = temporaryDirectory.resolve(student.getName());
        Files.createDirectories(studentReportDirectory);

        new ResultService(configuration).html(Collections.singletonList(student));

        String report = new String(Files.readAllBytes(studentReportDirectory.resolve("index.html")),
                StandardCharsets.UTF_8);
        assertTrue(report.contains("loanRequest()"));
        assertTrue(report.contains("[2] failing invocation"));
        assertTrue(report.contains("expected true but was false"));
        assertTrue(report.contains(">0.0</th>"));
    }
}
