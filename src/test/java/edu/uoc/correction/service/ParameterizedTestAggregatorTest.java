package edu.uoc.correction.service;

import edu.uoc.correction.model.ExerciceConfiguration;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterizedTestAggregatorTest {

    private static final String CLASS_NAME = "edu.uoc.pac2.ExampleTest";

    @Test
    void groupsPassingParameterizedInvocationsByParentMethod() {
        Testcase first = testcase("[1] first");
        Testcase second = testcase("[2] second");
        Testsuite testsuite = testsuite(first, second);

        List<String> warnings = aggregate(testsuite, configuration(
                        note("ExampleTest", "loanRequest()", 0.06)),
                invocation("loanRequest(String)", "[1] first", "SUCCESS", "", ""),
                invocation("loanRequest(String)", "[2] second", "SUCCESS", "", ""));

        assertEquals(1, testsuite.getTestcase().size());
        Testcase grouped = testsuite.getTestcase().get(0);
        assertEquals("loanRequest()", grouped.getName());
        assertTrue(grouped.isGrouped());
        assertNull(grouped.getFailure());
        assertEquals(2, grouped.getInvocations().size());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void acceptsTypedSignatureWhenThatAliasIsConfigured() {
        Testsuite testsuite = testsuite(testcase("[1] first"));

        aggregate(testsuite, configuration(note("ExampleTest",
                        "loanRequest(String, double)", 0.06)),
                invocation("loanRequest(String, double)", "[1] first", "SUCCESS", "", ""));

        assertEquals("loanRequest(String, double)", testsuite.getTestcase().get(0).getName());
    }

    @Test
    void failedInvocationFailsTheWholeGroupedMethodAndRetainsItsMessage() {
        Testsuite testsuite = testsuite(testcase("[1] success"), testcase("[2] failure"));

        aggregate(testsuite, configuration(note("ExampleTest", "loanRequest()", 0.06)),
                invocation("loanRequest(String)", "[1] success", "SUCCESS", "", ""),
                invocation("loanRequest(String)", "[2] failure", "FAILURE",
                        "expected true but was false", "org.opentest4j.AssertionFailedError"));

        assertEquals(1, testsuite.getTestcase().size());
        Testcase grouped = testsuite.getTestcase().get(0);
        assertNotNull(grouped.getFailure());
        assertEquals("[2] failure", grouped.getInvocations().get(1).getName());
        assertEquals("expected true but was false",
                grouped.getInvocations().get(1).getFailure().getMessage());
    }

    @Test
    void usesParameterizedMethodDisplayNameAsConfigured() {
        Testsuite testsuite = testsuite(testcase("invocation label"));

        aggregate(testsuite, configuration(note("ExampleTest", "Loan request display name", 0.06)),
                invocation("Loan request display name", "invocation label", "SUCCESS", "", ""));

        assertEquals("Loan request display name", testsuite.getTestcase().get(0).getName());
        assertNull(testsuite.getTestcase().get(0).getFailure());
    }

    @Test
    void warnsForConfiguredMethodThatDidNotExecute() {
        Testsuite testsuite = testsuite(testcase("existingMethod()"));

        List<String> warnings = aggregate(testsuite, configuration(
                note("ExampleTest", "missingTest()", 0.2)));

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("missingTest()"));
        assertTrue(warnings.get(0).contains("ExampleTest"));
    }

    @Test
    void leavesOrdinaryTestsUnchanged() {
        Testcase ordinaryTest = testcase("isValidSampleCode");
        Testsuite testsuite = testsuite(ordinaryTest);

        aggregate(testsuite, configuration(note("ExampleTest", "isValidSampleCode", 0.4)));

        assertEquals(1, testsuite.getTestcase().size());
        assertTrue(testsuite.getTestcase().get(0) == ordinaryTest);
        assertFalse(ordinaryTest.isGrouped());
    }

    @Test
    void keepsMethodsWithIdenticalInvocationNamesUnderTheirOwnParents() {
        Testsuite testsuite = testsuite(testcase("[1] value"), testcase("[1] value"));

        aggregate(testsuite, configuration(note("ExampleTest",
                        "first()", 0.1, "second()", 0.2)),
                invocation("first(String)", "[1] value", "SUCCESS", "", ""),
                invocation("second(String)", "[1] value", "SUCCESS", "", ""));

        assertEquals(Arrays.asList("first()", "second()"),
                Arrays.asList(testsuite.getTestcase().get(0).getName(),
                        testsuite.getTestcase().get(1).getName()));
    }

    private List<String> aggregate(Testsuite testsuite, ExerciceConfiguration configuration,
                                  ParameterizedTestAggregator.InvocationResult... results) {
        return new ParameterizedTestAggregator().aggregate(
                testsuite, Arrays.asList(results), configuration);
    }

    private static Testsuite testsuite(Testcase... testcases) {
        Testsuite testsuite = new Testsuite();
        testsuite.setName(CLASS_NAME);
        testsuite.setTestcase(new ArrayList<>(Arrays.asList(testcases)));
        return testsuite;
    }

    private static Testcase testcase(String name) {
        Testcase testcase = new Testcase();
        testcase.setName(name);
        testcase.setClassname(CLASS_NAME);
        return testcase;
    }

    private static ExerciceConfiguration configuration(TestNote testNote) {
        ExerciceConfiguration configuration = new ExerciceConfiguration();
        configuration.setListTestNote(Collections.singletonList(testNote));
        configuration.setScoreByMethods(true);
        return configuration;
    }

    private static TestNote note(String className, Object... methodNamesAndScores) {
        TestNote testNote = new TestNote();
        testNote.setTestName(className);
        HashMap<String, Double> methodsNote = new HashMap<>();
        for (int i = 0; i < methodNamesAndScores.length; i += 2) {
            methodsNote.put((String) methodNamesAndScores[i], (Double) methodNamesAndScores[i + 1]);
        }
        testNote.setMethodsNote(methodsNote);
        testNote.setScore(methodsNote.values().stream().mapToDouble(Double::doubleValue).sum());
        return testNote;
    }

    private static ParameterizedTestAggregator.InvocationResult invocation(
            String methodName, String invocationName, String status, String message, String exceptionType) {
        return new ParameterizedTestAggregator.InvocationResult(
                CLASS_NAME, methodName, invocationName, status, message, exceptionType);
    }
}
