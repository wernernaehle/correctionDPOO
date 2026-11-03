package edu.uoc.correction.model;

public class MinimumRequirement {
    private final String testClass;
    private final String testName;

    public MinimumRequirement(String testClass, String testName) {
        this.testClass = testClass;
        this.testName = testName;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestName() {
        return testName;
    }

    public String getDisplayName() {
        return testClass + "#" + testName;
    }
}
