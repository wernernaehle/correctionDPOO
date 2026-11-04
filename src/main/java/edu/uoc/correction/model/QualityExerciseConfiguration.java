package edu.uoc.correction.model;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;

public final class QualityExerciseConfiguration {
    private final double maxPenalty;
    private final Map<QualityCategory, Double> weights;
    private final int violationsForFullCategoryPenalty;
    private final int complexityThreshold;
    private final int nestingThreshold;
    private final int methodLengthThreshold;
    private final Set<String> classes;
    private final Path referencePath;
    private final Set<QualityRule> disabledRules;

    public QualityExerciseConfiguration(double maxPenalty, Map<QualityCategory, Double> weights,
                                        int violationsForFullCategoryPenalty, int complexityThreshold,
                                        int nestingThreshold, int methodLengthThreshold,
                                        Set<String> classes, Path referencePath,
                                        Set<QualityRule> disabledRules) {
        this.maxPenalty = maxPenalty;
        this.weights = Collections.unmodifiableMap(new EnumMap<>(weights));
        this.violationsForFullCategoryPenalty = violationsForFullCategoryPenalty;
        this.complexityThreshold = complexityThreshold;
        this.nestingThreshold = nestingThreshold;
        this.methodLengthThreshold = methodLengthThreshold;
        this.classes = java.util.Collections.unmodifiableSet(new TreeSet<>(classes));
        this.referencePath = referencePath;
        this.disabledRules = disabledRules.isEmpty()
                ? Collections.unmodifiableSet(EnumSet.noneOf(QualityRule.class))
                : Collections.unmodifiableSet(EnumSet.copyOf(disabledRules));
    }

    public double getMaxPenalty() { return maxPenalty; }
    public Map<QualityCategory, Double> getWeights() { return weights; }
    public int getViolationsForFullCategoryPenalty() { return violationsForFullCategoryPenalty; }
    public int getComplexityThreshold() { return complexityThreshold; }
    public int getNestingThreshold() { return nestingThreshold; }
    public int getMethodLengthThreshold() { return methodLengthThreshold; }
    public Set<String> getClasses() { return classes; }
    public Path getReferencePath() { return referencePath; }
    public Set<QualityRule> getDisabledRules() { return disabledRules; }
}
