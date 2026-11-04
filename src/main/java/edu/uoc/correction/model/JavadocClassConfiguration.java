package edu.uoc.correction.model;

import java.util.EnumSet;
import java.util.Set;

public class JavadocClassConfiguration {
    private final String className;
    private final double presenceScore;
    private final double qualityMaxScore;
    private final Set<JavadocElementType> include;
    private final String referencePath;

    public JavadocClassConfiguration(String className, double presenceScore, double qualityMaxScore,
                                     Set<JavadocElementType> include, String referencePath) {
        this.className = className;
        this.presenceScore = presenceScore;
        this.qualityMaxScore = qualityMaxScore;
        this.include = include == null || include.isEmpty()
                ? EnumSet.allOf(JavadocElementType.class) : EnumSet.copyOf(include);
        this.referencePath = referencePath;
    }

    public String getClassName() {
        return className;
    }

    public double getPresenceScore() {
        return presenceScore;
    }

    public double getQualityMaxScore() {
        return qualityMaxScore;
    }

    public double getMaximumScore() {
        return presenceScore + qualityMaxScore;
    }

    public Set<JavadocElementType> getInclude() {
        return EnumSet.copyOf(include);
    }

    public String getReferencePath() {
        return referencePath;
    }
}
