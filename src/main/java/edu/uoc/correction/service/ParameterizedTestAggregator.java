package edu.uoc.correction.service;

import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Failure;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.Testsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ParameterizedTestAggregator {

    private static final Logger LOGGER = Logger.getLogger(ParameterizedTestAggregator.class.getName());

    List<InvocationResult> readInvocationResults(File resultFile) {
        if (!resultFile.isFile()) {
            return Collections.emptyList();
        }

        List<InvocationResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(resultFile), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\t", -1);
                if (fields.length != 6) {
                    throw new IOException("Invalid grouped test result at line " + lineNumber);
                }
                results.add(new InvocationResult(
                        decode(fields[0]), decode(fields[1]), decode(fields[2]), fields[3],
                        decode(fields[4]), decode(fields[5])));
            }
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "WARN: Could not read grouped test results from "
                    + resultFile.getAbsolutePath() + "; invocation grouping will be skipped", e);
            return Collections.emptyList();
        }
        return results;
    }

    List<String> aggregate(Testsuite testsuite, List<InvocationResult> invocationResults,
                           ExerciceConfiguration configuration) {
        List<TestNote> testNotes = classNotes(testsuite, configuration);
        List<Testcase> originalTestcases = testsuite.getTestcase() == null
                ? Collections.emptyList() : testsuite.getTestcase();
        List<InvocationResult> classResults = new ArrayList<>();
        for (InvocationResult result : invocationResults) {
            if (testsuite.getName().equals(result.className)) {
                classResults.add(result);
            }
        }

        Map<String, Testcase> groupedTestcases = new LinkedHashMap<>();
        Map<String, List<Testcase>> groupedInvocations = new LinkedHashMap<>();
        Map<Testcase, String> groupByOriginalTestcase = new HashMap<>();
        if (classResults.size() == originalTestcases.size()) {
            for (int i = 0; i < originalTestcases.size(); i++) {
                assignGroup(originalTestcases.get(i), classResults.get(i), testsuite,
                        testNotes, groupedTestcases, groupedInvocations, groupByOriginalTestcase);
            }
        } else if (!classResults.isEmpty()) {
            LOGGER.warning("WARN: Could not align JUnit listener events with XML results for "
                    + testsuite.getName() + "; invocation grouping was skipped");
            return missingConfiguredNames(originalTestcases, testNotes);
        } else {
            return missingConfiguredNames(originalTestcases, testNotes);
        }

        if (groupByOriginalTestcase.isEmpty()) {
            return missingConfiguredNames(originalTestcases, testNotes);
        }

        List<Testcase> aggregatedTestcases = new ArrayList<>();
        for (Testcase testcase : originalTestcases) {
            String scoreName = groupByOriginalTestcase.get(testcase);
            if (scoreName == null) {
                aggregatedTestcases.add(testcase);
            } else if (groupedTestcases.get(scoreName).getInvocations() == null) {
                Testcase groupedTestcase = groupedTestcases.get(scoreName);
                List<Testcase> invocations = groupedInvocations.get(scoreName);
                groupedTestcase.setInvocations(invocations);
                if (invocations.stream().anyMatch(invocation -> invocation.getFailure() != null)) {
                    groupedTestcase.setFailure(new Failure(
                            "One or more invocations did not pass.", "InvocationFailure"));
                }
                aggregatedTestcases.add(groupedTestcase);
            }
        }

        testsuite.setTestcase(aggregatedTestcases);
        return missingConfiguredNames(testcases(testsuite), testNotes);
    }

    private void assignGroup(Testcase testcase, InvocationResult result, Testsuite testsuite,
                             List<TestNote> testNotes, Map<String, Testcase> groupedTestcases,
                             Map<String, List<Testcase>> groupedInvocations,
                             Map<Testcase, String> groupByOriginalTestcase) {
        if (result.methodName.isEmpty()) {
            return;
        }

        String scoreName = resolveScoreName(result.methodName, testNotes);
        groupedTestcases.computeIfAbsent(scoreName, name -> groupedTestcase(name, testsuite.getName()));
        groupedInvocations.computeIfAbsent(scoreName, ignored -> new ArrayList<>())
                .add(invocationTestcase(testcase, result));
        groupByOriginalTestcase.put(testcase, scoreName);
    }

    private List<TestNote> classNotes(Testsuite testsuite, ExerciceConfiguration configuration) {
        if (configuration == null || configuration.getListTestNote() == null) {
            return Collections.emptyList();
        }
        String className = simpleClassName(testsuite.getName());
        List<TestNote> classNotes = new ArrayList<>();
        for (TestNote testNote : configuration.getListTestNote()) {
            if (className.equals(testNote.getTestName())) {
                classNotes.add(testNote);
            }
        }
        return classNotes;
    }

    private List<String> missingConfiguredNames(List<Testcase> testcases, List<TestNote> testNotes) {
        List<String> warnings = new ArrayList<>();
        Map<String, Boolean> executedNames = new HashMap<>();
        for (Testcase testcase : testcases) {
            executedNames.put(testcase.getName(), Boolean.TRUE);
        }
        for (TestNote testNote : testNotes) {
            if (testNote.getMethodsNote() == null) {
                continue;
            }
            for (String configuredName : testNote.getMethodsNote().keySet()) {
                if (!executedNames.containsKey(configuredName)) {
                    String normalizedName = normalizeTypedMethodName(configuredName);
                    if (!executedNames.containsKey(normalizedName)) {
                        warnings.add("WARN: '" + configuredName + "' no encontrado en "
                                + testNote.getTestName());
                    }
                }
            }
        }
        return warnings;
    }

    private List<Testcase> testcases(Testsuite testsuite) {
        return testsuite.getTestcase() == null ? Collections.emptyList() : testsuite.getTestcase();
    }

    private String resolveScoreName(String methodName, List<TestNote> testNotes) {
        for (TestNote testNote : testNotes) {
            if (testNote.getMethodsNote() != null && testNote.getMethodsNote().containsKey(methodName)) {
                return methodName;
            }
        }

        String normalizedName = normalizeTypedMethodName(methodName);
        for (TestNote testNote : testNotes) {
            if (testNote.getMethodsNote() != null
                    && testNote.getMethodsNote().containsKey(normalizedName)) {
                return normalizedName;
            }
        }
        return normalizedName;
    }

    private String normalizeTypedMethodName(String methodName) {
        int openingParenthesis = methodName.indexOf('(');
        if (openingParenthesis > 0 && methodName.endsWith(")")) {
            return methodName.substring(0, openingParenthesis) + "()";
        }
        return methodName;
    }

    private Testcase groupedTestcase(String name, String className) {
        Testcase testcase = new Testcase();
        testcase.setName(name);
        testcase.setClassname(className);
        testcase.setGrouped(true);
        return testcase;
    }

    private Testcase invocationTestcase(Testcase xmlTestcase, InvocationResult result) {
        Testcase invocation = new Testcase();
        invocation.setName(xmlTestcase.getName());
        invocation.setClassname(xmlTestcase.getClassname());
        invocation.setTime(xmlTestcase.getTime());
        invocation.setSuccessful(xmlTestcase.getSuccessful());
        if (!"SUCCESS".equals(result.status)) {
            String message = result.message;
            if (message == null || message.trim().isEmpty()) {
                message = "Invocation " + result.status.toLowerCase() + ".";
            }
            invocation.setFailure(new Failure(message, result.exceptionType));
        }
        return invocation;
    }

    private String simpleClassName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    static final class InvocationResult {
        private final String className;
        private final String methodName;
        private final String invocationName;
        private final String status;
        private final String message;
        private final String exceptionType;

        InvocationResult(String className, String methodName, String invocationName,
                         String status, String message, String exceptionType) {
            this.className = className;
            this.methodName = methodName;
            this.invocationName = invocationName;
            this.status = status;
            this.message = message;
            this.exceptionType = exceptionType;
        }
    }
}
