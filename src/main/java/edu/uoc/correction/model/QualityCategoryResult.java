package edu.uoc.correction.model;

public final class QualityCategoryResult {
    private final String category;
    private final int violations;
    private final double penalty;

    public QualityCategoryResult(String category, int violations, double penalty) {
        this.category = category;
        this.violations = violations;
        this.penalty = penalty;
    }

    public String getCategory() { return category; }
    public int getViolations() { return violations; }
    public double getPenalty() { return penalty; }
}
