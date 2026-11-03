package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testcase;
import edu.uoc.correction.model.Testsuite;

import java.util.List;
import java.util.logging.Logger;

final class ScoreCalculationService {

    private static final Logger LOGGER = Logger.getLogger(ScoreCalculationService.class.getName());

    private final Configuration configuration;

    ScoreCalculationService(Configuration configuration) {
        this.configuration = configuration;
    }

    double calculateStudentScore(List<Exercise> exercises, List<Double> exerciseScores) {
        double studentScore = 0;
        if (exercises == null) {
            LOGGER.warning("Cannot calculate a student score because exercises are missing");
            return studentScore;
        }
        for (Exercise exercise : exercises) {
            if (exercise == null) {
                LOGGER.warning("Cannot calculate a score for a missing exercise");
                continue;
            }
            double exerciseScore = 0;
            if (exercise.getTestsuiteList() != null) {
                for (Testsuite testsuite : exercise.getTestsuiteList()) {
                    if (testsuite == null) {
                        LOGGER.warning("Cannot calculate a score for a missing test suite in exercise "
                                + exercise.getName());
                        continue;
                    }
                    exerciseScore += calculateTestsuiteScore(exercise, testsuite);
                }
                exerciseScores.add(exerciseScore);
            }
            studentScore += exerciseScore;
        }
        return studentScore;
    }

    private double calculateTestsuiteScore(Exercise exercise, Testsuite testsuite) {
        ExerciceConfiguration exerciseConfiguration =
                configuration.getMapExercisesTests().get(exercise.getName());
        if (exerciseConfiguration != null
                && exerciseConfiguration.isScoreByMethodsForClass(testsuite.getName())) {
            return calculateMethodScore(exercise, testsuite, exerciseConfiguration);
        }
        if (exerciseConfiguration == null) {
            LOGGER.warning("No test score configuration found for exercise " + exercise.getName());
        }

        Integer tests = testsuite.getTests();
        Integer failures = testsuite.getFailures();
        Integer errors = testsuite.getErrors();
        if (tests == null || failures == null || errors == null || tests == 0) {
            LOGGER.warning("Cannot calculate test score because test counts are incomplete for "
                    + testsuite.getName());
            return 0;
        }

        int success = ((tests - failures - errors) * 100) / tests;
        return (testsuite.getScore() * success) / 100;
    }

    private double calculateMethodScore(Exercise exercise, Testsuite testsuite,
                                        ExerciceConfiguration exerciseConfiguration) {
        List<Testcase> testcases = testsuite.getTestcase();
        if (testcases == null) {
            LOGGER.warning("Cannot calculate method score because test cases are missing for "
                    + testsuite.getName());
            return 0;
        }
        List<TestNote> testNotes = exerciseConfiguration.getListTestNote();
        if (testNotes == null) {
            LOGGER.warning("Cannot calculate method score because exercise notes are missing for "
                    + exercise.getName());
            return 0;
        }

        double score = 0;
        String testClassName = testsuite.getName().substring(testsuite.getName().lastIndexOf('.') + 1);
        boolean testClassConfigured = testNotes.stream()
                .anyMatch(testNote -> testClassName.equals(testNote.getTestName()));
        for (Testcase testcase : testcases) {
            if (testcase == null || testcase.getFailure() != null) {
                continue;
            }
            boolean methodConfigured = false;
            for (TestNote testNote : testNotes) {
                if (testNote.getMethodsNote() == null || testNote.getMethodsNote().isEmpty()) {
                    continue;
                }
                if (!testClassName.equals(testNote.getTestName())) {
                    continue;
                }
                Double methodScore = testNote.getMethodsNote().get(testcase.getName());
                if (methodScore != null) {
                    methodConfigured = true;
                    score += methodScore;
                }
            }
            if (!methodConfigured && testClassConfigured) {
                LOGGER.warning("No configured score for method " + testcase.getName()
                        + " in exercise " + exercise.getName());
            }
        }
        return score;
    }
}
