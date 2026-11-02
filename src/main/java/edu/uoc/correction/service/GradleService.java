package edu.uoc.correction.service;
import edu.uoc.correction.model.Configuration;

import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class GradleService {

    public static String GRADLEW_EXECUTABLE;
    Configuration configuration;

    public GradleService(Configuration configuration){
        this.configuration = configuration;
    }

    private void setGradlewExecutableOS(){
        if(System.getProperty("os.name").startsWith("Windows")){
            GRADLEW_EXECUTABLE = System.getProperty("user.dir")+"/gradlew.bat";
        }else{
            GRADLEW_EXECUTABLE = System.getProperty("user.dir")+"/gradlew";
        }
    }

    private void copyOriginalTests(String exerciseName, String url) throws IOException{
        if (!exerciseName.equals("PAC3Ex4")) {
            exerciseName = exerciseName.endsWith("_sol") ? exerciseName.split("_")[0] : exerciseName;
            File srcDir = new File(configuration.getDirectoryTestsPACs() + "/" + exerciseName.trim());
            File destDir = new File(url);
            FileUtils.copyDirectory(srcDir, destDir);
        }
    }

    private void myExecRuntime(String[] command) throws IOException {
        long init = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec(command/*, null, new File("/Users")*/);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(" Duration: " + (double) ((end - init)/1000) +" seconds");
        //System.out.println("OK");
        p.destroy();
    }

    private void myExecRuntime2(String[] command) throws IOException {
        long init = System.currentTimeMillis();
        try {
            Process process = new ProcessBuilder(command).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    //System.out.println(line);
                }
            }
            int exitCode = process.waitFor();
            //System.out.println("Exit Code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(" Duration: " + (double) ((end - init)/1000) +" seconds");
    }

    public void run() throws IOException{

        setGradlewExecutableOS();

        File folder = new File(configuration.getDirectoryStudentsPACs());
        int cont = 1;

        //TODO maybe performance problem
        File[] listExercisesDirectory = Arrays.stream(Objects.requireNonNull(folder.listFiles())).filter(Predicate.not(File::isHidden)).toArray(File[]::new);
        //File[] listExercisesDirectory = folder.listFiles();
        assert listExercisesDirectory != null;
        Arrays.sort(listExercisesDirectory, Comparator.comparing(File::getName));

        for (File studentDirectory : listExercisesDirectory) {
            if(studentDirectory.isDirectory()/* && !studentDirectory.isHidden()*/) {

                String studentName = studentDirectory.getName().split("_")[0]+"_"+studentDirectory.getName().split("_")[1];
                System.out.println(System.lineSeparator() + "_______________________ " + cont + " - " + studentName + " _______________________");

                //TODO maybe performance problem
                //File[] listExercises = studentDirectory.listFiles();
                File[] listExercises = Arrays.stream(studentDirectory.listFiles()).filter(Predicate.not(File::isHidden)).toArray(File[]::new);
                assert listExercises != null;
                Arrays.sort(listExercises, Comparator.comparing(File::getName));

                for (File exercise : listExercises) {
                    System.out.println(exercise.getName());
                    boolean existGradleFile = new File(exercise.getAbsolutePath()+"/build.gradle").exists();
                    if(existGradleFile){

                        //Copy test files
                        copyOriginalTests(exercise.getName(), exercise.getAbsolutePath());

                        //Gradle clean
                        //String[] commandClean = new String[] {GRADLEW_EXECUTABLE + " -b " + exercise.getAbsolutePath() + "/build.gradle clean"};
                        //String[] commandClean = new String[] {GRADLEW_EXECUTABLE, " -b ", exercise.getAbsolutePath() + "/build.gradle", " clean"};
                        String[] commandClean = new String[] {GRADLEW_EXECUTABLE, "-b", exercise.getAbsolutePath() + "/build.gradle", "clean"};
                        System.out.print("\tExecuting gradle CLEAN: ");
                        //System.out.println("DISABLED until repairing");
                        //myExecRuntime(commandClean);
                        myExecRuntime2(commandClean);

                        //Gradle test
                        //String[] commandTest = new String[] {GRADLEW_EXECUTABLE + " -b " + exercise.getAbsolutePath() + "/build.gradle test"};
                        //String[] commandTest = new String[] {GRADLEW_EXECUTABLE, " -b ", exercise.getAbsolutePath() + "/build.gradle", " test"};
                        //String[] commandTest = new String[] {GRADLEW_EXECUTABLE, "-b " + exercise.getAbsolutePath() + "/build.gradle test"};
                        String testListener = System.getProperty("user.dir")
                                + "/src/main/resources/gradle-test-listener.init.gradle";
                        String[] commandTest = new String[] {GRADLEW_EXECUTABLE, "-I", testListener,
                                "-b", exercise.getAbsolutePath() + "/build.gradle", "test"};
                        System.out.print("\tExecuting gradle  TEST: ");
                        //System.out.println("DISABLED until repairing");
                        //myExecRuntime(commandTest);
                        myExecRuntime2(commandTest);
                    }
                }
            }
            cont++;
        }
    }
}
