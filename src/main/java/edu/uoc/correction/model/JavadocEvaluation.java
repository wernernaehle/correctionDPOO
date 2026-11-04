package edu.uoc.correction.model;

import java.util.ArrayList;
import java.util.List;

public class JavadocEvaluation {
    private final List<JavadocClassResult> classResults;
    private final double totalScore;
    private final double maximumScore;
    private final double presenceScore;
    private final double presenceMaximumScore;

    public JavadocEvaluation(List<JavadocClassResult> classResults) {
        this.classResults = classResults == null ? new ArrayList<>() : new ArrayList<>(classResults);
        this.totalScore = this.classResults.stream().mapToDouble(JavadocClassResult::getTotalScore).sum();
        this.maximumScore = this.classResults.stream().mapToDouble(JavadocClassResult::getMaximumScore).sum();
        this.presenceScore = this.classResults.stream().mapToDouble(JavadocClassResult::getPresenceScore).sum();
        this.presenceMaximumScore = this.classResults.stream()
                .mapToDouble(JavadocClassResult::getPresenceMaxScore).sum();
    }

    public List<JavadocClassResult> getClassResults() {
        return new ArrayList<>(classResults);
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getMaximumScore() {
        return maximumScore;
    }

    public double getPresenceScore() {
        return presenceScore;
    }

    public double getPresenceMaximumScore() {
        return presenceMaximumScore;
    }
}
