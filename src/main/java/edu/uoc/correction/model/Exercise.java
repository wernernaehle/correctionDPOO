package edu.uoc.correction.model;

import java.util.List;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class Exercise {
    private String name;
    private List<Testsuite> testsuiteList;
    private String reportUrl;
    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Testsuite> getTestsuiteList() {
        return testsuiteList;
    }

    public void setTestsuiteList(List<Testsuite> testsuiteList) {
        this.testsuiteList = testsuiteList;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
