package edu.uoc.correction.model;

public final class QualityCategoryResult {
    private final String category;
    private final String description;
    private final int violations;
    private final double penalty;

    public QualityCategoryResult(String category, String description, int violations, double penalty) {
        this.category = category;
        this.description = description;
        this.violations = violations;
        this.penalty = penalty;
    }

    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public int getViolations() { return violations; }
    public double getPenalty() { return penalty; }
}
