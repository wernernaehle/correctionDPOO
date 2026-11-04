package edu.uoc.correction.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class JavadocClassResult {
    private final String className;
    private final double presenceScore;
    private final double presenceMaxScore;
    private final double qualityScore;
    private final double qualityMaxScore;
    private final boolean classFound;
    private final boolean hasJavadoc;
    private final String statusMessage;
    private final List<String> details;

    public JavadocClassResult(String className, double presenceScore, double presenceMaxScore,
                              double qualityScore, double qualityMaxScore, boolean classFound,
                              boolean hasJavadoc, String statusMessage, List<String> details) {
        this.className = className;
        this.presenceScore = presenceScore;
        this.presenceMaxScore = presenceMaxScore;
        this.qualityScore = qualityScore;
        this.qualityMaxScore = qualityMaxScore;
        this.classFound = classFound;
        this.hasJavadoc = hasJavadoc;
        this.statusMessage = statusMessage;
        this.details = details == null ? new ArrayList<>() : new ArrayList<>(details);
    }

    public String getClassName() {
        return className;
    }

    public double getPresenceScore() {
        return presenceScore;
    }

    public double getPresenceMaxScore() {
        return presenceMaxScore;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public double getQualityMaxScore() {
        return qualityMaxScore;
    }

    public double getPenalty() {
        return BigDecimal.valueOf(qualityMaxScore - qualityScore)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getTotalScore() {
        return presenceScore + qualityScore;
    }

    public double getMaximumScore() {
        return presenceMaxScore + qualityMaxScore;
    }

    public boolean isClassFound() {
        return classFound;
    }

    public boolean isHasJavadoc() {
        return hasJavadoc;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public List<String> getDetails() {
        return new ArrayList<>(details);
    }
}
