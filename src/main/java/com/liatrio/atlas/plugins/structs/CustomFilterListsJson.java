package com.liatrio.atlas.plugins.structs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CustomFilterListsJson {
    @XmlElement
    private String filterTitle;

    @XmlElement
    private String customTitle;

    public CustomFilterListsJson() {
    }

    public CustomFilterListsJson(String filterTitle, String customTitle) {
        this.filterTitle = filterTitle;
        this.customTitle = customTitle;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public String getFilterTitle() {
        return filterTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public void setFilterTitle(String filterTitle) {
        this.filterTitle = filterTitle;
    }

    @Override
    public String toString() {
        return "CustomFilterListsJson [filterTitle=" + filterTitle + ", customTitle=" + customTitle + "]";
    }
}
