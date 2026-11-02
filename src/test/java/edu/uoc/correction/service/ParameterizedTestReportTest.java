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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterizedTestReportTest {

    @Test
    void rendersGroupedFailureWithInvocationNameAndMessage(@TempDir Path temporaryDirectory) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());
        configuration.setName("PAC2");
        configuration.setScorePAC(0.06);

        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        HashMap<String, Double> methodNotes = new HashMap<>();
        methodNotes.put("testSingleLoanRequest()", 0.06);
        methodNotes.put("testLoanEligibilityForEveryCategory()", 0.03);
        methodNotes.put("testSingleRequestRequiresLoanableArtifact()", 0.06);
        methodNotes.put("testSingleRequestRejectsInvalidCategories()", 0.04);
        methodNotes.put("testSingleRequestRejectsInvalidInsuredValues()", 0.04);
        methodNotes.put("testSingleRequestRejectsInvalidDurations()", 0.04);
        methodNotes.put("testSingleRequestRejectsInvalidDatesBeforeCheckingEligibility()", 0.03);
        methodNotes.put("testMixedRequestsAndFractionalPercentage()", 0.2);
        methodNotes.put("testAllNoneSingleAndEmptyRequests()", 0.2);
        methodNotes.put("testStatisticsReflectLoanConditions()", 0.2);
        methodNotes.put("testStatisticsRejectEachNullArrayAndLengthMismatch()", 0.2);
        methodNotes.put("testStatisticsRejectInvalidElementsAtEveryPosition()", 0.2);
        methodNotes.put("testStatisticsValidateDatesIncludingEmptyArrays()", 0.2);
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
        groupedTestcase.setName("testSingleLoanRequest()");
        groupedTestcase.setGrouped(true);
        groupedTestcase.setFailure(new Failure("One or more invocations did not pass.",
                "InvocationFailure"));
        groupedTestcase.setInvocations(Collections.singletonList(failedInvocation));

        ArrayList<Testcase> testcases = new ArrayList<>();
        testcases.add(groupedTestcase);
        for (String methodName : methodNotes.keySet()) {
            if (!"testSingleLoanRequest()".equals(methodName)) {
                Testcase passingTest = new Testcase();
                passingTest.setName(methodName);
                passingTest.setClassname("edu.uoc.pac2.ExampleTest");
                testcases.add(passingTest);
            }
        }

        Testsuite testsuite = new Testsuite();
        testsuite.setName("edu.uoc.pac2.ExampleTest");
        testsuite.setTests(31);
        testsuite.setFailures(1);
        testsuite.setErrors(0);
        testsuite.setScore(1.5);
        testsuite.setTestcase(testcases);

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setReportUrl("./PAC2Ex1/index.html");
        exercise.setTestsuiteList(Collections.singletonList(testsuite));

        Student student = new Student();
        student.setName("student_PEC_example");
        student.setExercises(new ArrayList<>(Collections.singletonList(exercise)));
        Files.createDirectory(temporaryDirectory.resolve(student.getName()));

        new ResultService(configuration).html(Collections.singletonList(student));

        String report = new String(Files.readAllBytes(
                temporaryDirectory.resolve(student.getName()).resolve("index.html")),
                StandardCharsets.UTF_8);
        assertTrue(report.contains("testSingleLoanRequest()"));
        assertTrue(report.contains("[0.06 puntos]"));
        assertTrue(report.contains("[2] failing invocation"));
        assertTrue(report.contains("expected true but was false"));
        assertTrue(report.contains(">1.44</th>"));
    }
}
