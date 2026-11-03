package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import net.lingala.zip4j.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportArchiveServiceTest {

    @Test
    void createsAnArchiveForEachReportDirectory(@TempDir Path temporaryDirectory) throws IOException {
        Path studentReportDirectory = temporaryDirectory.resolve("student_PEC_example");
        Files.createDirectories(studentReportDirectory);
        Files.write(studentReportDirectory.resolve("index.html"),
                "<html>report</html>".getBytes(StandardCharsets.UTF_8));

        Configuration configuration = new Configuration();
        configuration.setDirectoryReportsPACs(temporaryDirectory.toString());

        new ReportArchiveService(configuration).createArchives(
                Collections.singletonMap("student_PEC_example", 1.25));

        Path archive = temporaryDirectory.resolve("student_PEC_example.zip");
        assertTrue(Files.exists(archive));
        String passwordFile = new String(
                Files.readAllBytes(temporaryDirectory.resolve("PASSWORD.txt")), StandardCharsets.UTF_8);
        assertTrue(passwordFile.contains("Estudiante: student"));
        assertTrue(passwordFile.contains("Tu nota numérica es un 1.25."));
        Matcher passwordMatcher = Pattern.compile("Password: (\\w+)").matcher(passwordFile);
        assertTrue(passwordMatcher.find());
        Path extractedDirectory = temporaryDirectory.resolve("extracted");
        new ZipFile(archive.toFile(), passwordMatcher.group(1).toCharArray())
                .extractAll(extractedDirectory.toString());
        assertEquals("<html>report</html>", new String(Files.readAllBytes(
                extractedDirectory.resolve("student_PEC_example/index.html")), StandardCharsets.UTF_8));
    }
}
