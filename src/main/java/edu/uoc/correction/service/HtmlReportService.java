package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.JavadocClassResult;
import edu.uoc.correction.model.JavadocEvaluation;
import edu.uoc.correction.model.Student;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.MathTool;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class HtmlReportService {

    private static final Logger LOGGER = Logger.getLogger(HtmlReportService.class.getName());
    private static final String TEMPLATE_PATH = "src/main/resources/template/index.vm";

    private final Configuration configuration;
    private final ScoreCalculationService scoreCalculationService;

    HtmlReportService(Configuration configuration) {
        this(configuration, new JavadocEvaluator(configuration));
    }

    HtmlReportService(Configuration configuration, JavadocEvaluator javadocEvaluator) {
        this.configuration = configuration;
        this.scoreCalculationService = new ScoreCalculationService(configuration, javadocEvaluator);
    }

    Map<String, Double> generate(List<Student> students) {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        Template template = velocityEngine.getTemplate(TEMPLATE_PATH, "UTF-8");
        VelocityContext context = new VelocityContext();
        Map<String, Double> studentScores = new LinkedHashMap<>();
        for (Student student : students) {
            studentScores.put(student.getName(), renderStudentReport(student, template, context));
        }
        return studentScores;
    }

    private double renderStudentReport(Student student, Template template, VelocityContext context) {
        List<Exercise> exercises = student.getExercises();
        List<Double> exerciseScores = new ArrayList<>();
        double studentScore = scoreCalculationService.calculateStudentScore(exercises, exerciseScores);
        JavadocEvaluation javadocEvaluation = combineJavadocEvaluations(exercises);

        String[] splitName = student.getName().split("_PEC_");
        context.put("math", new MathTool());
        context.put("username", splitName[0]);
        context.put("pac", configuration.getName());
        context.put("exercises", exercises);
        context.put("noteExercises", exerciseScores);
        context.put("notePAC", studentScore);
        context.put("scorePAC", configuration.getScorePAC());
        context.put("configurationExercises", configuration.getMapExercisesTests());
        context.put("javadocExerciseTitle", configuration.getJavadocExerciseTitle());
        context.put("javadocEvaluation", javadocEvaluation);

        try (Writer writer = new FileWriter(
                configuration.getDirectoryReportsPACs() + "/" + student.getName() + "/index.html")) {
            template.merge(context, writer);
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not write report for student " + student.getName(), e);
            throw new IllegalStateException("Could not write report for student " + student.getName(), e);
        }
        return studentScore;
    }

    private JavadocEvaluation combineJavadocEvaluations(List<Exercise> exercises) {
        if (exercises == null) {
            return null;
        }
        List<JavadocClassResult> classResults = new ArrayList<>();
        for (Exercise exercise : exercises) {
            if (exercise != null && exercise.getJavadocEvaluation() != null) {
                classResults.addAll(exercise.getJavadocEvaluation().getClassResults());
            }
        }
        return classResults.isEmpty() ? null : new JavadocEvaluation(classResults);
    }
}
