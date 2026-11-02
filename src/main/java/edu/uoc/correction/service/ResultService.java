package edu.uoc.correction.service;

import edu.uoc.correction.model.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.MathTool;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class ResultService {

    private static final String DIRECTORY_TEST_RESULTS_XML = "/build/test-results/test/";
    Configuration configuration;
    private final Map<String, Double> studentScores = new HashMap<>();

    public ResultService(Configuration configuration){
        this.configuration = configuration;
    }

    public List<Student> parseXmlTests() {

        System.out.println(System.lineSeparator() + "############ Parser TEST Results ############");

        List<Student> students = new ArrayList<>();

        File folder = new File(configuration.getDirectoryStudentsPACs());
        File[] filesSorted = Arrays.stream(folder.listFiles()).filter(Predicate.not(File::isHidden)).toArray(File[]::new);
        //File[] filesSorted = folder.listFiles();
        assert filesSorted != null;
        Arrays.sort(filesSorted);

        for (File file : filesSorted) {
            String studentName= file.getName();
            Student student = new Student();
            student.setName(studentName);

            //create student directory
            File dirResultStudent = new File(configuration.getDirectoryReportsPACs() +"/"+ studentName);
            dirResultStudent.mkdir();

            //TODO maybe performance problem
            //File[] listExercisesDirectory = file.listFiles();
            File[] listExercisesDirectory = Arrays.stream(file.listFiles()).filter(Predicate.not(File::isHidden)).toArray(File[]::new);
            assert listExercisesDirectory != null;
            Arrays.sort(listExercisesDirectory, Comparator.comparing(File::getName));

            List<Exercise> listExercises = new ArrayList<>();

            for (File exerciseCurrent : listExercisesDirectory) {
                Exercise exercise =  new Exercise();
                exercise.setName(exerciseCurrent.getName());
                List<File> xmlFiles = new ArrayList<>();
                File projectDirectory = new File(exerciseCurrent.getAbsolutePath());
                if(projectDirectory.exists()){

                    File srcDir = new File(exerciseCurrent.getAbsolutePath() + "/build/reports/tests/test");
                    File destDir = new File(configuration.getDirectoryReportsPACs() +"/"+ studentName +"/"+ exercise.getName());

                    exercise.setReportUrl("./" + exercise.getName() + "/index.html");
                    try {
                        FileUtils.copyDirectory(srcDir, destDir);
                    } catch (IOException e) {
                        //exercise.setComment("Exercise Not compile or NP");
                        listExercises.add(exercise);
                        //break;

                    }

                    File folderXmlFiles = new File(exerciseCurrent.getAbsolutePath() + DIRECTORY_TEST_RESULTS_XML);

                    if(folderXmlFiles.exists()){
                        for (File xmlFile : Objects.requireNonNull(folderXmlFiles.listFiles())) {
                            if (!xmlFile.isDirectory() && xmlFile.getName().endsWith(".xml")) {
                                xmlFiles.add(xmlFile);
                                //System.out.println(xmlFile.getName());
                            }
                        }
                        File invocationResultsFile = new File(exerciseCurrent.getAbsolutePath()
                                + "/build/test-results/correction-methods/test.tsv");
                        List<ParameterizedTestAggregator.InvocationResult> invocationResults =
                                readInvocationResults(invocationResultsFile);
                        List<Testsuite> listTestsuite = this.parserTestSuite(
                                exerciseCurrent.getName(), xmlFiles, invocationResults);
                        warnForMissingTestClasses(exerciseCurrent.getName(), listTestsuite);
                        exercise.setTestsuiteList(listTestsuite);
                        listExercises.add(exercise);
                    }else{
                        //Not exist test directory, however, the exercise no compile
                        student.setComment("Compile ERROR");
                    }
                }else{
                    //Not exist project directory, however, the exercise is not present
                    student.setComment("Exercise NP");
                }
            }

            student.setExercises(listExercises);
            students.add(student);


        }
        System.out.println("OK");
        return students;
    }

    private List<Testsuite> parserTestSuite(String exerciseName, List<File> xmlFiles,
                                            List<ParameterizedTestAggregator.InvocationResult> invocationResults){
        JAXBContext jaxbContext;

        List<Testsuite> listTestsuite = new ArrayList<>();
        for(File xmlFile: xmlFiles){
            try {
                jaxbContext = JAXBContext.newInstance(Testsuite.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Testsuite testsuite = (Testsuite) jaxbUnmarshaller.unmarshal(xmlFile);
                //System.out.println(testsuite);

                //search exercise name in hashmap, and test name in list (loop)
                //List<TestNote> listTestNote = configuration.getMapExercisesTests().get(exerciseName);
                ExerciceConfiguration exerciceConfiguration = configuration.getMapExercisesTests().get(exerciseName);
                if (exerciceConfiguration != null) {
                    for(TestNote current: exerciceConfiguration.getListTestNote()){
                        String testsuiteClassName = testsuite.getName().substring(testsuite.getName().lastIndexOf(".") + 1);
                        if(testsuiteClassName.equals(current.getTestName())){
                            testsuite.setScore(current.getScore());
                            break;
                        }
                    }
                }
                ParameterizedTestAggregator.AggregationResult aggregationResult =
                        new ParameterizedTestAggregator().aggregate(
                                testsuite, invocationResults, exerciceConfiguration);
                for (String warning : aggregationResult.getWarnings()) {
                    System.out.println("[WARNING] " + warning);
                }
                listTestsuite.add(testsuite);

            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return listTestsuite;
    }

    private List<ParameterizedTestAggregator.InvocationResult> readInvocationResults(File file) {
        try {
            return new ParameterizedTestAggregator().readInvocationResults(file);
        } catch (IOException e) {
            System.out.println("[WARNING] Could not read grouped test results from "
                    + file.getAbsolutePath() + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void warnForMissingTestClasses(String exerciseName, List<Testsuite> testsuites) {
        ExerciceConfiguration exerciceConfiguration =
                configuration.getMapExercisesTests().get(exerciseName);
        if (exerciceConfiguration == null) {
            return;
        }

        for (TestNote testNote : exerciceConfiguration.getListTestNote()) {
            boolean classFound = false;
            for (Testsuite testsuite : testsuites) {
                String className = testsuite.getName().substring(testsuite.getName().lastIndexOf(".") + 1);
                if (className.equals(testNote.getTestName())) {
                    classFound = true;
                    break;
                }
            }
            if (!classFound) {
                for (String methodName : testNote.getMethodsNote().keySet()) {
                    System.out.println("[WARNING] Configured test '" + testNote.getTestName() + "."
                            + methodName + "' was not found in test results.");
                }
            }
        }
    }

    public void html(List<Student> students){

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        Template template = velocityEngine.getTemplate("src/main/resources/template/index.vm", "UTF-8");

        VelocityContext context = new VelocityContext();

        for(Student student : students){


            List<Exercise> exercises = student.getExercises();
            List<Double> notesExercises = new ArrayList<>();

            double notePAC = 0;
            //double scorePAC = 0;
            for(Exercise exercise : exercises){
                int testsTotal = 0;
                int failuresTotal = 0;
                int errorsTotal = 0;
                double scoreTotal = 0;
                double noteTotal = 0;

                if(exercise.getTestsuiteList() != null){
                    for (Testsuite testsuite : exercise.getTestsuiteList()){
                        String testClassName = testsuite.getName().substring(testsuite.getName().lastIndexOf(".") + 1);
                        if (configuration.getMapExercisesTests().get(exercise.getName()) != null) {
                            boolean isScoreByMethods = configuration.getMapExercisesTests().get(exercise.getName()).isScoreByMethods();

                            double noteTotalByMethod = 0;
                            if(isScoreByMethods){
                                for (Testcase testcase : testsuite.getTestcase()){
                                    if(testcase.getFailure() == null){
                                        for (TestNote testNote : configuration.getMapExercisesTests().get(exercise.getName()).getListTestNote()){
                                            if(testNote.getTestName().equals(testClassName) && testNote.getMethodsNote().size() > 0 && testNote.getMethodsNote().containsKey(testcase.getName())){
                                                noteTotalByMethod += testNote.getMethodsNote().get(testcase.getName());
                                            }
                                        }
                                    }
                                }
                            }

                            testsTotal = testsTotal + testsuite.getTests();
                            failuresTotal = failuresTotal + testsuite.getFailures();
                            errorsTotal = errorsTotal + testsuite.getErrors();
                            scoreTotal = scoreTotal + testsuite.getScore();

                            int success = ((testsuite.getTests() - testsuite.getFailures() - testsuite.getErrors()) * 100) / testsuite.getTests();
                            double note;
                            if(isScoreByMethods){
                                note = noteTotalByMethod;
                            }else{
                                note = (testsuite.getScore() * success)/100;
                            }

                            noteTotal = noteTotal + note;
                        }

                    }
                    notePAC = notePAC + noteTotal;
                    notesExercises.add(noteTotal);
                }
            }

            //System.out.println("NOTA PAC: "+notePAC);

            studentScores.put(student.getName(), notePAC);

            //todo fran
            for (Map.Entry<String, ExerciceConfiguration> entry : configuration.getMapExercisesTests().entrySet()) {
                String exerciseName = entry.getKey();
                for(Exercise current: exercises){
                    if(exerciseName.equals(current.getName())){
                        break;
                    }
                }
            }


            String[] splitName = student.getName().split("_PEC_");

            context.put("math", new MathTool());
            context.put("username", splitName[0]);
            context.put("pac", configuration.getName());
            context.put("exercises", exercises);
            context.put("noteExercises", notesExercises);
            context.put("notePAC", notePAC);
            context.put("scorePAC", configuration.getScorePAC());
            context.put("configurationExercises", configuration.getMapExercisesTests());


            Writer writer;
            try {
                writer = new FileWriter(configuration.getDirectoryReportsPACs() +"/"+ student.getName() + "/index.html");
                template.merge(context, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void zips() throws IOException {
        System.out.println(System.lineSeparator() + "############ Generate ZIP files ############");
        File folder = new File(configuration.getDirectoryReportsPACs());

        //TODO maybe performance problem
        //File[] corrections = folder.listFiles();
        File[] corrections = Arrays.stream(folder.listFiles()).filter(Predicate.not(File::isHidden)).toArray(File[]::new);
        assert corrections != null;
        Arrays.sort(corrections, Comparator.comparing(File::getName));

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        try (Writer passwordWriter = Files.newBufferedWriter(
                Paths.get(configuration.getDirectoryReportsPACs(), "PASSWORDS.txt"),
                StandardCharsets.UTF_8)) {
            for (File current : corrections) {
                if (current.isDirectory()) {
                    String studentName = current.getName();
                    String password = RandomStringUtils.randomAlphanumeric(10);
                    Double note = studentScores.get(studentName);
                    if (note == null) {
                        System.out.println("[WARNING] Skipping report directory without a current student score: "
                                + studentName);
                        continue;
                    }

                    ZipFile zipFile = new ZipFile(
                            Paths.get(configuration.getDirectoryReportsPACs(), studentName + ".zip").toFile(),
                            password.toCharArray());
                    try {
                        zipFile.addFolder(current, zipParameters);
                    } catch (ZipException e) {
                        throw new IllegalStateException("Could not create ZIP for student " + studentName, e);
                    }

                    String[] splitName = studentName.split("_PEC_");
                    String username = splitName[0];
                    passwordWriter.write("Estudiante: " + username + System.lineSeparator());
                    passwordWriter.write("Fichero: " + studentName + ".zip" + System.lineSeparator());
                    passwordWriter.write("Password: " + password + System.lineSeparator());
                    passwordWriter.write("Mensaje:" + System.lineSeparator());
                    passwordWriter.write("-----" + System.lineSeparator());
                    passwordWriter.write("Hola," + System.lineSeparator() + System.lineSeparator());
                    passwordWriter.write("Tu nota numérica es un "
                            + String.format(Locale.ROOT, "%.2f", note)
                            + ". Puedes ver desglosada tus puntuaciones en la imagen adjunta. Además, te adjunto un "
                            + "fichero .zip que contiene el informe en HTML del resultado de la ejecución de los "
                            + "tests y la nota obtenida. Para descomprimir el informe, debes usar el password \""
                            + password + "\"." + System.lineSeparator() + System.lineSeparator());
                    passwordWriter.write("Te recomiendo comparar y analizar tus soluciones con las proporcionadas. "
                            + "Esto te ayudará a seguir mejorando!!" + System.lineSeparator() + System.lineSeparator());
                    passwordWriter.write("Un saludo" + System.lineSeparator());
                    passwordWriter.write("-----" + System.lineSeparator() + System.lineSeparator());
                }
            }
        }
    }
}
