package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.MinimumRequirement;
import edu.uoc.correction.model.QualityCategory;
import edu.uoc.correction.model.QualityExerciseConfiguration;
import edu.uoc.correction.model.QualityRule;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.Testsuite;
import edu.uoc.correction.model.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityScoreCalculationTest {

    @Test
    void appliesQualityAfterTestsAndClampsTheExerciseScoreAtZero(@TempDir Path temp) throws IOException {
        Configuration configuration = configuration(temp);
        Exercise exercise = exercise(temp.resolve("student"), true);
        exercise.getTestsuiteList().get(0).setScore(0.01);
        List<Double> exerciseScores = new ArrayList<>();

        double score = new ScoreCalculationService(configuration)
                .calculateStudentScore(List.of(exercise), exerciseScores);

        assertEquals(0.0, score);
        assertEquals(0.0, exercise.getScore());
        assertEquals(0.0, exercise.getQualityEvaluation().getFinalScore());
    }

    @Test
    void skipsQualityWhenCompilationFailsOrMinimumRequirementsAreNotMet(@TempDir Path temp)
            throws IOException {
        Configuration configuration = configuration(temp);
        Exercise compileFailure = exercise(temp.resolve("missing"), false);
        compileFailure.setTestsuiteList(null);
        new ScoreCalculationService(configuration)
                .calculateStudentScore(List.of(compileFailure), new ArrayList<>());
        assertNull(compileFailure.getQualityEvaluation());

        Exercise minimumFailure = exercise(temp.resolve("student"), true);
        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get("PAC2Ex1");
        exerciseConfiguration.setMinimumRequirements(
                List.of(new MinimumRequirement("ExampleTest", "required()")));
        minimumFailure.getTestsuiteList().get(0).setTestcase(List.of(new Testcase()));
        new ScoreCalculationService(configuration)
                .calculateStudentScore(List.of(minimumFailure), new ArrayList<>());
        assertEquals(0.0, minimumFailure.getScore());
        assertNull(minimumFailure.getQualityEvaluation());
    }

    @Test
    void rendersTheQualitySectionWithCategoryAndViolationDetails(@TempDir Path temp) throws IOException {
        Configuration configuration = configuration(temp);
        Path reports = temp.resolve("reports");
        Path studentDirectory = reports.resolve("quality-student_PEC_2");
        Files.createDirectories(studentDirectory);
        configuration.setDirectoryReportsPACs(reports.toString());
        Exercise exercise = exercise(temp.resolve("student"), true);
        Student student = new Student();
        student.setName("quality-student_PEC_2");
        student.setExercises(Arrays.asList(exercise));

        HtmlReportService service = new HtmlReportService(configuration,
                new JavadocEvaluator(configuration), new CodeQualityEvaluator(configuration));
        service.generate(List.of(student));

        String report = Files.readString(studentDirectory.resolve("index.html"));
        assertTrue(report.contains("Calidad del código"));
        assertTrue(report.contains("javaIdioms"));
        assertTrue(report.contains("Detalle de incumplimientos por categoría"));
        assertTrue(report.contains("<h6 class=\"mt-3\">javaIdioms</h6>"));
        assertTrue(report.contains("Uso de patrones y características de Java de forma clara y adecuada."));
        assertTrue(report.contains("STRING_REFERENCE_COMPARISON"));
        assertTrue(report.contains("Sample.java:1"));
    }

    private Configuration configuration(Path temp) throws IOException {
        Path reference = temp.resolve("reference");
        Path student = temp.resolve("student");
        Files.createDirectories(reference);
        Files.createDirectories(student);
        Files.write(reference.resolve("Sample.java"), "class Sample {}".getBytes(StandardCharsets.UTF_8));
        Files.write(student.resolve("Sample.java"),
                "class Sample { String first; String second; boolean same() { return first == second; } }"
                        .getBytes(StandardCharsets.UTF_8));

        Configuration configuration = new Configuration();
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        EnumMap<QualityCategory, Double> weights = new EnumMap<>(QualityCategory.class);
        for (QualityCategory category : QualityCategory.values()) weights.put(category, 1.0);
        exerciseConfiguration.setQualityConfiguration(new QualityExerciseConfiguration(
                1.0, weights, 3, 10, 3, 30, Set.of("Sample"), reference,
                EnumSet.noneOf(QualityRule.class)));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);
        return configuration;
    }

    private Exercise exercise(Path source, boolean compiled) {
        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex1");
        exercise.setSourceDirectory(source.toString());
        if (compiled) {
            Testsuite testsuite = new Testsuite();
            testsuite.setName("ExampleTest");
            testsuite.setTests(1);
            testsuite.setFailures(0);
            testsuite.setErrors(0);
            testsuite.setScore(0.01);
            exercise.setTestsuiteList(List.of(testsuite));
        }
        return exercise;
    }
}
