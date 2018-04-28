package com.nextpage.hipetdemo.beacon;

/**
 * Created by jacobsFactory on 2017-11-16.
 */

public class BeaconData {

    private int minor;
    private double limit;

    public BeaconData(int minor, int limit) {
        this.minor = minor;
        this.limit = limit;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }
}
