package pl.edu.pg.eti.utils;

import org.springframework.stereotype.Service;

public class MetricProperties {

    public MetricProperties(String n, String d) {
        setFullname(n);
        setFullDescription(d);
    }

    private String fullname;
    private String fullDescription;

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }
}