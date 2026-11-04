package edu.uoc.correction;

import edu.uoc.correction.model.Configuration;
import edu.uoc.correction.model.Student;
import edu.uoc.correction.service.CodeQualityEvaluator;
import edu.uoc.correction.service.GradleService;
import edu.uoc.correction.service.JavadocEvaluator;
import edu.uoc.correction.service.ResultService;

import java.io.IOException;
import java.util.List;

/**
 * @author Werner Nähle
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) throws IOException {

        Configuration configuration = new Configuration();
        JavadocEvaluator javadocEvaluator = new JavadocEvaluator(configuration);
        javadocEvaluator.validateConfiguration();
        CodeQualityEvaluator codeQualityEvaluator = new CodeQualityEvaluator(configuration);
        codeQualityEvaluator.validateConfiguration();

        GradleService gradleService = new GradleService(configuration);
        gradleService.run();

        ResultService resultService = new ResultService(
                configuration, javadocEvaluator, codeQualityEvaluator);
        List<Student> students = resultService.parseXmlTests();
        resultService.html(students);
        resultService.zips();
    }
}
