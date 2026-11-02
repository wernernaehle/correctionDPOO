package edu.uoc.correction.model;

import java.util.List;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class Student {
    private String name;
    private String comment;

    private List<Exercise> exercises;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
}
