package com.liatrio.atlas.plugins.structs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FilterPair {
    @XmlElement
    private String label;

    @XmlElement
    private String value;

    public FilterPair() {
    }

    public FilterPair(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FilterPair[label=" + label + ", value=" + value + "]";
    }
}
