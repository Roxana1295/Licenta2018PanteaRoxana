package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.vuforia.samples.SampleApplication.utils.DBManager;
import com.vuforia.samples.SampleApplication.utils.ImageArrayAdapter;
import com.vuforia.samples.VuforiaSamples.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by pntro on 10/15/2017.
 */

public class SearchLandmarkActivity extends Activity{

    private static final String LOGTAG = "Srch Landmark Activity";
    private ListView listView;
    private  DBManager db_man;
    private ArrayList<String> landmarks_names;
    private ArrayList<String> displayed_landmarks_names;
    private SearchView searchView;
    private ImageArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.search_landmark_activity);
        db_man=new DBManager(SearchLandmarkActivity.this);
        getLandMarksNames();

        searchView=(SearchView) findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println(searchView.getQuery());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text= String.valueOf( searchView.getQuery());
                updateLandmarksList(text);
                return true;
            }

    });

    listView=(ListView)findViewById(R.id.landmarks_list);
    displayList();

    }
    private void displayList(){
        adapter = new ImageArrayAdapter(this,displayed_landmarks_names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                String Slecteditem= displayed_landmarks_names.get(position);
                startInfoActivity(Slecteditem);
            }
        });
    }
    private void updateLandmarksList(String text) {
        ArrayList<String> modifLandmarksNames=new ArrayList<>();

        for (String landmark:landmarks_names){
            if (landmark.toLowerCase().contains(text.toLowerCase())){
                modifLandmarksNames.add(landmark);
            }
        }
        displayed_landmarks_names=modifLandmarksNames;
        displayList();

    }

    private void getLandMarksNames(){
        try {
            db_man.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        landmarks_names=db_man.getNames();
        Collections.sort(landmarks_names, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        displayed_landmarks_names=landmarks_names;

//        for(String d:displayed_landmarks_names){
//            System.out.println(d);
//        }
        db_man.close();
    }
    private void startInfoActivity(String landmark){
        //start new activity
        Intent intent = new Intent(this, LandmarkInfoActivity.class);
        intent.putExtra("LANDMARK_NAME",landmark);
        intent.putExtra("WIKI",db_man.getWikiByName(landmark));


        this.startActivity(intent);
        finish();
    }


}
