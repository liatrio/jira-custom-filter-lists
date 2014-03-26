package com.liatrio.atlas.plugins.structs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FilterPairList {
    @XmlElement
    private List<FilterPair> filter;

    public FilterPairList() {
        this.filter = new ArrayList<FilterPair>();
    }

    public FilterPairList(List<FilterPair> filter) {
        this.filter = filter;
    }

    public void addFilterPair(FilterPair fp) {
        this.filter.add(fp);
    }

    public List<FilterPair> getFilter() {
        return filter;
    }

    public void setFilter(List<FilterPair> filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "FilterPairList[filter=" + filter + "]";
    }
}
