package edu.uoc.correction.model;

public final class QualityViolation {
    private final String file;
    private final long line;
    private final QualityRule rule;
    private final String message;
    private final int measuredValue;

    public QualityViolation(String file, long line, QualityRule rule, String message) {
        this(file, line, rule, message, -1);
    }

    public QualityViolation(String file, long line, QualityRule rule, String message, int measuredValue) {
        this.file = file;
        this.line = line;
        this.rule = rule;
        this.message = message;
        this.measuredValue = measuredValue;
    }

    public String getFile() { return file; }
    public long getLine() { return line; }
    public QualityRule getRule() { return rule; }
    public String getMessage() { return message; }
    public int getMeasuredValue() { return measuredValue; }
    public String getCategory() { return rule.getCategory(); }
}
