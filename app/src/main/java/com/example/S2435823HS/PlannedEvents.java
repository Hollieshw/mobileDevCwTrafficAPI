package com.example.S2435823HS;

/**
 * simple getter setter class for planned events/ types used for unplanned also
 */
public class PlannedEvents {

    private String source;
    private String comment;
    private String type;
    private String roadName;
    private String delayType;
    private double toLatitude;
    private double toLongitude;
    private double fromLatitude;
    private double fromLongitude;


    public String getSource() {
        return source;
    }

    public String getComment() {
        return comment;
    }

    public String getType() {
        return type;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getDelayType() {
        return delayType;
    }

    public double getToLatitude() {
        return toLatitude;
    }

    public double getToLongitude() {
        return toLongitude;
    }

    public double getFromLatitude() {
        return fromLatitude;
    }

    public double getFromLongitude() {
        return fromLongitude;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public void setDelayType(String delayType) {
        this.delayType = delayType;
    }


    public void setToLatitude(double latitude) {
        this.toLatitude = latitude;
    }

    public void setToLongitude(double longitude) {
        this.toLongitude = longitude;
    }

    public void setFromLatitude(double latitude) {
        this.fromLatitude = latitude;
    }

    public void setFromLongitude(double longitude) {
        this.fromLongitude = longitude;
    }
}