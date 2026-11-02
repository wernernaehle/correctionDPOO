package edu.uoc.correction.service;

import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.Testsuite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterizedTestAggregatorTest {

    private static final String CLASS_NAME = "edu.uoc.pac2.ExampleTest";

    @Test
    void groupsAllPassingParameterizedInvocationsUnderTypedMethodAlias() {
        Testcase first = testcase("[1] first");
        Testcase second = testcase("[2] second");
        Testcase displayNamedTest = testcase("isValidSampleCode");
        Testsuite suite = testsuite(first, second, displayNamedTest);
        ExerciceConfiguration configuration = configuration(note("ExampleTest",
                "testLoanRequest()", 0.06, "isValidSampleCode", 0.4));

        ParameterizedTestAggregator.AggregationResult result = new ParameterizedTestAggregator().aggregate(
                suite, Arrays.asList(
                        invocation("testLoanRequest(String, double)", "[1] first", "SUCCESS", "", ""),
                        invocation("testLoanRequest(String, double)", "[2] second", "SUCCESS", "", ""),
                        invocation("", "isValidSampleCode", "SUCCESS", "", "")
                ), configuration);

        assertEquals(2, suite.getTestcase().size());
                Testcase grouped = suite.getTestcase().get(0);
                assertEquals("testLoanRequest()", grouped.getName());
                assertTrue(grouped.isGrouped());
                assertNull(grouped.getFailure());
                assertEquals(2, grouped.getInvocations().size());
                assertEquals("isValidSampleCode", suite.getTestcase().get(1).getName());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void failsParameterizedMethodWhenAnyInvocationFails() {
        Testcase first = testcase("[1] success");
        Testcase second = testcase("[2] failure");
        Testsuite suite = testsuite(first, second);
        ExerciceConfiguration configuration = configuration(note("ExampleTest",
                "testLoanRequest()", 0.06));

        new ParameterizedTestAggregator().aggregate(suite, Arrays.asList(
                invocation("testLoanRequest(String)", "[1] success", "SUCCESS", "", ""),
                invocation("testLoanRequest(String)", "[2] failure", "FAILURE",
                        "expected true but was false", "org.opentest4j.AssertionFailedError")
        ), configuration);

        assertEquals(1, suite.getTestcase().size());
        Testcase grouped = suite.getTestcase().get(0);
        assertEquals("testLoanRequest()", grouped.getName());
        assertNotNull(grouped.getFailure());
        assertNull(grouped.getInvocations().get(0).getFailure());
        assertEquals("[2] failure", grouped.getInvocations().get(1).getName());
        assertEquals("expected true but was false",
                grouped.getInvocations().get(1).getFailure().getMessage());
    }

    @Test
    void acceptsJUnitDefaultTypedMethodNameAsConfiguredAlias() {
        Testsuite suite = testsuite(testcase("[1] value"));
        ExerciceConfiguration configuration = configuration(note("ExampleTest",
                "testLoanRequest(String, double)", 0.06));

        new ParameterizedTestAggregator().aggregate(suite, Arrays.asList(
                invocation("testLoanRequest(String, double)", "[1] value", "SUCCESS", "", "")
        ), configuration);

        assertEquals("testLoanRequest(String, double)", suite.getTestcase().get(0).getName());
        assertNull(suite.getTestcase().get(0).getFailure());
    }

    @Test
    @DisplayName("Uses the parameterized method's DisplayName")
    void resolvesParameterizedDisplayNameExactlyAsConfigured() throws NoSuchMethodException {
        Testsuite suite = testsuite(testcase("invocation label"));
        String displayName = DisplayNamedFixture.class.getDeclaredMethod("loanRequest", String.class)
                .getAnnotation(DisplayName.class).value();
        ExerciceConfiguration configuration = configuration(note("ExampleTest",
                displayName, 0.06));

        new ParameterizedTestAggregator().aggregate(suite, Arrays.asList(
                invocation(displayName, "invocation label", "SUCCESS", "", "")
        ), configuration);

        assertEquals(displayName, suite.getTestcase().get(0).getName());
        assertNull(suite.getTestcase().get(0).getFailure());
    }

    @Test
    void leavesOrdinaryDisplayNamedTestUnchanged() {
        Testcase displayNamedTest = testcase("isValidSampleCode");
        Testsuite suite = testsuite(displayNamedTest);
        ExerciceConfiguration configuration = configuration(note("ExampleTest",
                "isValidSampleCode", 0.4));

        new ParameterizedTestAggregator().aggregate(suite, new ArrayList<>(), configuration);

        assertEquals(1, suite.getTestcase().size());
        assertEquals("isValidSampleCode", suite.getTestcase().get(0).getName());
        assertFalse(suite.getTestcase().get(0).isGrouped());
        assertTrue(suite.getTestcase().get(0) == displayNamedTest);
    }

    @Test
    void warnsWhenConfiguredMethodDoesNotExistInResults() {
        Testsuite suite = testsuite(testcase("existingMethod()"));
        ExerciceConfiguration configuration = configuration(note("ExampleTest",
                "existingMethod()", 0.1, "missingMethod()", 0.2));

        ParameterizedTestAggregator.AggregationResult result =
                new ParameterizedTestAggregator().aggregate(suite, new ArrayList<>(), configuration);

        assertEquals(1, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("missingMethod()"));
    }

    private static Testsuite testsuite(Testcase... testcases) {
        Testsuite suite = new Testsuite();
        suite.setName(CLASS_NAME);
        suite.setTestcase(new ArrayList<>(Arrays.asList(testcases)));
        return suite;
    }

    private static Testcase testcase(String name) {
        Testcase testcase = new Testcase();
        testcase.setName(name);
        testcase.setClassname(CLASS_NAME);
        return testcase;
    }

    private static ExerciceConfiguration configuration(TestNote testNote) {
        ExerciceConfiguration configuration = new ExerciceConfiguration();
        configuration.setListTestNote(Arrays.asList(testNote));
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
        return testNote;
    }

    private static ParameterizedTestAggregator.InvocationResult invocation(
            String methodName, String invocationName, String status, String message, String exceptionType) {
        return new ParameterizedTestAggregator.InvocationResult(
                CLASS_NAME, methodName, invocationName, status, message, exceptionType);
    }

    private static class DisplayNamedFixture {
        @DisplayName("Loan request display name")
        void loanRequest(String input) {}
    }
}
