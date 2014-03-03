package com.liatrio.atlas.plugins.structs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CustomFilterListsJson {
    @XmlElement
    private String html;

    @XmlElement
    private String customTitle;

    public CustomFilterListsJson() {
    }

    public CustomFilterListsJson(String html, String customTitle) {
        this.html = html;
        this.customTitle = customTitle;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public String getHtml() {
        return html;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public String toString() {
        return "CustomFilterListsJson[html=" + html + ", customTitle=" + customTitle + "]";
    }
}
