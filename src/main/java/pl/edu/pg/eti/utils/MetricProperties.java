package pl.edu.pg.eti.utils;

import org.springframework.stereotype.Service;

public class MetricProperties {

    public MetricProperties(String n, String fn, String d) {
        setName(n);
        setFullname(fn);
        setFullDescription(d);
    }

    private String name;
    private String fullname;
    private String fullDescription;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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