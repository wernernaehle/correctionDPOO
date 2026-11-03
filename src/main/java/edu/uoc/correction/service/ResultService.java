package edu.uoc.correction.service;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.ExerciceConfiguration;
import edu.uoc.correction.model.Exercise;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.model.TestNote;
import edu.uoc.correction.model.Testsuite;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Werner Nähle
 * @version 1.0
 */
public class ResultService {

    private static final String DIRECTORY_TEST_RESULTS_XML = "/build/test-results/test/";
    private static final Logger LOGGER = Logger.getLogger(ResultService.class.getName());
    private final Configuration configuration;
    private final HtmlReportService htmlReportService;
    private final ReportArchiveService reportArchiveService;
    private Map<String, Double> studentScores;

    public ResultService(Configuration configuration){
        this.configuration = configuration;
        this.htmlReportService = new HtmlReportService(configuration);
        this.reportArchiveService = new ReportArchiveService(configuration);
    }

    public List<Student> parseXmlTests() {

        System.out.println(System.lineSeparator() + "############ Parser TEST Results ############");

        List<Student> students = new ArrayList<>();

        File folder = new File(configuration.getDirectoryStudentsPACs());
        File[] studentDirectories = folder.listFiles(File::isDirectory);
        if (studentDirectories == null) {
            throw new IllegalStateException("Could not list student directory: " + folder.getAbsolutePath());
        }
        studentDirectories = Arrays.stream(studentDirectories)
                .filter(studentDirectory -> !studentDirectory.isHidden())
                .toArray(File[]::new);
        Arrays.sort(studentDirectories);

        for (File studentDirectory : studentDirectories) {
            students.add(parseStudent(studentDirectory));
        }
        System.out.println("OK");
        return students;
    }

    private Student parseStudent(File studentDirectory) {
        String studentName= studentDirectory.getName();
        Student student = new Student();
        student.setName(studentName);

        //create student directory
        File dirResultStudent = new File(configuration.getDirectoryReportsPACs() +"/"+ studentName);
        if (!dirResultStudent.mkdirs() && !dirResultStudent.isDirectory()) {
            throw new IllegalStateException("Could not create student report directory: "
                    + dirResultStudent.getAbsolutePath());
        }

        File[] exerciseDirectories = studentDirectory.listFiles(
                file -> file.isDirectory() && !file.isHidden());
        if (exerciseDirectories == null) {
            throw new IllegalStateException("Could not list exercises for student: "
                    + studentDirectory.getAbsolutePath());
        }
        Arrays.sort(exerciseDirectories, Comparator.comparing(File::getName));

        List<Exercise> exercises = new ArrayList<>();

        for (File exerciseDirectory : exerciseDirectories) {
            parseExercise(exerciseDirectory, studentName, student, exercises);
        }

        student.setExercises(exercises);
        return student;
    }

    private void parseExercise(File exerciseDirectory, String studentName, Student student,
                               List<Exercise> exercises) {
        Exercise exercise =  new Exercise();
        exercise.setName(exerciseDirectory.getName());
        List<File> testResultFiles = new ArrayList<>();
        File projectDirectory = new File(exerciseDirectory.getAbsolutePath());
        if(projectDirectory.exists()){

            File srcDir = new File(exerciseDirectory.getAbsolutePath() + "/build/reports/tests/test");
            File destDir = new File(configuration.getDirectoryReportsPACs() +"/"+ studentName +"/"+ exercise.getName());

            exercise.setReportUrl("./" + exercise.getName() + "/index.html");
            boolean reportCopyFailed = false;
            try {
                FileUtils.copyDirectory(srcDir, destDir);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not copy test report from " + srcDir.getAbsolutePath(), e);
                reportCopyFailed = true;
            }

            File folderXmlFiles = new File(exerciseDirectory.getAbsolutePath() + DIRECTORY_TEST_RESULTS_XML);

            if(folderXmlFiles.exists()){
                File[] xmlFiles = folderXmlFiles.listFiles();
                if (xmlFiles == null) {
                    throw new IllegalStateException("Could not list test results in "
                            + folderXmlFiles.getAbsolutePath());
                }
                for (File xmlFile : xmlFiles) {
                    if (!xmlFile.isDirectory() && xmlFile.getName().endsWith(".xml")) {
                        testResultFiles.add(xmlFile);
                    }
                }
                File invocationResultsFile = new File(exerciseDirectory,
                        "build/test-results/correction-methods/test.tsv");
                List<ParameterizedTestAggregator.InvocationResult> invocationResults =
                        new ParameterizedTestAggregator().readInvocationResults(invocationResultsFile);
                List<Testsuite> testSuites = this.parseTestSuites(
                        exerciseDirectory.getName(), testResultFiles, invocationResults);
                exercise.setTestsuiteList(testSuites);
                exercises.add(exercise);
            }else{
                student.setComment("Compile ERROR");
                if (reportCopyFailed) {
                    exercises.add(exercise);
                }
            }
        }else{
            student.setComment("Exercise NP");
        }
    }

