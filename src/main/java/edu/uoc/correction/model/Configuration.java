package edu.uoc.correction.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class Configuration {
    private String directoryStudentsPACs;
    private String directoryReportsPACs;
    private String directoryTestsPACs;
    private String name;
    TreeMap<String, ExerciceConfiguration> mapExercisesTests;
    private double scorePAC;

    public Configuration() {
        mapExercisesTests = new TreeMap<>();

        Scanner sc = null;
        double notePAC = 0;
        try {
            sc = new Scanner(new File(System.getProperty("user.dir") + "/src/main/resources/config.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert sc != null;
        this.setDirectoryStudentsPACs(sc.nextLine().split("=")[1].trim());
        this.setDirectoryReportsPACs(sc.nextLine().split("=")[1].trim());
        this.setDirectoryTestsPACs(sc.nextLine().split("=")[1].trim());
        this.setName(sc.nextLine().split("=")[1].trim());

        String exerciseName = "";
        while(sc.hasNext()) {

            String[] splitTestClasses = sc.nextLine().split(";");
            for(String testClass : splitTestClasses){

                String[] splitMethodsNote = testClass.split("#");
                String tests = splitMethodsNote[0];
                String methods = splitMethodsNote.length == 2 ? splitMethodsNote[1] : null;


                String[] splitTests;
                String[] split = tests.split("=");
                if(tests.contains("=")){
                    exerciseName = split[0].trim();
                    splitTests = split[1].split(",");
                }else{
                    splitTests = split[0].split(",");

                }



                ExerciceConfiguration exerciceConfiguration = new ExerciceConfiguration();
                List<TestNote> listTestNotes = new ArrayList<>();
                for(String test : splitTests){

                    String[] splitTestNote = test.split(":");
                    TestNote testNote = new TestNote();
                    testNote.setTestName(splitTestNote[0].trim());
                    testNote.setScore(Double.parseDouble(splitTestNote[1].trim()));

                    if(methods != null){
                        exerciceConfiguration.setScoreByMethods(true);
                        for(String method : splitMethodScores(methods)){
                            int idx = method.lastIndexOf(":");
                            String methodName = method.substring(0, idx).trim();
                            Double methodScore = Double.parseDouble(method.substring(idx + 1).trim());
                            testNote.getMethodsNote().put(methodName, methodScore);
                        }
                    }

                    listTestNotes.add(testNote);
                    notePAC += testNote.getScore();

                }
                //Changed by Werner
                //exerciceConfiguration.setListTestNote(listTestNotes);
                //this.getMapExercisesTests().put(exerciseName, exerciceConfiguration);
                if (this.getMapExercisesTests().containsKey(exerciseName)) {
                    this.getMapExercisesTests().get(exerciseName).getListTestNote().add(listTestNotes.get(0));
                } else {
                    exerciceConfiguration.setListTestNote(listTestNotes);
                    this.getMapExercisesTests().put(exerciseName, exerciceConfiguration);
                }
            }

        }
        this.setScorePAC(notePAC);

    }

    public String getDirectoryStudentsPACs() {
        return directoryStudentsPACs;
    }

    public void setDirectoryStudentsPACs(String directoryStudentsPACs) {
        this.directoryStudentsPACs = directoryStudentsPACs;
    }

    public String getDirectoryReportsPACs() {
        return directoryReportsPACs;
    }

    public void setDirectoryReportsPACs(String directoryReportsPACs) {
        this.directoryReportsPACs = directoryReportsPACs;
    }

    public String getDirectoryTestsPACs() {
        return directoryTestsPACs;
    }

    public void setDirectoryTestsPACs(String directoryTestsPACs) {
        this.directoryTestsPACs = directoryTestsPACs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<String, ExerciceConfiguration> getMapExercisesTests() {
        return mapExercisesTests;
    }

    /*public void setMapExercisesTests(TreeMap<String, List<TestNote>> mapExercisesTests) {
        this.mapExercisesTests = mapExercisesTests;
    }*/

    public double getScorePAC() {
        return scorePAC;
    }

    public void setScorePAC(double scorePAC) {
        this.scorePAC = scorePAC;
    }

    private List<String> splitMethodScores(String methods) {
        List<String> parsedMethods = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String token : methods.split(",")) {
            if (current.length() > 0) {
                current.append(", ");
            }
            current.append(token.trim());

            String candidate = current.toString();
            int idx = candidate.lastIndexOf(":");
            if (idx > 0) {
                String scorePart = candidate.substring(idx + 1).trim();
                try {
                    Double.parseDouble(scorePart);
                    parsedMethods.add(candidate);
                    current.setLength(0);
                } catch (NumberFormatException ignored) {
                    // Keep accumulating fragments until we have a valid trailing numeric score.
                }
            }
        }

        return parsedMethods;
    }
}
