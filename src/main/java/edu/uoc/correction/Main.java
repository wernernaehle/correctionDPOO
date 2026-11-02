package edu.uoc.correction;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.service.GradleService;
import edu.uoc.correction.service.ResultService;

import java.io.IOException;
import java.util.List;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) throws IOException {

        Configuration configuration = new Configuration();

        GradleService gradleService = new GradleService(configuration);
        gradleService.run();

        ResultService resultService = new ResultService(configuration);
        List<Student> students = resultService.parseXmlTests();
        resultService.html(students);
        resultService.zips();
    }
}
