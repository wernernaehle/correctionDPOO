package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.QualityCategory;
import edu.uoc.correction.model.QualityExerciseConfiguration;
import edu.uoc.correction.model.QualityRule;
import edu.uoc.correction.model.QualityViolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeQualityEvaluatorTest {

    @Test
    void referenceIsUnpenalizedAndAcceptancePenaltyUsesCategoryWeights(@TempDir Path temp)
            throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        write(reference.resolve("Sample.java"), "class Sample { }");
        write(student.resolve("Sample.java"), "import java.util.List;\n"
                + "class Sample {\n"
                + "  String first;\n"
                + "  String second;\n"
                + "  boolean same() { return first == second || first != second; }\n"
                + "}");

        Configuration configuration = configuredQuality(reference);
        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(configuration);
        evaluator.validateConfiguration();

        Exercise referenceExercise = exercise("PAC2Ex1", reference);
        var referenceResult = evaluator.evaluate(referenceExercise, 3.0);
        assertEquals(0.0, referenceResult.getPenalty());
        assertTrue(referenceResult.getViolations().isEmpty());

        Exercise studentExercise = exercise("PAC2Ex1", student);
        var result = evaluator.evaluate(studentExercise, 3.0);
        assertEquals(2, count(result.getViolations(), QualityRule.STRING_REFERENCE_COMPARISON));
        assertEquals(1, count(result.getViolations(), QualityRule.UNUSED_IMPORT));
        assertEquals(0.13, categoryPenalty(result, QualityCategory.JAVA_IDIOMS), 0.000001);
        assertEquals(0.07, categoryPenalty(result, QualityCategory.DEAD_CODE), 0.000001);
        assertEquals(0.20, result.getPenalty(), 0.000001);
        assertEquals(2.80, result.getFinalScore(), 0.000001);
        assertEquals(0.0, evaluator.evaluate(studentExercise, 0.01).getFinalScore());
    }

    @Test
    void usesReferenceComplexityAsTheThreshold(@TempDir Path temp) throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        write(reference.resolve("Sample.java"), "class Sample { int calculate(int a) { "
                + "if (a > 0) return a; return 0; } }");
        write(student.resolve("Sample.java"), "class Sample { int calculate(int a) { "
                + "if (a > 0) { if (a > 1) { if (a > 2) { if (a > 3) return a; } } } return 0; } }");

        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(configuredQuality(reference));
        evaluator.validateConfiguration();
        var result = evaluator.evaluate(exercise("PAC2Ex1", student), 1.0);
        assertTrue(result.getViolations().stream()
                .anyMatch(violation -> violation.getRule() == QualityRule.NESTING_DEPTH));
        assertTrue(result.getViolations().stream()
                .noneMatch(violation -> violation.getRule() == QualityRule.CYCLOMATIC_COMPLEXITY));
    }

    @Test
    void automaticallyDisablesRulesTheReferenceItselfViolates(@TempDir Path temp) throws IOException {
        Path reference = temp.resolve("reference");
        Files.createDirectories(reference);
        write(reference.resolve("Sample.java"), "class Sample { private int unusedField; }");
        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(configuredQuality(reference));
        evaluator.validateConfiguration();

        var result = evaluator.evaluate(exercise("PAC2Ex1", reference), 3.0);
        assertEquals(0.0, result.getPenalty());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void allowsNullComparisonsEqualsAndValidCamelCase(@TempDir Path temp) throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        write(reference.resolve("Sample.java"), "class Sample { }");
        write(student.resolve("Sample.java"), "class Sample { String title; "
                + "boolean isValid(String input) { return input == null || title.equals(input); } }");
        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(configuredQuality(reference));
        evaluator.validateConfiguration();

        var violations = evaluator.evaluate(exercise("PAC2Ex1", student), 1.0).getViolations();
        assertTrue(violations.stream()
                .noneMatch(item -> item.getRule() == QualityRule.STRING_REFERENCE_COMPARISON));
        assertTrue(violations.stream()
                .noneMatch(item -> item.getRule() == QualityRule.NON_CAMEL_CASE_NAME));
    }

    @Test
    void doesNotReportPrivateMethodsCalledThroughThisOrUnqualifiedInvocation(@TempDir Path temp)
            throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        write(reference.resolve("Sample.java"), "class Sample { }");
        write(student.resolve("Sample.java"), "class Sample {\n"
                + "  void caller() { this.usedThroughThis(); usedUnqualified(); }\n"
                + "  private void usedThroughThis() { }\n"
                + "  private void usedUnqualified() { }\n"
                + "  private void unused() { }\n"
                + "}");
        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(configuredQuality(reference));
        evaluator.validateConfiguration();

        var violations = evaluator.evaluate(exercise("PAC2Ex1", student), 1.0).getViolations();
        assertEquals(1, count(violations, QualityRule.UNUSED_PRIVATE_METHOD));
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getRule() == QualityRule.UNUSED_PRIVATE_METHOD && violation.getLine() == 5));
    }

    @Test
    void reportsStableFileLineAndRuleIdentifiers(@TempDir Path temp) throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        write(reference.resolve("Sample.java"), "class Sample { }");
        write(student.resolve("Sample.java"), "class Sample {\n"
                + "  String first;\n"
                + "  String second;\n"
                + "  boolean same() { return first == second; }\n"
                + "}");
        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(configuredQuality(reference));
        evaluator.validateConfiguration();
        QualityViolation violation = evaluator.evaluate(exercise("PAC2Ex1", student), 1.0)
                .getViolations().stream()
                .filter(item -> item.getRule() == QualityRule.STRING_REFERENCE_COMPARISON)
                .findFirst().orElseThrow();
        assertEquals("Sample.java", violation.getFile());
        assertEquals(4, violation.getLine());
    }

    @Test
    void detectsEveryConfiguredRule(@TempDir Path temp) throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        write(reference.resolve("Sample.java"), "class Sample { }");
        write(student.resolve("Sample.java"), "import java.util.List;\n"
                + "class Sample {\n"
                + "  int Bad_Name;\n"
                + "  private int unusedField;\n"
                + "  private static final int badConstant = 4;\n"
                + "  int initialized = 42;\n"
                + "  Sample() { initialized = 42; }\n"
                + "  Sample(int value) { initialized = 42; }\n"
                + "  private void unusedMethod(int ignored) { int notUsed = 0; }\n"
                + "  void duplicate() { one(); two(); three(); four(); five(); "
                + "one(); two(); three(); four(); five(); }\n"
                + "  void nested(boolean a, boolean b, boolean c) { "
                + "if (a) { if (b) { if (c) { one(); } } } }\n"
                + "  void longMethod() { one(); two(); three(); four(); five(); }\n"
                + "  void jumps() { while (true) { if (Bad_Name > 0) break; "
                + "if (Bad_Name < 0) continue; } }\n"
                + "  boolean compare(String first, String second) { return first == second; }\n"
                + "  int immediate() { int value = 42; return value; }\n"
                + "  boolean conditional(boolean condition) { "
                + "if (condition) return true; else return false; }\n"
                + "  String line() { return \"\\n\"; }\n"
                + "  // return 123;\n"
                + "}");
        CodeQualityEvaluator evaluator = new CodeQualityEvaluator(
                configuredQuality(reference, 1, 1, 3));
        evaluator.validateConfiguration();

        Set<QualityRule> detected = evaluator.evaluate(exercise("PAC2Ex1", student), 1.0)
                .getViolations().stream().map(QualityViolation::getRule).collect(java.util.stream.Collectors.toSet());
        assertEquals(EnumSet.allOf(QualityRule.class), detected);
    }

    private Configuration configuredQuality(Path reference) {
        return configuredQuality(reference, 10, 3, 30);
    }

    private Configuration configuredQuality(Path reference, int complexity, int nesting, int methodLength) {
        Configuration configuration = new Configuration();
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        exerciseConfiguration.setQualityConfiguration(new QualityExerciseConfiguration(
                1.0, equalWeights(), 3, complexity, nesting, methodLength, Set.of("Sample"), reference,
                EnumSet.noneOf(QualityRule.class)));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);
        return configuration;
    }

    private EnumMap<QualityCategory, Double> equalWeights() {
        EnumMap<QualityCategory, Double> weights = new EnumMap<>(QualityCategory.class);
        for (QualityCategory category : QualityCategory.values()) weights.put(category, 1.0);
        return weights;
    }

    private Exercise exercise(String name, Path sourceDirectory) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setSourceDirectory(sourceDirectory.toString());
        return exercise;
    }

    private int count(java.util.List<QualityViolation> violations, QualityRule rule) {
        return (int) violations.stream().filter(item -> item.getRule() == rule).count();
    }

    private double categoryPenalty(edu.uoc.correction.model.QualityEvaluation evaluation,
                                   QualityCategory category) {
        return evaluation.getCategories().stream()
                .filter(item -> item.getCategory().equals(category.getKey()))
                .findFirst().orElseThrow().getPenalty();
    }

    private void write(Path file, String source) throws IOException {
        Files.write(file, source.getBytes(StandardCharsets.UTF_8));
    }
}
