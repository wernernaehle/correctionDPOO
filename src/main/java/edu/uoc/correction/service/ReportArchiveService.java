package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ReportArchiveService {

    private static final Logger LOGGER = Logger.getLogger(ReportArchiveService.class.getName());

    private final Configuration configuration;

    ReportArchiveService(Configuration configuration) {
        this.configuration = configuration;
    }

    void createArchives(Map<String, Double> studentScores) {
        System.out.println(System.lineSeparator() + "############ Generate ZIP files ############");
        File reportsDirectory = new File(configuration.getDirectoryReportsPACs());
        File[] reportDirectories = reportsDirectory.listFiles();
        if (reportDirectories == null) {
            throw new IllegalStateException("Could not list report directory: "
                    + reportsDirectory.getAbsolutePath());
        }
        Arrays.sort(reportDirectories, Comparator.comparing(File::getName));

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        Path passwordFile = Paths.get(configuration.getDirectoryReportsPACs(), "PASSWORD.txt");
        try (BufferedWriter passwordWriter = Files.newBufferedWriter(passwordFile,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (File reportDirectory : reportDirectories) {
                if (reportDirectory.isDirectory()) {
                    createArchive(reportDirectory, zipParameters, studentScores, passwordWriter);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not write ZIP password information to "
                    + passwordFile.toAbsolutePath(), e);
            throw new IllegalStateException("Could not write ZIP password information to "
                    + passwordFile.toAbsolutePath(), e);
        }
    }

    private void createArchive(File reportDirectory, ZipParameters zipParameters,
                               Map<String, Double> studentScores, BufferedWriter passwordWriter)
            throws IOException {
        String studentName = reportDirectory.getName();
        Double score = studentScores == null ? null : studentScores.get(studentName);
        if (score == null) {
            System.out.println("[WARNING] Skipping report directory without a current student score: "
                    + studentName);
            return;
        }

        String password = RandomStringUtils.randomAlphanumeric(10);
        Path archivePath = Paths.get(configuration.getDirectoryReportsPACs(), studentName + ".zip");
        try (ZipFile zipFile = new ZipFile(archivePath.toFile(), password.toCharArray())) {
            zipFile.addFolder(reportDirectory, zipParameters);
        } catch (ZipException e) {
            LOGGER.log(Level.SEVERE, "Could not create ZIP for report directory "
                    + reportDirectory.getAbsolutePath(), e);
            throw new IllegalStateException("Could not create ZIP for report directory "
                    + reportDirectory.getAbsolutePath(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not close ZIP for report directory "
                    + reportDirectory.getAbsolutePath(), e);
            throw new IllegalStateException("Could not close ZIP for report directory "
                    + reportDirectory.getAbsolutePath(), e);
        }

        String[] nameParts = studentName.split("_PEC_");
        String username = nameParts[0];
        String lineSeparator = System.lineSeparator();
        double canvasGrade = score * configuration.getCanvasGradeMultiplier() / 10.0;
        passwordWriter.write("Estudiante: " + username + lineSeparator
                + "Fichero: " + studentName + ".zip" + lineSeparator
                + "Password: " + password + lineSeparator
                + "Nota para Canvas (evaluación continua): "
                + String.format(Locale.ROOT, "%.2f", canvasGrade) + lineSeparator
                + "Mensaje:" + lineSeparator
                + "-----" + lineSeparator
                + "Hola," + lineSeparator + lineSeparator
                + "Tu nota numérica es un " + String.format(Locale.ROOT, "%.2f", score)
                + ". Te adjunto un fichero .zip que contiene el informe en HTML del resultado de la "
                + "ejecución de los tests, calidad y la nota obtenida. Para descomprimir el informe, "
                + "debes usar el password \"" + password + "\"." + lineSeparator + lineSeparator
                + "Te recomiendo comparar y analizar tus soluciones con las proporcionadas. "
                + "Esto te ayudará a seguir mejorando!!" + lineSeparator + lineSeparator
                + "-----" + lineSeparator + lineSeparator);
    }
}
