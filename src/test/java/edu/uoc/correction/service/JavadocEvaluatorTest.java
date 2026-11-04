package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.JavadocClassConfiguration;
import edu.uoc.correction.model.JavadocClassResult;
import edu.uoc.correction.model.JavadocElementType;
import edu.uoc.correction.model.JavadocEvaluation;
import edu.uoc.correction.model.JavadocExerciseConfiguration;
import edu.uoc.correction.model.MinimumRequirement;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.Testsuite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavadocEvaluatorTest {

    @Test
    void awardsMaximumForCompleteDocumentationWithDifferentWording(@TempDir Path temporaryDirectory)
            throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                completeReferenceSource());
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject",
                completeStudentSource());
        Exercise exercise = exercise(temporaryDirectory, reference, student, EnumSet.allOf(JavadocElementType.class),
                0.5, 0.5);
        JavadocEvaluator evaluator = evaluator(exercise, EnumSet.allOf(JavadocElementType.class), 0.5, 0.5);

        JavadocEvaluation result = evaluator.evaluate(exercise);

        assertEquals(1.0, result.getTotalScore());
        assertEquals(1.0, result.getMaximumScore());
        assertTrue(result.getClassResults().get(0).getDetails().isEmpty());
    }

    @Test
    void appliesHalfQualityAndReportsMissingClassTags(@TempDir Path temporaryDirectory) throws IOException {
        String referenceSource = "/** Represents a useful value.\n * @author Reference\n */\n"
                + "public class DocSubject {}";
        String studentSource = "/** Describes a useful value in this package. */\n"
                + "public class DocSubject {}";
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject", referenceSource);
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject", studentSource);
        Exercise exercise = exercise(temporaryDirectory, reference, student,
                EnumSet.of(JavadocElementType.CLASS), 0.5, 0.5);

        JavadocClassResult result = evaluator(exercise, EnumSet.of(JavadocElementType.CLASS),
                0.5, 0.5).evaluate(exercise).getClassResults().get(0);

        assertEquals(0.5, result.getPresenceScore());
        assertEquals(0.25, result.getQualityScore());
        assertEquals(0.25, result.getPenalty());
        assertEquals(0.75, result.getTotalScore());
        assertTrue(result.getDetails().contains("DocSubject: falta @author"));
    }

    @Test
    void emptyAndOrdinaryCommentsDoNotCountAsJavadoc(@TempDir Path temporaryDirectory) throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                completeReferenceSource());
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject",
                "/**   */\npublic class DocSubject {\n"
                        + "    // ordinary comment\n"
                        + "    /* another ordinary comment */\n"
                        + "    private int value;\n"
                        + "}");
        Exercise exercise = exercise(temporaryDirectory, reference, student,
                EnumSet.allOf(JavadocElementType.class), 0.5, 0.5);

        JavadocClassResult result = evaluator(exercise, EnumSet.allOf(JavadocElementType.class),
                0.5, 0.5).evaluate(exercise).getClassResults().get(0);

        assertFalse(result.isHasJavadoc());
        assertEquals(0.0, result.getTotalScore());
        assertEquals("No se han encontrado comentarios Javadoc.", result.getStatusMessage());
    }

    @Test
    void reportsMissingClassAndDoesNotAwardJavadoc(@TempDir Path temporaryDirectory) throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                completeReferenceSource());
        Path studentDirectory = Files.createDirectories(temporaryDirectory.resolve("student"));
        Exercise exercise = exercise(temporaryDirectory, reference, studentDirectory,
                EnumSet.allOf(JavadocElementType.class), 0.5, 0.5);

        JavadocClassResult result = evaluator(exercise, EnumSet.allOf(JavadocElementType.class),
                0.5, 0.5).evaluate(exercise).getClassResults().get(0);

        assertFalse(result.isClassFound());
        assertEquals(0.0, result.getTotalScore());
        assertTrue(result.getStatusMessage().contains("No se ha encontrado la clase DocSubject"));
    }

    @Test
    void excludesReferenceElementsMissingFromTheDelivery(@TempDir Path temporaryDirectory) throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                completeReferenceSource());
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject",
                "/** Describes another useful value.\n * @author Student\n * @version 2\n */\n"
                        + "public class DocSubject {\n"
                        + "    /** Transforms the incoming text.\n"
                        + "     * @param incoming source text\n"
                        + "     * @return transformed text\n"
                        + "     * @throws IllegalStateException when conversion fails\n"
                        + "     */\n"
                        + "    public java.lang.String convert(java.lang.String incoming)"
                        + " throws java.lang.IllegalStateException { return incoming; }\n"
                        + "}");
        Exercise exercise = exercise(temporaryDirectory, reference, student,
                EnumSet.of(JavadocElementType.PUBLIC_METHODS), 0.5, 0.5);

        JavadocClassResult result = evaluator(exercise, EnumSet.of(JavadocElementType.PUBLIC_METHODS),
                0.5, 0.5).evaluate(exercise).getClassResults().get(0);

        assertEquals(0.5, result.getQualityScore(), result.getDetails().toString());
        assertTrue(result.getDetails().contains(
                "isReady(): no evaluado: no existe en la entrega"));
    }

    @Test
    void compileFailureDoesNotEvaluateSourceOrAwardJavadoc(@TempDir Path temporaryDirectory)
            throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                completeReferenceSource());
        Exercise exercise = exercise(temporaryDirectory, reference,
                temporaryDirectory.resolve("missing-student"), EnumSet.allOf(JavadocElementType.class),
                0.5, 0.5);
        exercise.setTestsuiteList(null);

        JavadocClassResult result = evaluator(exercise, EnumSet.allOf(JavadocElementType.class),
                0.5, 0.5).evaluate(exercise).getClassResults().get(0);

        assertEquals(0.0, result.getTotalScore());
        assertTrue(result.getStatusMessage().contains("porque el ejercicio no compila"));
    }

    @Test
    void rejectsReferenceClassWithoutJavadocBeforeEvaluation(@TempDir Path temporaryDirectory)
            throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                "public class DocSubject {}");
        Exercise exercise = exercise(temporaryDirectory, reference, temporaryDirectory,
                EnumSet.allOf(JavadocElementType.class), 0.5, 0.5);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> evaluator(exercise, EnumSet.allOf(JavadocElementType.class),
                        0.5, 0.5).validateConfiguration());

        assertTrue(error.getMessage().contains("PAC2Ex2.javadoc.classes"));
        assertTrue(error.getMessage().contains("DocSubject"));
        assertTrue(error.getMessage().contains("no Javadoc"));
    }

    @Test
    void rejectsReferenceClassThatDoesNotExist(@TempDir Path temporaryDirectory) throws IOException {
        Path reference = Files.createDirectories(temporaryDirectory.resolve("reference"));
        Exercise exercise = exercise(temporaryDirectory, reference, temporaryDirectory,
                EnumSet.allOf(JavadocElementType.class), 0.5, 0.5);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> evaluator(exercise, EnumSet.allOf(JavadocElementType.class),
                        0.5, 0.5).validateConfiguration());

        assertTrue(error.getMessage().contains("PAC2Ex2.javadoc.classes"));
        assertTrue(error.getMessage().contains("class does not exist"));
    }

    @Test
    void rejectsTodoAndElementNameOnlyDescriptionsForQuality(@TempDir Path temporaryDirectory)
            throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                "/** Represents a useful subject. */ public class DocSubject {}");
        Path todoStudent = writeSource(temporaryDirectory.resolve("todo-student"), "DocSubject",
                "/** TODO */ public class DocSubject {}");
        Exercise todoExercise = exercise(temporaryDirectory, reference, todoStudent,
                EnumSet.of(JavadocElementType.CLASS), 0.5, 0.5);
        JavadocClassResult todoResult = evaluator(todoExercise, EnumSet.of(JavadocElementType.CLASS),
                0.5, 0.5).evaluate(todoExercise).getClassResults().get(0);

        Path repeatedStudent = writeSource(temporaryDirectory.resolve("repeated-student"), "DocSubject",
                "/** DocSubject */ public class DocSubject {}");
        Exercise repeatedExercise = exercise(temporaryDirectory, reference, repeatedStudent,
                EnumSet.of(JavadocElementType.CLASS), 0.5, 0.5);
        JavadocClassResult repeatedResult = evaluator(repeatedExercise,
                EnumSet.of(JavadocElementType.CLASS), 0.5, 0.5)
                .evaluate(repeatedExercise).getClassResults().get(0);

        assertEquals(0.0, todoResult.getQualityScore());
        assertEquals(0.0, repeatedResult.getQualityScore());
    }

    @Test
    void reportsMissingParameterDocumentationForAnExistingMethod(@TempDir Path temporaryDirectory)
            throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                "/** Describes the subject. */ public class DocSubject {\n"
                        + "    /** Checks elapsed time.\n"
                        + "     * @param days number of days\n"
                        + "     */\n"
                        + "    public void check(int days) {}\n"
                        + "}");
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject",
                "public class DocSubject {\n"
                        + "    /** Checks the elapsed interval. */\n"
                        + "    public void check(int duration) {}\n"
                        + "}");
        Exercise exercise = exercise(temporaryDirectory, reference, student,
                EnumSet.of(JavadocElementType.PUBLIC_METHODS), 0.5, 0.5);

        JavadocClassResult result = evaluator(exercise,
                EnumSet.of(JavadocElementType.PUBLIC_METHODS), 0.5, 0.5)
                .evaluate(exercise).getClassResults().get(0);

        assertEquals(0.25, result.getQualityScore());
        assertTrue(result.getDetails().contains("check(int): falta @param duration"));
    }

    @Test
    void sumsScoresForMultipleConfiguredClasses(@TempDir Path temporaryDirectory) throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                "/** Represents a useful subject. */ public class DocSubject {}");
        writeSource(reference, "SecondSubject", "/** Describes a second subject. */ public class SecondSubject {}");
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject",
                "/** Explains the subject in another way. */ public class DocSubject {}");
        writeSource(student, "SecondSubject",
                "/** Introduces another documented subject. */ public class SecondSubject {}");
        Configuration configuration = new Configuration();
        configuration.getMapExercisesTests().get("PAC2Ex2").setJavadocConfiguration(
                new JavadocExerciseConfiguration(true, Arrays.asList(
                        new JavadocClassConfiguration("DocSubject", 0.5, 0.5,
                                EnumSet.of(JavadocElementType.CLASS), reference.toString()),
                        new JavadocClassConfiguration("SecondSubject", 0.25, 0.25,
                                EnumSet.of(JavadocElementType.CLASS), reference.toString()))));
        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex2");
        exercise.setSourceDirectory(student.toString());
        exercise.setTestsuiteList(Collections.emptyList());
        JavadocEvaluator evaluator = new JavadocEvaluator(configuration);
        evaluator.validateConfiguration();

        JavadocEvaluation result = evaluator.evaluate(exercise);

        assertEquals(1.5, result.getTotalScore());
        assertEquals(1.5, result.getMaximumScore());
    }

    @Test
    void evaluatesTheRepositoryReferenceSolutionAtMaximum(@TempDir Path temporaryDirectory)
            throws IOException {
        Configuration configuration = new Configuration();
        JavadocClassConfiguration classConfiguration = new JavadocClassConfiguration(
                "MuseumArtifact", 0.5, 0.5, EnumSet.allOf(JavadocElementType.class), null);
        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get("PAC2Ex2");
        exerciseConfiguration.setJavadocConfiguration(new JavadocExerciseConfiguration(
                true, Collections.singletonList(classConfiguration)));
        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex2");
        exercise.setSourceDirectory(new java.io.File(
                "reference-solutions/DPOO_PAC2_sol/PAC2Ex2_sol/src/main/java").getAbsolutePath());
        exercise.setTestsuiteList(Collections.emptyList());
        JavadocEvaluator evaluator = new JavadocEvaluator(configuration);
        evaluator.validateConfiguration();

        JavadocEvaluation result = evaluator.evaluate(exercise);

        assertEquals(1.0, result.getTotalScore());
        assertEquals(1.0, result.getMaximumScore());
    }

    @Test
    void minimumRequirementFailureDoesNotSuppressJavadocScore(@TempDir Path temporaryDirectory)
            throws IOException {
        Path reference = writeSource(temporaryDirectory.resolve("reference"), "DocSubject",
                "/** Represents a useful subject. */ public class DocSubject {}");
        Path student = writeSource(temporaryDirectory.resolve("student"), "DocSubject",
                "/** Describes a useful subject in a different way. */ public class DocSubject {}");
        Configuration configuration = new Configuration();
        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get("PAC2Ex2");
        exerciseConfiguration.setJavadocConfiguration(new JavadocExerciseConfiguration(true,
                Collections.singletonList(new JavadocClassConfiguration("DocSubject", 0.5, 0.5,
                        EnumSet.of(JavadocElementType.CLASS), reference.toString()))));
        exerciseConfiguration.setMinimumRequirements(Collections.singletonList(
                new MinimumRequirement("ExampleTest", "mustPass()")));

        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(1.0);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        Testcase failedTest = new Testcase();
        failedTest.setName("mustPass()");
        failedTest.setFailure(new edu.uoc.correction.model.Failure("failed", "AssertionError"));
        Testsuite testsuite = new Testsuite();
        testsuite.setName("edu.uoc.pac2.ExampleTest");
        testsuite.setTests(1);
        testsuite.setFailures(1);
        testsuite.setErrors(0);
        testsuite.setScore(1.0);
        testsuite.setTestcase(Collections.singletonList(failedTest));

        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex2");
        exercise.setSourceDirectory(student.toString());
        exercise.setTestsuiteList(Collections.singletonList(testsuite));
        JavadocEvaluator evaluator = new JavadocEvaluator(configuration);
        evaluator.validateConfiguration();

        java.util.List<Double> exerciseScores = new java.util.ArrayList<>();
        double studentScore = new ScoreCalculationService(configuration, evaluator)
                .calculateStudentScore(Collections.singletonList(exercise), exerciseScores);

        assertEquals(1.0, studentScore);
        assertEquals(Collections.singletonList(0.0), exerciseScores);
        assertEquals(1.0, exercise.getJavadocEvaluation().getTotalScore());
        assertEquals(1, exercise.getFailedMinimumRequirements().size());
    }

    private JavadocEvaluator evaluator(Exercise exercise, EnumSet<JavadocElementType> include,
                                       double presenceScore, double qualityMaxScore) {
        Configuration configuration = new Configuration();
        JavadocClassConfiguration classConfiguration = new JavadocClassConfiguration(
                "DocSubject", presenceScore, qualityMaxScore, include,
                exerciseReferencePath.toString());
        configuration.getMapExercisesTests().get("PAC2Ex2")
                .setJavadocConfiguration(new JavadocExerciseConfiguration(
                        true, Collections.singletonList(classConfiguration)));
        JavadocEvaluator evaluator = new JavadocEvaluator(configuration);
        evaluator.validateConfiguration();
        return evaluator;
    }

    private Path exerciseReferencePath;

    private Exercise exercise(Path temporaryDirectory, Path reference, Path student,
                             EnumSet<JavadocElementType> include,
                             double presenceScore, double qualityMaxScore) throws IOException {
        Files.createDirectories(student);
        exerciseReferencePath = reference;
        Exercise exercise = new Exercise();
        exercise.setName("PAC2Ex2");
        exercise.setSourceDirectory(student.toString());
        exercise.setTestsuiteList(Collections.emptyList());
        return exercise;
    }

    private Path writeSource(Path directory, String className, String source) throws IOException {
        Path sourceDirectory = Files.createDirectories(directory.resolve("src"));
        Path sourceFile = sourceDirectory.resolve(className + ".java");
        Files.write(sourceFile, source.getBytes(StandardCharsets.UTF_8));
        return directory;
    }

    private String completeReferenceSource() {
        return "/** Represents a documented subject.\n"
                + " * @author Reference\n"
                + " * @version 1\n"
                + " */\n"
                + "public class DocSubject {\n"
                + "    /** Stores the subject name. */\n"
                + "    private String name;\n"
                + "    /** Creates a subject.\n"
                + "     * @param input initial name\n"
                + "     * @throws IllegalArgumentException if the name is invalid\n"
                + "     */\n"
                + "    public DocSubject(String input) throws IllegalArgumentException {}\n"
                + "    /** Converts text.\n"
                + "     * @param input source text\n"
                + "     * @return converted text\n"
                + "     * @throws IllegalStateException if conversion fails\n"
                + "     */\n"
                + "    public String convert(String input) throws IllegalStateException { return input; }\n"
                + "    /** Reports readiness.\n"
                + "     * @return whether the subject is ready\n"
                + "     */\n"
                + "    public boolean isReady() { return true; }\n"
                + "    /** Checks a name.\n"
                + "     * @param candidate name to check\n"
                + "     * @return whether the name is accepted\n"
                + "     * @throws IllegalArgumentException if the name is invalid\n"
                + "     */\n"
                + "    private boolean valid(String candidate) throws IllegalArgumentException { return true; }\n"
                + "}";
    }

    private String completeStudentSource() {
        return "/** Describes a useful subject in a different way.\n"
                + " * @author Student\n"
                + " * @version 2\n"
                + " */\n"
                + "public class DocSubject {\n"
                + "    /** Keeps the display name for later use. */\n"
                + "    private String name;\n"
                + "    /** Initializes this object with a value.\n"
                + "     * @param text starting value\n"
                + "     * @throws java.lang.IllegalArgumentException for an invalid value\n"
                + "     */\n"
                + "    public DocSubject(String text) throws java.lang.IllegalArgumentException {}\n"
                + "    /** Changes a string into its output form.\n"
                + "     * @param incoming source characters\n"
                + "     * @return the resulting string\n"
                + "     * @throws java.lang.IllegalStateException when conversion cannot finish\n"
                + "     */\n"
                + "    public java.lang.String convert(java.lang.String incoming)"
                + " throws java.lang.IllegalStateException { return incoming; }\n"
                + "    /** Indicates whether use is possible.\n"
                + "     * @return the current readiness value\n"
                + "     */\n"
                + "    public boolean isReady() { return true; }\n"
                + "    /** Verifies that a candidate can be used.\n"
                + "     * @param value value to inspect\n"
                + "     * @return whether it is valid\n"
                + "     * @throws java.lang.IllegalArgumentException when validation fails\n"
                + "     */\n"
                + "    private boolean valid(String value) throws java.lang.IllegalArgumentException { return true; }\n"
                + "}";
    }
}
