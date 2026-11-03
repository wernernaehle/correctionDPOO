package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.model.TestNote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultServiceCharacterizationTest {

    @Test
    void parsesStudentTestResultsAndCopiesReport(@TempDir Path temporaryDirectory) throws IOException {
        Path studentsDirectory = temporaryDirectory.resolve("students");
        Path reportsDirectory = temporaryDirectory.resolve("reports");
        Path exerciseDirectory = studentsDirectory.resolve("student_PEC_example/PAC2Ex1");
        Path sourceReport = exerciseDirectory.resolve("build/reports/tests/test/index.html");
        Path testResultsDirectory = exerciseDirectory.resolve("build/test-results/test");
        Files.createDirectories(sourceReport.getParent());
        Files.createDirectories(testResultsDirectory);
        Files.createDirectories(reportsDirectory);
        Files.createDirectories(studentsDirectory.resolve(".idea"));
        Files.write(studentsDirectory.resolve(".DS_Store"), new byte[0]);
        Files.write(studentsDirectory.resolve("readme.txt"), new byte[0]);
        Files.createDirectories(exerciseDirectory.resolve(".idea"));
        Files.write(exerciseDirectory.resolve(".DS_Store"), new byte[0]);
        Files.write(sourceReport, "<html>report</html>".getBytes(StandardCharsets.UTF_8));
        Files.write(testResultsDirectory.resolve("TEST-example.xml"),
                ("<testsuite name=\"edu.uoc.pac2.ExampleTest\" tests=\"2\" skipped=\"1\" "
                        + "failures=\"0\" errors=\"0\" timestamp=\"2024-01-01T00:00:00\" time=\"0\">"
                        + "<testcase name=\"passes\" classname=\"edu.uoc.pac2.ExampleTest\" time=\"0\"/>"
                        + "<testcase name=\"isSkipped\" classname=\"edu.uoc.pac2.ExampleTest\" time=\"0\">"
                        + "<skipped message=\"disabled\"/></testcase>"
                        + "</testsuite>").getBytes(StandardCharsets.UTF_8));

        Configuration configuration = new Configuration();
        configuration.setDirectoryStudentsPACs(studentsDirectory.toString());
        configuration.setDirectoryReportsPACs(reportsDirectory.toString());
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(1.0);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        PrintStream originalOutput = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        List<Student> students;
        try {
            System.setOut(new PrintStream(capturedOutput, true, "UTF-8"));
            students = new ResultService(configuration).parseXmlTests();
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(System.lineSeparator() + "############ Parser TEST Results ############"
                        + System.lineSeparator() + "OK" + System.lineSeparator(),
                new String(capturedOutput.toByteArray(), StandardCharsets.UTF_8));
        assertEquals(1, students.size());
        assertEquals("student_PEC_example", students.get(0).getName());
        assertEquals(1, students.get(0).getExercises().size());
        assertNull(students.get(0).getComment());
        assertEquals(1.0, students.get(0).getExercises().get(0).getTestsuiteList().get(0).getScore());
        assertTrue(students.get(0).getExercises().get(0).getTestsuiteList().get(0)
                .getTestcase().get(1).isSkipped());
        assertTrue(Files.exists(reportsDirectory.resolve(
                "student_PEC_example/PAC2Ex1/index.html")));
        assertEquals("<html>report</html>", new String(Files.readAllBytes(
                reportsDirectory.resolve("student_PEC_example/PAC2Ex1/index.html")),
                StandardCharsets.UTF_8));
    }

    @Test
    void retainsOnlyOneExerciseWhenCopyingItsHtmlReportFails(@TempDir Path temporaryDirectory)
            throws IOException {
        Path studentsDirectory = temporaryDirectory.resolve("students");
        Path reportsDirectory = temporaryDirectory.resolve("reports");
        Path exerciseDirectory = studentsDirectory.resolve("student_PEC_example/PAC2Ex1");
        Path testResultsDirectory = exerciseDirectory.resolve("build/test-results/test");
        Files.createDirectories(testResultsDirectory);
        Files.createDirectories(reportsDirectory);
        Files.write(testResultsDirectory.resolve("TEST-example.xml"),
                ("<testsuite name=\"edu.uoc.pac2.ExampleTest\" tests=\"1\" skipped=\"0\" "
                        + "failures=\"0\" errors=\"0\" timestamp=\"2024-01-01T00:00:00\" time=\"0\">"
                        + "<testcase name=\"passes\" classname=\"edu.uoc.pac2.ExampleTest\" time=\"0\"/>"
                        + "</testsuite>").getBytes(StandardCharsets.UTF_8));

        Configuration configuration = new Configuration();
        configuration.setDirectoryStudentsPACs(studentsDirectory.toString());
        configuration.setDirectoryReportsPACs(reportsDirectory.toString());
        ExerciceConfiguration exerciseConfiguration = new ExerciceConfiguration();
        TestNote testNote = new TestNote();
        testNote.setTestName("ExampleTest");
        testNote.setScore(1.0);
        exerciseConfiguration.setListTestNote(Collections.singletonList(testNote));
        configuration.getMapExercisesTests().put("PAC2Ex1", exerciseConfiguration);

        List<Student> students = new ResultService(configuration).parseXmlTests();

        assertEquals(1, students.get(0).getExercises().size());
        assertEquals("PAC2Ex1", students.get(0).getExercises().get(0).getName());
    }
}
