package edu.uoc.correction.model;

import java.util.List;

/**
 * @author Werner Nähle
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

    public boolean isScoreByMethodsForClass(String className) {
        if (className == null || listTestNote == null) {
            return false;
        }
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        for (TestNote testNote : listTestNote) {
            if (simpleClassName.equals(testNote.getTestName())
                    && testNote.getMethodsNote() != null
                    && !testNote.getMethodsNote().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
