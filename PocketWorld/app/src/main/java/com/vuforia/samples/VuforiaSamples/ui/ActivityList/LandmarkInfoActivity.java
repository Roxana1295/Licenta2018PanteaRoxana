package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vuforia.samples.SampleApplication.utils.Coordinates;
import com.vuforia.samples.SampleApplication.utils.DBManager;
import com.vuforia.samples.VuforiaSamples.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by pntro on 10/12/2017.
 */

public class LandmarkInfoActivity extends Activity {

    private static final String LOGTAG = "Landmark Info Activity";
    private final String CODE="BROAD";
    private String wikiURL;
    private String wikiHead="https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro=&explaintext=&titles=";
    private String mLandmarkName;

    private TextView landmarkName;
    private TextView landmarkInfo;
    private ImageView wikiImage;
    private Button findClosest;
    private Button removeButton;
    private DBManager db_man;
    Coordinates coordinates;

    String wikiText=null;
    String imgSource=null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        String wikiTail;
        coordinates=new Coordinates(0.0,0.0);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            mLandmarkName= null;
            wikiTail=null;
        } else {
            mLandmarkName= extras.getString("LANDMARK_NAME");
            wikiTail=extras.getString("WIKI");
        }

        setContentView(R.layout.landmark_info_activity);
        db_man=new DBManager(this);
        landmarkName=(TextView)findViewById(R.id.textView);
        landmarkName.setText(mLandmarkName);

        removeButton=(Button) findViewById(R.id.button_delete);
        removeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                interrogateRemove();
            }



        });

        landmarkInfo=(TextView)findViewById(R.id.textView2);
        wikiImage=(ImageView) findViewById(R.id.wikiImage);
        findClosest=(Button)findViewById(R.id.find_closest_landmarks);

        findClosest.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    findClosestLandmarks();
                }
        });
        wikiURL=wikiHead+wikiTail;

        if (!isNetworkAvailable()){
            Toast.makeText(this, "No network connection",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        //you can't get URL information from the main thred
        new Thread(new Runnable() {
            public void run() {
                try {
                    getWikiInfo(wikiURL);
                } catch (Exception e) {
                    e.printStackTrace();
                    wikiText="No wiki available.";
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                landmarkInfo.setText(wikiText);
                                removeButton.setVisibility(View.VISIBLE);
                                findClosest.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        }).start();
    }
    private void interrogateRemove(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message="Are you sure about removing "+
                mLandmarkName + "? It will cause permanent damage." +
                " Data can't be recovered";
        builder.setMessage(message).setPositiveButton("Yes",dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void removeElement(){

        if (db_man.removeElementByName(mLandmarkName) ){
            Toast.makeText(getBaseContext(), "Landmark " + mLandmarkName + " deleted",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getBaseContext(), "Landmark " + mLandmarkName + " was not deleted",
                    Toast.LENGTH_SHORT).show();
        }
        goToSearch();
    }
    private void goToSearch(){
        Intent i = new Intent(LandmarkInfoActivity.this,ActivityLauncher.class);
        startActivity(i);
        finish();
    }

    private void findClosestLandmarks() {
        Intent i = new Intent(LandmarkInfoActivity.this,ClosestLandmarksActivity.class);
        i.putExtra("CODE",CODE);
        i.putExtra("LANDMARK",mLandmarkName);
        startActivity(i);
        finish();
    }

    public void getWikiInfo(String url) throws IOException, JSONException {

        URL u = new URL(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        u.openStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();

        String finalSb=String.valueOf(sb);

        //must return to main thread in order to set text
        parseWiki(finalSb);


    }
    private void parseWiki(String s) throws JSONException {

        JSONObject searchJson = new JSONObject(s);
        JSONObject query = searchJson.getJSONObject("query");
        JSONObject pages = query.getJSONObject("pages");

        Iterator<String> keys = pages.keys();
        // get some_name_i_wont_know in str_Name
        String pageNo=keys.next();
        // get the value i care about
        JSONObject coreSection = pages.getJSONObject(pageNo);
//        String imgSource;

        try {
            //set description
            final String description = coreSection.getString("extract");
            wikiText = description.toString();

            //set URL image
            JSONObject thumbnail = coreSection.getJSONObject("thumbnail");
            imgSource = (thumbnail.getString("source")).replace("px", "0px");


        }catch (Exception e){
            wikiText="No wiki available.";
            imgSource="";
        }
        final String finalImgSource = imgSource;
        new Thread(new Runnable() {
            public void run() {
                try {
                    setURLImage(finalImgSource);

                } catch (Exception e) {
                    e.printStackTrace();
                }}
        }).start();
    }
    private void setURLImage(String imgSource) {
        URL url = null;
        Bitmap bmp=null;
        try {
            url = new URL(imgSource);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Bitmap finalBmp = bmp;
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    landmarkInfo.setText(wikiText);
                    wikiImage.setImageBitmap(finalBmp);
                    removeButton.setVisibility(View.VISIBLE);
                    findClosest.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    removeElement();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
