package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.Failure;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.Testsuite;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculationServiceTest {

    @Test
    void deductsTheFailedParameterizedMethodFromTheClassScore() {
        Configuration configuration = new Configuration();
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(1.5);
        HashMap<String, Double> methodNotes = new HashMap<>();
        methodNotes.put("loanRequest()", 0.06);
        methodNotes.put("eligibility()", 0.03);
        methodNotes.put("loanable()", 0.06);
        methodNotes.put("categories()", 0.04);
        methodNotes.put("insuredValues()", 0.04);
        methodNotes.put("durations()", 0.04);
        methodNotes.put("dates()", 0.03);
        methodNotes.put("mixedRequests()", 0.2);
        methodNotes.put("emptyRequests()", 0.2);
        methodNotes.put("statistics()", 0.2);
        methodNotes.put("nullArrays()", 0.2);
        methodNotes.put("invalidElements()", 0.2);
        methodNotes.put("statisticsDates()", 0.2);
        testNote.setMethodsNote(methodNotes);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        List<Testcase> testcases = new ArrayList<>();
        for (String methodName : methodNotes.keySet()) {
            Testcase testcase = new Testcase();
            testcase.setName(methodName);
            if ("loanRequest()".equals(methodName)) {
                testcase.setFailure(new Failure(
                        "One or more invocations did not pass.", "InvocationFailure"));
            }
            testcases.add(testcase);
        }
        Testsuite testsuite = new Testsuite();
        testsuite.setName("edu.uoc.pac2.ExampleTest");
        testsuite.setTestcase(testcases);

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Collections.singletonList(testsuite));

        assertEquals(1.44, new ScoreCalculationService(configuration)
                .calculateStudentScore(Collections.singletonList(exercise), new ArrayList<>()), 0.0000001);
    }

    @Test
    void scoresMethodsOnlyAgainstTheirOwnTestClass() {
        Configuration configuration = new Configuration();
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        exerciseConfiguration.setListTestNote(Arrays.asList(
                note("FirstTest", "sharedMethod()", 0.1),
                note("SecondTest", "sharedMethod()", 0.9)));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Testcase testcase = new Testcase();
        testcase.setName("sharedMethod()");
        Testsuite testsuite = new Testsuite();
        testsuite.setName("edu.uoc.pac2.FirstTest");
        testsuite.setTestcase(Arrays.asList(testcase));

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Arrays.asList(testsuite));

        assertEquals(0.1, new ScoreCalculationService(configuration)
                .calculateStudentScore(Arrays.asList(exercise), new ArrayList<>()));
    }

    @Test
    void preservesTestCountScoringForClassesWithoutMethodScores() {
        Configuration configuration = new Configuration();
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setScoreByMethods(true);
        TestNote methodScoredNote = note("MethodScoredTest", "configuredMethod()", 0.1);
        TestNote countScoredNote = new TestNote();
        countScoredNote.setTestName("CountScoredTest");
        countScoredNote.setScore(1.0);
        exerciseConfiguration.setListTestNote(Arrays.asList(methodScoredNote, countScoredNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        Testcase countScoredTestcase = new Testcase();
        countScoredTestcase.setName("ordinaryMethod");
        Testsuite countScoredTestsuite = new Testsuite();
        countScoredTestsuite.setName("edu.uoc.pac2.CountScoredTest");
        countScoredTestsuite.setTests(2);
        countScoredTestsuite.setFailures(0);
        countScoredTestsuite.setErrors(0);
        countScoredTestsuite.setScore(1.0);
        countScoredTestsuite.setTestcase(Collections.singletonList(countScoredTestcase));

        Testcase methodScoredTestcase = new Testcase();
        methodScoredTestcase.setName("configuredMethod()");
        Testsuite methodScoredTestsuite = new Testsuite();
        methodScoredTestsuite.setName("edu.uoc.pac2.MethodScoredTest");
        methodScoredTestsuite.setTestcase(Collections.singletonList(methodScoredTestcase));

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setTestsuiteList(Arrays.asList(countScoredTestsuite, methodScoredTestsuite));

        assertEquals(1.1, new ScoreCalculationService(configuration)
                .calculateStudentScore(Collections.singletonList(exercise), new ArrayList<>()));
    }

    private TestNote note(String className, String methodName, double methodScore) {
        TestNote testNote = new TestNote();
        testNote.setTestName(className);
        HashMap<String, Double> methodsNote = new HashMap<>();
        methodsNote.put(methodName, methodScore);
        testNote.setMethodsNote(methodsNote);
        return testNote;
    }
}
