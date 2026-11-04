package edu.uoc.correction.model;

import java.util.List;

public final class QualityEvaluation {
    private final double scoreObtained;
    private final double penalty;
    private final double maxPenalty;
    private final double finalScore;
    private final List<QualityCategoryResult> categories;
    private final List<QualityViolation> violations;

    public QualityEvaluation(double scoreObtained, double penalty, double maxPenalty, double finalScore,
                             List<QualityCategoryResult> categories, List<QualityViolation> violations) {
        this.scoreObtained = scoreObtained;
        this.penalty = penalty;
        this.maxPenalty = maxPenalty;
        this.finalScore = finalScore;
        this.categories = List.copyOf(categories);
        this.violations = List.copyOf(violations);
    }

    public double getScoreObtained() { return scoreObtained; }
    public double getPenalty() { return penalty; }
    public double getMaxPenalty() { return maxPenalty; }
    public double getFinalScore() { return finalScore; }
    public List<QualityCategoryResult> getCategories() { return categories; }
    public List<QualityViolation> getViolations() { return violations; }
}
