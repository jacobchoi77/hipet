package com.nextpage.hipetdemo.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by jacobsFactory on 2017-11-03.
 */
public class Report {
    private String latitude;
    private String longitude;
    private String petImageUrl;
    private String petOwnerName;
    private String petOwnerPhone;
    private String petOwnerEmail;
    private String petName;
    private String reportDate;

    public Report() {

    }

    public double getLatitude() {
        return Double.valueOf(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return Double.valueOf(longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPetImageUrl() {
        return petImageUrl;
    }

    public void setPetImageUrl(String petImageUrl) {
        this.petImageUrl = petImageUrl;
    }

    public String getPetOwnerName() {
        return petOwnerName;
    }

    public void setPetOwnerName(String petOwnerName) {
        this.petOwnerName = petOwnerName;
    }

    public String getPetOwnerPhone() {
        return petOwnerPhone;
    }

    public void setPetOwnerPhone(String petOwnerPhone) {
        this.petOwnerPhone = petOwnerPhone;
    }

    public String getPetOwnerEmail() {
        return petOwnerEmail;
    }

    public void setPetOwnerEmail(String petOwnerEmail) {
        this.petOwnerEmail = petOwnerEmail;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Date getReportDate() {
        return new Date(Long.valueOf(reportDate));
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public LatLng getLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }
}
