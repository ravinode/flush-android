package com.flush.kira.flush.pojo;

public class LatLongReq {
    Double lat;
    Double log;
    String distance;
    String KmOrMiles;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLog() {
        return log;
    }

    public void setLog(Double log) {
        this.log = log;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getKmOrMiles() {
        return KmOrMiles;
    }

    public void setKmOrMiles(String kmOrMiles) {
        KmOrMiles = kmOrMiles;
    }
}
