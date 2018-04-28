package com.nextpage.hipetdemo.model;

import java.util.List;

/**
 * Created by jacobsFactory on 2017-11-03.
 */
public class SearchResult {
    private String areaName;
    private List<Report> reports;

    public SearchResult() {
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
}
