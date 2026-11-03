package edu.uoc.correction.service;
import edu.uoc.correction.model.Configuration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Werner Nähle
 * @version 1.0
 */
public class GradleService {

    private static final Logger LOGGER = Logger.getLogger(GradleService.class.getName());

    public static String GRADLEW_EXECUTABLE;
    private final Configuration configuration;

    public GradleService(Configuration configuration){
        this.configuration = configuration;
    }

    private void configureGradleWrapperExecutableForCurrentOS(){
        if(System.getProperty("os.name").startsWith("Windows")){
            GRADLEW_EXECUTABLE = System.getProperty("user.dir")+"/gradlew.bat";
        }else{
            GRADLEW_EXECUTABLE = System.getProperty("user.dir")+"/gradlew";
        }
    }

    private void copyOriginalTests(String exerciseName, String exerciseDirectory) throws IOException{
        exerciseName = exerciseName.endsWith("_sol") ? exerciseName.split("_")[0] : exerciseName;
        File srcDir = new File(configuration.getDirectoryTestsPACs() + "/" + exerciseName);
        File destDir = new File(exerciseDirectory);
        FileUtils.copyDirectory(srcDir, destDir);
    }

    private void executeGradleCommand(String... command) throws IOException {
        long startTime = System.currentTimeMillis();
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process process = processBuilder.start();
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
            try {
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException interruptedException) {
                process.destroyForcibly();
                e.addSuppressed(interruptedException);
            }
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for Gradle command", e);
        }
        long endTime = System.currentTimeMillis();
        System.out.println(" Duration: " + (double) ((endTime - startTime)/1000) +" seconds");
        process.destroy();
        if (exitCode != 0) {
            throw new IOException("Gradle command failed with exit code " + exitCode
                    + ": " + String.join(" ", command));
        }
    }

    public void run() throws IOException{

        configureGradleWrapperExecutableForCurrentOS();

        String studentsPath = configuration.getDirectoryStudentsPACs();
        if (studentsPath == null || studentsPath.trim().isEmpty()) {
            throw new IOException("Student directory is not configured.");
        }
        File studentsDirectory = new File(studentsPath);
        if (!studentsDirectory.isDirectory()) {
            throw new IOException("Student directory does not exist or is not a directory: "
                    + studentsDirectory.getAbsolutePath());
        }
        int studentNumber = 1;

        File[] studentDirectories = studentsDirectory.listFiles(
                file -> file.isDirectory() && !file.isHidden());
        if (studentDirectories == null) {
            throw new IOException("Could not list student directory: " + studentsDirectory.getAbsolutePath());
        }
        Arrays.sort(studentDirectories, Comparator.comparing(File::getName));

        for (File studentDirectory : studentDirectories) {
            processStudentDirectory(studentDirectory, studentNumber);
            studentNumber++;
        }
    }

    private void processStudentDirectory(File studentDirectory, int studentNumber) throws IOException {
        String studentName = studentDirectory.getName().split("_")[0]+"_"+studentDirectory.getName().split("_")[1];
        System.out.println(System.lineSeparator() + "_______________________ " + studentNumber + " - " + studentName + " _______________________");

        File[] exerciseDirectories = studentDirectory.listFiles(
                file -> file.isDirectory() && !file.isHidden());
        if (exerciseDirectories == null) {
            throw new IOException("Could not list exercises for student: "
                    + studentDirectory.getAbsolutePath());
        }
        Arrays.sort(exerciseDirectories, Comparator.comparing(File::getName));

        for (File exerciseDirectory : exerciseDirectories) {
            processExercise(exerciseDirectory);
        }
    }

    private void processExercise(File exercise) throws IOException {
        System.out.println(exercise.getName());
        boolean existGradleFile = new File(exercise.getAbsolutePath()+"/build.gradle").exists();
        if(existGradleFile){

            //Copy test files
            copyOriginalTests(exercise.getName(), exercise.getAbsolutePath());

            //Gradle clean
            String buildFile = exercise.getAbsolutePath()+"/build.gradle";
            System.out.print("\tExecuting gradle CLEAN: ");
            executeGradleCommand(GRADLEW_EXECUTABLE, "-b", buildFile, "clean");

            //check is package not void (Windows problem)
            File dirPackage = new File(exercise.getAbsolutePath()+"/src/main/java/edu/uoc/"+configuration.getName().toLowerCase());
            File[] packageFiles = dirPackage.listFiles();
            if(packageFiles != null && packageFiles.length > 0){
                //Gradle test
                System.out.print("\tExecuting gradle  TEST: ");
                String listenerScript = Paths.get(System.getProperty("user.dir"),
                        "src/main/resources/gradle-test-listener.init.gradle").toString();
                executeGradleCommand(GRADLEW_EXECUTABLE, "-I", listenerScript,
                        "-b", buildFile, "test");
            } else if (packageFiles == null) {
                LOGGER.warning("Could not list source package directory: " + dirPackage.getAbsolutePath());
            }
        }
    }
}
