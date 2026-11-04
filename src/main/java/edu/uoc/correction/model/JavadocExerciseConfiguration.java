package edu.uoc.correction.model;

import java.util.ArrayList;
import java.util.List;

public class JavadocExerciseConfiguration {
    private final boolean enabled;
    private final List<JavadocClassConfiguration> classes;

    public JavadocExerciseConfiguration(boolean enabled, List<JavadocClassConfiguration> classes) {
        this.enabled = enabled;
        this.classes = classes == null ? new ArrayList<>() : new ArrayList<>(classes);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<JavadocClassConfiguration> getClasses() {
        return new ArrayList<>(classes);
    }

    public double getMaximumScore() {
        return classes.stream().mapToDouble(JavadocClassConfiguration::getMaximumScore).sum();
    }
}
