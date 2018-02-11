/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.app.ARCamera.ARCamera;


// This activity starts activities which demonstrate the Vuforia features
public class ActivityLauncher extends Activity
{
    private static final String LOGTAG = "Activity Launcher";
    private final String CODE="LOCAL";
    private Button button_scan;
    private Button button_search;
    private Button button_proximity;
    private Button button_add;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_launcher_layout);

        button_scan=(Button) findViewById(R.id.button_scan);
        button_search=(Button) findViewById(R.id.button_search);
        button_proximity=(Button)findViewById(R.id.button_find_near);
        button_add=(Button)findViewById(R.id.button_add_item) ;

        button_scan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startARActivity();
            }
        });

        button_search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startSearch();
            }
        });

        button_proximity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                findClosestLandmarks();
            }
        });

        button_add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addNewItem();
            }
        });

    }
    private void startARActivity()
    {
        Intent i = new Intent(ActivityLauncher.this,ARCamera.class);
        startActivity(i);
    }

    private void startSearch(){
        Intent i = new Intent(ActivityLauncher.this,SearchLandmarkActivity.class);
        startActivity(i);
    }

    private void findClosestLandmarks(){
        Intent i = new Intent(ActivityLauncher.this,ClosestLandmarksActivity.class);
        i.putExtra("CODE",CODE);
        i.putExtra("LANDMARK","");
        startActivity(i);
    }

    private void addNewItem(){
        Intent i=new Intent(ActivityLauncher.this, AddItemActivity.class);
        startActivity(i);

    }


}
