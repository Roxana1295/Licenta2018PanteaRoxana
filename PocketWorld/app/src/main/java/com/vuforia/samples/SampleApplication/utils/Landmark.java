package com.vuforia.samples.SampleApplication.utils;

/**
 * Created by pntro on 10/15/2017.
 */

public class Landmark{
    private Double latitude;
    private Double longitude;
    private String name;
    private Double distanceThroughPoint;

    public Landmark(Double latitude, Double longitude, String name){
        this.latitude=latitude;
        this.longitude=longitude;
        this.name=name;
    }
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Double getDistanceThroughPoint() {
        return distanceThroughPoint;
    }

    public void setDistanceThroughPoint(Double distanceThroughPoint) {
        this.distanceThroughPoint = distanceThroughPoint;
    }

    public void setDistanceThroughPoint(double latitude,double longitude){
        this.distanceThroughPoint=this.getDistanceBetween2Points(latitude,longitude,this.latitude,this.longitude);
    }
    private double getDistanceBetween2Points(double latitude1,double longitude1,double latitude2,double longitude2){

        final int R = 6371; // Radius of the earthvate

        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to meters
        distance=distance*1000;
        return distance;
    }

}
