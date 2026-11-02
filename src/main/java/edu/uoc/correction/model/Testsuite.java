package edu.uoc.correction.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Fran Manez
 * @version 1.0
 */
@XmlRootElement(name = "testsuite")
@XmlAccessorType(XmlAccessType.FIELD)
public class Testsuite implements Serializable {
    @XmlAttribute
    private String name;

    @XmlAttribute
    private Integer tests;

    @XmlAttribute
    private Integer skipped;

    @XmlAttribute
    private Integer failures;

    @XmlAttribute
    private Integer errors;

    @XmlAttribute
    private Date timestamp;

    @XmlAttribute
    private double time;

    private List<Testcase> testcase;

    private int successfulTotal;

    private double score;

    public Testsuite(){
        super();
    }

    public Testsuite(String name, Integer tests, Integer skipped, Integer failures, Integer errors, Date timestamp, double time, List<Testcase> testcase) {
        this.name = name;
        this.tests = tests;
        this.skipped = skipped;
        this.failures = failures;
        this.errors = errors;
        this.timestamp = timestamp;
        this.time = time;
        this.testcase = testcase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTests() {
        return tests;
    }

    public void setTests(Integer tests) {
        this.tests = tests;
    }

    public Integer getSkipped() {
        return skipped;
    }

    public void setSkipped(Integer skipped) {
        this.skipped = skipped;
    }

    public Integer getFailures() {
        return failures;
    }

    public void setFailures(Integer failures) {
        this.failures = failures;
    }

    public Integer getErrors() {
        return errors;
    }

    public void setErrors(Integer errors) {
        this.errors = errors;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public List<Testcase> getTestcase() {
        return testcase;
    }

    public void setTestcase(List<Testcase> testcase) {
        this.testcase = testcase;
    }

    public int getSuccessfulTotal() {
        return successfulTotal;
    }

    public void setSuccessfulTotal(int successfulTotal) {
        this.successfulTotal = successfulTotal;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "TestSuite [name=" + name + ", tests=" + tests + ", \ntestcase=" + testcase + "]";
    }
}
