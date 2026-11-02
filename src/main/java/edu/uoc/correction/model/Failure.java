package edu.uoc.correction.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Fran Manez
 * @version 1.0
 */
@XmlRootElement(name = "failure")
@XmlAccessorType(XmlAccessType.FIELD)
public class Failure implements Serializable {

    @XmlAttribute
    private String message;

    @XmlAttribute
    private String type;

    public Failure() {
        super();
    }

    public Failure(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Failure [message=" + message + ", type=" + type + "]";
    }
}
