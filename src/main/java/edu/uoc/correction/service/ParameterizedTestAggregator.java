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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParameterizedTestAggregator {

    public AggregationResult aggregate(Testsuite testsuite, List<InvocationResult> invocationResults,
                                       ExerciceConfiguration configuration) {
        String simpleClassName = testsuite.getName().substring(testsuite.getName().lastIndexOf(".") + 1);
        List<TestNote> testNotes = new ArrayList<>();
        if (configuration != null) {
            for (TestNote testNote : configuration.getListTestNote()) {
                if (simpleClassName.equals(testNote.getTestName())) {
                    testNotes.add(testNote);
                }
            }
        }
        List<Testcase> testcases = testsuite.getTestcase() == null
                ? new ArrayList<>()
                : new ArrayList<>(testsuite.getTestcase());

        List<InvocationResult> classResults = new ArrayList<>();
        Map<String, List<InvocationResult>> groupedResults = new LinkedHashMap<>();
        Map<String, Testcase> groupedTestcases = new LinkedHashMap<>();
        for (InvocationResult invocation : invocationResults) {
            if (!testsuite.getName().equals(invocation.getClassName())) {
                continue;
            }
            classResults.add(invocation);
        }

        List<String> warnings = new ArrayList<>();
        List<Testcase> remainingTestcases = testcases;
        if (!classResults.isEmpty() && classResults.size() != testcases.size()) {
            warnings.add("Could not align JUnit listener events with XML results for '"
                    + testsuite.getName() + "'. Invocation grouping was skipped.");
        } else if (classResults.size() == testcases.size()) {
            remainingTestcases = new ArrayList<>();
            for (int i = 0; i < testcases.size(); i++) {
                Testcase testcase = testcases.get(i);
                InvocationResult result = classResults.get(i);
                if (result.getMethodName().isEmpty()) {
                    remainingTestcases.add(testcase);
                    continue;
                }

                String key = resolveConfiguredName(result.getMethodName(), testNotes);
                Testcase groupedTestcase = groupedTestcases.get(key);
                if (groupedTestcase == null) {
                    groupedTestcase = new Testcase();
                    groupedTestcase.setName(key);
                    groupedTestcase.setClassname(testsuite.getName());
                    groupedTestcase.setGrouped(true);
                    groupedTestcases.put(key, groupedTestcase);
                    remainingTestcases.add(groupedTestcase);
                }
                InvocationResult namedResult = new InvocationResult(result.getClassName(),
                        result.getMethodName(), testcase.getName(), result.getStatus(),
                        result.getMessage(), result.getExceptionType());
                groupedResults.computeIfAbsent(key, ignored -> new ArrayList<>()).add(namedResult);
            }
        }

        for (Map.Entry<String, List<InvocationResult>> entry : groupedResults.entrySet()) {
            List<Testcase> invocations = new ArrayList<>();
            boolean allPassed = !entry.getValue().isEmpty();
            for (InvocationResult result : entry.getValue()) {
                Testcase invocation = new Testcase();
                invocation.setName(result.getInvocationName());
                invocation.setClassname(result.getClassName());
                if (!"SUCCESS".equals(result.getStatus())) {
                    allPassed = false;
                    String message = result.getMessage();
                    if (message == null || message.trim().isEmpty()) {
                        message = "Invocation " + result.getStatus().toLowerCase() + ".";
                    }
                    invocation.setFailure(new Failure(message, result.getExceptionType()));
                }
                invocations.add(invocation);
            }

            Testcase groupedTestcase = groupedTestcases.get(entry.getKey());
            groupedTestcase.setInvocations(invocations);
            if (!allPassed) {
                groupedTestcase.setFailure(new Failure("One or more invocations did not pass.",
                        "InvocationFailure"));
            }
        }

        testsuite.setTestcase(remainingTestcases);
        Set<String> actualNames = new HashSet<>();
        for (Testcase testcase : remainingTestcases) {
            actualNames.add(testcase.getName());
        }

        for (TestNote testNote : testNotes) {
            for (String configuredName : testNote.getMethodsNote().keySet()) {
                if (!actualNames.contains(configuredName)) {
                    warnings.add("Configured test '" + testNote.getTestName() + "."
                            + configuredName + "' was not found in test results.");
                }
            }
        }
        return new AggregationResult(warnings);
    }

    public List<InvocationResult> readInvocationResults(File file) throws IOException {
        if (!file.isFile()) {
            return Collections.emptyList();
        }

        List<InvocationResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\t", -1);
                if (fields.length != 6) {
                    throw new IOException("Invalid invocation result at line " + lineNumber + ".");
                }
                try {
                    results.add(new InvocationResult(decode(fields[0]), decode(fields[1]),
                            decode(fields[2]), fields[3], decode(fields[4]), decode(fields[5])));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid invocation result at line " + lineNumber + ".", e);
                }
            }
        }
        return results;
    }

    private String resolveConfiguredName(String methodName, List<TestNote> testNotes) {
        for (TestNote testNote : testNotes) {
            if (testNote.getMethodsNote().containsKey(methodName)) {
                return methodName;
            }
        }

        int parameterStart = methodName.indexOf('(');
        if (parameterStart >= 0 && methodName.endsWith(")")) {
            String defaultName = methodName.substring(0, parameterStart) + "()";
            for (TestNote testNote : testNotes) {
                if (testNote.getMethodsNote().containsKey(defaultName)) {
                    return defaultName;
                }
            }
            return defaultName;
        }
        return methodName;
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public static class InvocationResult {
        private final String className;
        private final String methodName;
        private final String invocationName;
        private final String status;
        private final String message;
        private final String exceptionType;

        public InvocationResult(String className, String methodName, String invocationName,
                                String status, String message, String exceptionType) {
            this.className = className;
            this.methodName = methodName;
            this.invocationName = invocationName;
            this.status = status;
            this.message = message;
            this.exceptionType = exceptionType;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getInvocationName() {
            return invocationName;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getExceptionType() {
            return exceptionType;
        }
    }

    public static class AggregationResult {
        private final List<String> warnings;

        public AggregationResult(List<String> warnings) {
            this.warnings = warnings;
        }

        public List<String> getWarnings() {
            return warnings;
        }
    }
}
