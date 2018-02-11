package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.vuforia.samples.SampleApplication.utils.Coordinates;
import com.vuforia.samples.SampleApplication.utils.DBManager;
import com.vuforia.samples.SampleApplication.utils.DistanceArrayAdapter;
import com.vuforia.samples.SampleApplication.utils.GPS;
import com.vuforia.samples.SampleApplication.utils.IGPSActivity;
import com.vuforia.samples.SampleApplication.utils.Landmark;
import com.vuforia.samples.VuforiaSamples.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by pntro on 11/11/2017.
 */

public class ClosestLandmarksActivity extends Activity implements IGPSActivity{

    private static final String LOGTAG="Closest Landmark Activity";

    private static final String LOCAL_CODE="LOCAL";
    private static final String BROAD_CODE="BROAD";

    private String code;
    private TextView header;
    private ListView listView;

    private Double _lat;
    private Double _lng;
    private String city;

    private String landmarkName;
    private DBManager db_man;
    private Coordinates coordinates;
    private GPS gps;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.closest_landmarks);

        db_man=new DBManager(ClosestLandmarksActivity.this);

        Bundle extras=getIntent().getExtras();
        if (extras!=null){
            code=extras.getString("CODE");
            if (code.equals(BROAD_CODE)){

                landmarkName=extras.getString("LANDMARK");
            }
            else{
                landmarkName="you";
            }
        }

        header= (TextView) findViewById(R.id.header);
        listView=(ListView)findViewById(R.id.closest_landmarks_list);

        if (code.equals(LOCAL_CODE)) {
            gps=new GPS(this,getBaseContext());
        }
        else{
            this.city=db_man.getCityOfLandmark(landmarkName);
            getLandmarkCoordinates();
            getClosestLandmarks();

        }
    }

    private void getLandmarkCoordinates(){
        coordinates=db_man.getLongLatByName(landmarkName);
        _lat=coordinates.getLatitude();
        _lng=coordinates.getLongitude();
    }

    private void getClosestLandmarks(){
        ArrayList<Landmark> landmarks=db_man.getAllLongLatName(this.landmarkName,this.city);

        for (Landmark l:landmarks){
            l.setDistanceThroughPoint(_lat,_lng);
        }

        Collections.sort(landmarks, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark landmark, Landmark t1) {
                Double d1=landmark.getDistanceThroughPoint();
                Double d2=t1.getDistanceThroughPoint();
                return d1.compareTo(d2);
            }
        });
        listClosestLandmarks(landmarks);
    }

    private void listClosestLandmarks(ArrayList<Landmark> landmarks) {
        header.setText("Closest landmarks to \n"+this.landmarkName+"\n from "+this.city);
        final ArrayList<String> landmarkName=new ArrayList<>();
        for (Landmark l:landmarks)
            landmarkName.add(l.getName());

        DistanceArrayAdapter adapter = new DistanceArrayAdapter(this,landmarks);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                String Slecteditem= landmarkName.get(position);
                startInfActivity(Slecteditem);
            }
        });
    }

    private void startInfActivity(String landmark){
        //start new activity
        Intent intent = new Intent(ClosestLandmarksActivity.this, LandmarkInfoActivity.class);
        intent.putExtra("LANDMARK_NAME",landmark);
        intent.putExtra("WIKI",db_man.getWikiByName(landmark));

        this.finish();
        startActivity(intent);
    }


    @Override
    public void locationChanged(double longitude, double latitude) {
        _lat=latitude;
        _lng=longitude;
        city=gps.getCity();
        getClosestLandmarks();
    }
}
