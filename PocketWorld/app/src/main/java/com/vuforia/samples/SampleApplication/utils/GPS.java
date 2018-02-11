package com.vuforia.samples.SampleApplication.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by pntro on 2/9/2018.
 */

public class GPS {

    private IGPSActivity main;

    // Helper for GPS-Position
    private LocationListener mlocListener;
    private LocationManager mlocManager;

    private boolean isRunning;
    private String city;
    private Double _lat;
    private Double _lng;
    private Context baseContext;
    public GPS(IGPSActivity main,Context baseContext) {
        this.main = main;
        this. baseContext=baseContext;

        checkGPSStatus();

        // GPS Position
        mlocManager = (LocationManager) ((Activity) this.main).getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
        // GPS Position END
        this.isRunning = true;
    }

    public void stopGPS() {
        if(isRunning) {
            mlocManager.removeUpdates(mlocListener);
            this.isRunning = false;
        }
    }

    public void resumeGPS() {
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        this.isRunning = true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private void getCurrentCity(){
        Geocoder gcd = new Geocoder((Activity) this.main, Locale.getDefault());
        List<Address> addresses = null;
        try {

            addresses = gcd.getFromLocation(_lat, _lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            city = addresses.get(0).getLocality();
        }
    }

    public String getCity(){
        return this.city;
    }
    private void checkGPSStatus(){
        boolean GPS_enabled=getGPSStatus();
        if (!GPS_enabled) {
            Toast.makeText((Activity)this.main, "GPS not enabled. Please try again!", Toast.LENGTH_SHORT).show();
            ((Activity) this.main).finish();
        }
    }

    /*----Method to Check GPS is enable or disable ----- */
    public Boolean getGPSStatus() {
        ContentResolver contentResolver =this.baseContext
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }


    public class MyLocationListener implements LocationListener {

        private final String TAG = MyLocationListener.class.getSimpleName();

        @Override
        public void onLocationChanged(Location loc) {
//
            _lat=loc.getLatitude();
            _lng=loc.getLongitude();
//
            if(_lat!=null && _lng!=null) {
                getCurrentCity();
                GPS.this.main.locationChanged(loc.getLongitude(), loc.getLatitude());
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
//            GPS.this.main.displayGPSSettingsDialog();
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }


    }

}