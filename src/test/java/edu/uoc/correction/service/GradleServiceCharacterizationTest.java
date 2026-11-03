package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleServiceCharacterizationTest {

    @Test
    void runsCleanAndTestForStudentExercisesInOrder(@TempDir Path temporaryDirectory) throws IOException {
        Path workspace = temporaryDirectory.resolve("workspace with spaces");
        Path studentsDirectory = workspace.resolve("students");
        Path testsDirectory = workspace.resolve("tests");
        Path exerciseDirectory = studentsDirectory.resolve("student_01/PAC2Ex1");
        Path packageDirectory = exerciseDirectory.resolve("src/main/java/edu/uoc/pac2");
        Path testSourceDirectory = testsDirectory.resolve("PAC2Ex1");
        Path commandLog = workspace.resolve("commands.log");
        Path wrapper = workspace.resolve("gradlew");

        Files.createDirectories(packageDirectory);
        Files.createDirectories(testSourceDirectory);
        Files.createDirectories(studentsDirectory);
        Files.createDirectories(studentsDirectory.resolve(".idea"));
        Files.write(studentsDirectory.resolve("0-file"), new byte[0]);
        Files.write(studentsDirectory.resolve(".DS_Store"), new byte[0]);
        Files.createDirectories(exerciseDirectory.resolve(".idea"));
        Files.write(exerciseDirectory.resolve(".DS_Store"), new byte[0]);
        Files.write(exerciseDirectory.resolve("build.gradle"), new byte[0]);
        Files.write(packageDirectory.resolve("StudentCode.java"), new byte[0]);
        Files.write(testSourceDirectory.resolve("OriginalTest.java"), new byte[0]);
        Files.write(wrapper, ("#!/bin/sh\nprintf '%s\\n' \"$@\" >> \""
                + commandLog + "\"\n").getBytes(StandardCharsets.UTF_8));
        assertTrue(wrapper.toFile().setExecutable(true));

        Configuration configuration = new Configuration();
        configuration.setDirectoryStudentsPACs(studentsDirectory.toString());
        configuration.setDirectoryTestsPACs(testsDirectory.toString());
        configuration.setName("PAC2");

        String originalUserDirectory = System.getProperty("user.dir");
        String originalWrapperExecutable = GradleService.GRADLEW_EXECUTABLE;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        try {
            System.setProperty("user.dir", workspace.toString());
            System.setOut(new PrintStream(capturedOutput, true, "UTF-8"));
            new GradleService(configuration).run();
        } finally {
            System.setProperty("user.dir", originalUserDirectory);
            GradleService.GRADLEW_EXECUTABLE = originalWrapperExecutable;
            System.setOut(originalOutput);
        }

        String buildFile = exerciseDirectory.resolve("build.gradle").toString();
        String listenerScript = workspace.resolve(
                "src/main/resources/gradle-test-listener.init.gradle").toString();
        assertEquals("-b\n" + buildFile + "\nclean\n-I\n" + listenerScript + "\n-b\n"
                        + buildFile + "\ntest\n",
                new String(Files.readAllBytes(commandLog), StandardCharsets.UTF_8));
        assertTrue(Files.exists(exerciseDirectory.resolve("OriginalTest.java")));
        String output = new String(capturedOutput.toByteArray(), StandardCharsets.UTF_8);
        String normalizedOutput = Pattern.compile(" Duration: [^\\r\\n]+ seconds")
                .matcher(output).replaceAll(" Duration: <elapsed> seconds");
        assertEquals(System.lineSeparator()
                        + "_______________________ 1 - student_01 _______________________"
                        + System.lineSeparator()
                        + "PAC2Ex1" + System.lineSeparator()
                        + "\tExecuting gradle CLEAN:  Duration: <elapsed> seconds"
                        + System.lineSeparator()
                        + "\tExecuting gradle  TEST:  Duration: <elapsed> seconds"
                        + System.lineSeparator(),
                normalizedOutput);
    }

    @Test
    void skipsTestsWhenPackageDirectoryCannotBeListed(@TempDir Path temporaryDirectory) throws IOException {
        Path studentsDirectory = temporaryDirectory.resolve("students");
        Path testsDirectory = temporaryDirectory.resolve("tests");
        Path exerciseDirectory = studentsDirectory.resolve("student_01/PAC2Ex1");
        Path testSourceDirectory = testsDirectory.resolve("PAC2Ex1");
        Path commandLog = temporaryDirectory.resolve("commands.log");
        Path wrapper = temporaryDirectory.resolve("gradlew");

        Files.createDirectories(exerciseDirectory);
        Files.createDirectories(testSourceDirectory);
        Files.write(exerciseDirectory.resolve("build.gradle"), new byte[0]);
        Files.write(wrapper, ("#!/bin/sh\nprintf '%s\\n' \"$@\" >> \""
                + commandLog + "\"\n").getBytes(StandardCharsets.UTF_8));
        assertTrue(wrapper.toFile().setExecutable(true));

        Configuration configuration = new Configuration();
        configuration.setDirectoryStudentsPACs(studentsDirectory.toString());
        configuration.setDirectoryTestsPACs(testsDirectory.toString());
        configuration.setName("PAC2");

        String originalUserDirectory = System.getProperty("user.dir");
        String originalWrapperExecutable = GradleService.GRADLEW_EXECUTABLE;
        try {
            System.setProperty("user.dir", temporaryDirectory.toString());
            new GradleService(configuration).run();
        } finally {
            System.setProperty("user.dir", originalUserDirectory);
            GradleService.GRADLEW_EXECUTABLE = originalWrapperExecutable;
        }

        String buildFile = exerciseDirectory.resolve("build.gradle").toString();
        assertEquals("-b\n" + buildFile + "\nclean\n",
                new String(Files.readAllBytes(commandLog), StandardCharsets.UTF_8));
    }

    @Test
    void reportsGradleCommandFailure(@TempDir Path temporaryDirectory) throws IOException {
        Path studentsDirectory = temporaryDirectory.resolve("students");
        Path testsDirectory = temporaryDirectory.resolve("tests");
        Path exerciseDirectory = studentsDirectory.resolve("student_01/PAC2Ex1");
        Path packageDirectory = exerciseDirectory.resolve("src/main/java/edu/uoc/pac2");
        Path testSourceDirectory = testsDirectory.resolve("PAC2Ex1");
        Path wrapper = temporaryDirectory.resolve("gradlew");

        Files.createDirectories(packageDirectory);
        Files.createDirectories(testSourceDirectory);
        Files.write(exerciseDirectory.resolve("build.gradle"), new byte[0]);
        Files.write(packageDirectory.resolve("StudentCode.java"), new byte[0]);
        Files.write(wrapper, "#!/bin/sh\nexit 7\n".getBytes(StandardCharsets.UTF_8));
        assertTrue(wrapper.toFile().setExecutable(true));

        Configuration configuration = new Configuration();
        configuration.setDirectoryStudentsPACs(studentsDirectory.toString());
        configuration.setDirectoryTestsPACs(testsDirectory.toString());
        configuration.setName("PAC2");

        String originalUserDirectory = System.getProperty("user.dir");
        String originalWrapperExecutable = GradleService.GRADLEW_EXECUTABLE;
        try {
            System.setProperty("user.dir", temporaryDirectory.toString());
            IOException failure = assertThrows(IOException.class,
                    () -> new GradleService(configuration).run());
            assertTrue(failure.getMessage().contains("exit code 7"));
        } finally {
            System.setProperty("user.dir", originalUserDirectory);
            GradleService.GRADLEW_EXECUTABLE = originalWrapperExecutable;
        }

    }

    @Test
    void continuesToNextExerciseWhenTestTaskReturnsFailure(@TempDir Path temporaryDirectory)
            throws IOException {
        Path studentsDirectory = temporaryDirectory.resolve("students");
        Path testsDirectory = temporaryDirectory.resolve("tests");
        Path firstExercise = studentsDirectory.resolve("student_01/PAC2Ex1");
        Path secondExercise = studentsDirectory.resolve("student_01/PAC2Ex2");
        Path commandLog = temporaryDirectory.resolve("commands.log");
        Path wrapper = temporaryDirectory.resolve("gradlew");

        for (Path exercise : new Path[]{firstExercise, secondExercise}) {
            Files.createDirectories(exercise.resolve("src/main/java/edu/uoc/pac2"));
            Files.createDirectories(testsDirectory.resolve(exercise.getFileName()));
            Files.write(exercise.resolve("build.gradle"), new byte[0]);
            Files.write(exercise.resolve("src/main/java/edu/uoc/pac2/StudentCode.java"), new byte[0]);
        }
        Files.write(wrapper, ("#!/bin/sh\nprintf '%s\\n' \"$@\" >> \""
                + commandLog + "\"\n"
                + "last=\nfor argument do last=\"$argument\"; done\n"
                + "if [ \"$last\" = \"test\" ]; then exit 1; fi\n")
                .getBytes(StandardCharsets.UTF_8));
        assertTrue(wrapper.toFile().setExecutable(true));

        Configuration configuration = new Configuration();
        configuration.setDirectoryStudentsPACs(studentsDirectory.toString());
        configuration.setDirectoryTestsPACs(testsDirectory.toString());
        configuration.setName("PAC2");

        String originalUserDirectory = System.getProperty("user.dir");
        String originalWrapperExecutable = GradleService.GRADLEW_EXECUTABLE;
        try {
            System.setProperty("user.dir", temporaryDirectory.toString());
            new GradleService(configuration).run();
        } finally {
            System.setProperty("user.dir", originalUserDirectory);
            GradleService.GRADLEW_EXECUTABLE = originalWrapperExecutable;
        }

        String commands = new String(Files.readAllBytes(commandLog), StandardCharsets.UTF_8);
        assertEquals(2, commands.lines().filter("clean"::equals).count());
        assertEquals(2, commands.lines().filter("test"::equals).count());
    }

    @Test
    void reportsMissingStudentDirectoryWithoutNullPointerException(@TempDir Path temporaryDirectory) {
        Configuration configuration = new Configuration();
        Path missingStudentsDirectory = temporaryDirectory.resolve("missing-students");
        configuration.setDirectoryStudentsPACs(missingStudentsDirectory.toString());

        IOException failure = assertThrows(IOException.class,
                () -> new GradleService(configuration).run());

        assertTrue(failure.getMessage().contains(missingStudentsDirectory.toString()));
    }
}