    private List<Testsuite> parseTestSuites(String exerciseName, List<File> testResultFiles,
                                            List<ParameterizedTestAggregator.InvocationResult> invocationResults){
        List<Testsuite> testSuites = new ArrayList<>();
        ExerciceConfiguration exerciceConfiguration = configuration.getMapExercisesTests().get(exerciseName);
        if (exerciceConfiguration == null || exerciceConfiguration.getListTestNote() == null) {
            throw new IllegalStateException("No test score configuration found for exercise " + exerciseName);
        }
        warnForIncorrectConfiguredTotals(exerciceConfiguration, exerciseName);
        ParameterizedTestAggregator aggregator = new ParameterizedTestAggregator();
        for(File xmlFile: testResultFiles){
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Testsuite.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Testsuite testSuite = (Testsuite) jaxbUnmarshaller.unmarshal(xmlFile);
                for(TestNote current: exerciceConfiguration.getListTestNote()){
                    if(testSuite.getName().endsWith("." + current.getTestName())
                            || testSuite.getName().equals(current.getTestName())){
                        testSuite.setScore(current.getScore());
                        break;
                    }
                }
                for (String warning : aggregator.aggregate(
                        testSuite, invocationResults, exerciceConfiguration)) {
                    LOGGER.warning(warning);
                }
                testSuites.add(testSuite);

            } catch (JAXBException e) {
                LOGGER.log(Level.SEVERE, "Could not parse test result file: " + xmlFile.getAbsolutePath(), e);
            }
        }
        warnForMissingTestClasses(exerciseName, exerciceConfiguration, testSuites);
        return testSuites;
    }

    private void warnForIncorrectConfiguredTotals(ExerciceConfiguration exerciceConfiguration,
                                                   String exerciseName) {
        if (!exerciceConfiguration.isScoreByMethods()) {
            return;
        }
        for (TestNote testNote : exerciceConfiguration.getListTestNote()) {
            if (testNote.getMethodsNote() == null || testNote.getMethodsNote().isEmpty()) {
                continue;
            }
            double methodScoreTotal = 0;
            for (Double methodScore : testNote.getMethodsNote().values()) {
                if (methodScore != null) {
                    methodScoreTotal += methodScore;
                }
            }
            if (!Double.isFinite(methodScoreTotal) || !Double.isFinite(testNote.getScore())
                    || Math.abs(methodScoreTotal - testNote.getScore()) > 0.0001) {
                LOGGER.warning("WARN: Method scores for " + testNote.getTestName() + " in "
                        + exerciseName + " total " + methodScoreTotal
                        + ", but the declared class total is " + testNote.getScore());
            }
        }
    }

    private void warnForMissingTestClasses(String exerciseName,
                                           ExerciceConfiguration exerciceConfiguration,
                                           List<Testsuite> testSuites) {
        for (TestNote testNote : exerciceConfiguration.getListTestNote()) {
            if (testNote.getMethodsNote() == null || testNote.getMethodsNote().isEmpty()) {
                continue;
            }
            boolean classExecuted = false;
            for (Testsuite testSuite : testSuites) {
                if (testSuite.getName().endsWith("." + testNote.getTestName())
                        || testSuite.getName().equals(testNote.getTestName())) {
                    classExecuted = true;
                    break;
                }
            }
            if (!classExecuted) {
                for (String methodName : testNote.getMethodsNote().keySet()) {
                    LOGGER.warning("WARN: '" + methodName + "' no encontrado en "
                            + testNote.getTestName() + " (" + exerciseName + ")");
                }
            }
        }
    }

    public void html(List<Student> students){
        studentScores = htmlReportService.generate(students);
    }

    public void zips(){
        reportArchiveService.createArchives(studentScores);
    }
}
