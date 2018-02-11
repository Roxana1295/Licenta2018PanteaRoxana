package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vuforia.samples.SampleApplication.utils.DBManager;
import com.vuforia.samples.SampleApplication.utils.GPS;
import com.vuforia.samples.SampleApplication.utils.IGPSActivity;
import com.vuforia.samples.VuforiaSamples.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pntro on 1/31/2018.
 */

public class AddItemActivity extends Activity implements IGPSActivity{

    public static final int PICK_IMAGE = 1;
    private Button button_load_image;
    private Button button_check_data;
    private Button button_save_item;

    private ImageView imageView;

    private AutoCompleteTextView landmarkName;
    private AutoCompleteTextView landmarkWiki;
    private AutoCompleteTextView landmarkCity;
    private AutoCompleteTextView landmarkLatitude;
    private AutoCompleteTextView landmarkLongitude;
    DBManager db_man;

    private TextView data;

    private String _DBname;
    private String _DBwiki;
    private String _DBCity;
    private Double _DBlat;
    private Double _DBlng;

    private final String IMAGES_LOCATION="/sdcard/PocketWorld_images";


    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;

    private GPS gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.add_item_layout);
        db_man=new DBManager(AddItemActivity.this);

        _DBlat=0.0;
        _DBlng=0.0;
        _DBCity="";
        _DBname="";
        _DBwiki="";

        imageView = (ImageView) findViewById(R.id.image_preview);

        landmarkName=(AutoCompleteTextView)findViewById(R.id.landmark_name);
        landmarkWiki=(AutoCompleteTextView)findViewById(R.id.wiki_link);
        landmarkCity=(AutoCompleteTextView)findViewById(R.id.city_name);
        landmarkLatitude=(AutoCompleteTextView)findViewById(R.id.latitude_value);
        landmarkLongitude=(AutoCompleteTextView)findViewById(R.id.longitude_value);

        data=(TextView)findViewById(R.id.infos_confirmed);

        button_load_image=(Button)findViewById(R.id.load_image) ;
        button_load_image.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        button_check_data=(Button)findViewById(R.id.button_check_data);
        button_check_data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showDataToBeSaved();
            }
        });

        button_save_item=(Button)findViewById(R.id.button_save_item);
        button_save_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItemInDatabase();
            }
        });
    }

    private void saveItemInDatabase() {
        try {
            db_man.insertLandmark(_DBname, _DBCity, _DBwiki, _DBlat, _DBlng);
            Toast.makeText(this, "Landmark inserted",
                    Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Something went wrong",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        goToActivityLauncher();
    }

    private void goToActivityLauncher() {
        Intent intent=new Intent(AddItemActivity.this,ActivityLauncher.class);
        startActivity(intent);
        finish();
    }

    private void showDataToBeSaved() {

        String[] wiki_link=landmarkWiki.getText().toString().split("[\\/\\\\]");
        _DBwiki=wiki_link[wiki_link.length-1];
        _DBname=landmarkName.getText().toString();

        RadioButton ins=(RadioButton)findViewById(R.id.insert_location);
        if (ins.isChecked()) {
            _DBCity = landmarkCity.getText().toString();
            try {
                _DBlat = Double.valueOf(landmarkLatitude.getText().toString());
                _DBlng = Double.valueOf(landmarkLongitude.getText().toString());
            } catch (Exception e) {
                Toast.makeText(this, "Insert valid values for latitude and longitude",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        String infos="Name: "+_DBname+
                     "\nCity: "+_DBCity+
                     "\nLatitude: "+_DBlat.toString()+
                     "\nLongitude: "+_DBlng.toString()+
                     "\nWikiTail: "+_DBwiki;
        data.setText(infos);
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.insert_location:
                if (checked) {
                    landmarkCity.setEnabled(true);
                    landmarkLatitude.setEnabled(true);
                    landmarkLongitude.setEnabled(true);

                    break;
                }
            case R.id.current_location:
                if (checked) {
                    landmarkCity.setEnabled(false);
                    landmarkLatitude.setEnabled(false);
                    landmarkLongitude.setEnabled(false);
                    gps=new GPS(this, getBaseContext());
                    break;
                }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {

                _DBname=landmarkName.getText().toString();
                if (_DBname.compareTo("")==0){
                    Toast.makeText(getBaseContext(),"Landmark name missing. Please complete the empty fields.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageView.setImageBitmap(bitmap);

                    File myDir=new File(IMAGES_LOCATION);
                    myDir.mkdirs();
                    String img= _DBname.replace(" ", "").toLowerCase();
                    String fname = img+".jpg";
                    File file = new File (myDir, fname);
                    if (file.exists ()) file.delete ();
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void selectImage(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void locationChanged(double longitude, double latitude) {
        _DBlat=latitude;
        _DBlng=longitude;
        _DBCity=gps.getCity();
    }


}
