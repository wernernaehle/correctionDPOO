package edu.uoc.correction.model;

public enum QualityCategory {
    DUPLICATION("duplication"),
    DEAD_CODE("deadCode"),
    COMPLEXITY("complexity"),
    JAVA_IDIOMS("javaIdioms"),
    NAMING("naming");

    private final String key;

    QualityCategory(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
