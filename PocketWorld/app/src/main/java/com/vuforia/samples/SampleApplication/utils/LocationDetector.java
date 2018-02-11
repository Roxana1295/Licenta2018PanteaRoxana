package com.vuforia.samples.SampleApplication.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by pntro on 11/11/2017.
 */

public class LocationDetector implements LocationListener {
    @Override
    public void onLocationChanged(Location loc) {
//        latitude.setText(String.valueOf(loc.getLatitude()));
//        longitude.setText(String.valueOf(loc.getLongitude()));
//
//        Toast.makeText(getBaseContext(), "Location changed : Lat: " +
//                        loc.getLatitude() + " Lng: " + loc.getLongitude(),
//                Toast.LENGTH_SHORT).show();
//        Log.v(LOGTAG, String.valueOf(loc.getLatitude()));
//        Log.v(LOGTAG, String.valueOf(loc.getLongitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}