package com.liatrio.atlas.plugins.structs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CustomFilterListsJson {
    @XmlElement
    private String filterTitle;

    public CustomFilterListsJson() {
    }

    public CustomFilterListsJson(String filterTitle) {
        this.filterTitle = filterTitle;
    }

    public String getFilterTitle() {
        return filterTitle;
    }

    public void setFilterTitle(String filterTitle) {
        this.filterTitle = filterTitle;
    }

    @Override
    public String toString() {
        return "CustomFilterListsJson[filterTitle=" + filterTitle + "]";
    }
}
