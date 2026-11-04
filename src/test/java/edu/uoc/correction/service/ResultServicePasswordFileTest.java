package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.Student;
import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultServicePasswordFileTest {

    @Test
    void writesPersonalizedPasswordFileAndCreatesEncryptedZip(@TempDir Path temporaryDirectory)
            throws Exception {
        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());

        Student student = new Student();
        student.setName("student_PEC_test");
        student.setExercises(Collections.emptyList());
        Files.createDirectory(temporaryDirectory.resolve(student.getName()));

        ResultService resultService = new ResultService(configuration);
        resultService.html(Collections.singletonList(student));
        resultService.zips();

        String passwordFile = new String(
                Files.readAllBytes(temporaryDirectory.resolve("PASSWORD.txt")), StandardCharsets.UTF_8);
        assertTrue(passwordFile.contains("Estudiante: student"));
        assertTrue(passwordFile.contains("Tu nota numérica es un 0.00. Te adjunto un fichero .zip"));
        assertTrue(passwordFile.contains("tests, calidad y la nota obtenida."));
        assertTrue(!passwordFile.contains("imagen adjunta"));
        assertTrue(passwordFile.contains("Te recomiendo comparar y analizar tus soluciones"));
        assertTrue(!passwordFile.contains("<changeme>"));

        Matcher passwordMatcher = Pattern.compile("Password: (\\w+)").matcher(passwordFile);
        assertTrue(passwordMatcher.find());
        String password = passwordMatcher.group(1);
        assertTrue(passwordFile.contains("usar el password \"" + password + "\"."));

        Path extractedDirectory = temporaryDirectory.resolve("extracted");
        new ZipFile(temporaryDirectory.resolve("student_PEC_test.zip").toFile(), password.toCharArray())
                .extractAll(extractedDirectory.toString());
        assertTrue(Files.exists(extractedDirectory.resolve("student_PEC_test/index.html")));
    }
}
