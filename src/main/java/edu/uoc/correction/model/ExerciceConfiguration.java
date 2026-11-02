package edu.uoc.correction.model;

import java.util.List;

/**
 * @author Fran Manez
 * @version 1.0
 */
public class ExerciceConfiguration {
    private List<TestNote> listTestNote;
    private boolean scoreByMethods = false;

    public List<TestNote> getListTestNote() {
        return listTestNote;
    }

    public void setListTestNote(List<TestNote> listTestNote) {
        this.listTestNote = listTestNote;
    }

    public boolean isScoreByMethods() {
        return scoreByMethods;
    }

    public void setScoreByMethods(boolean scoreByMethods) {
        this.scoreByMethods = scoreByMethods;
    }
}
