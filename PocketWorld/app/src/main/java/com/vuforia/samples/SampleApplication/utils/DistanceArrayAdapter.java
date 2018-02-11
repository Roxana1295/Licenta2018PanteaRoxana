package com.vuforia.samples.SampleApplication.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;

import java.util.ArrayList;

/**
 * Created by pntro on 2/9/2018.
 */

public class DistanceArrayAdapter extends ArrayAdapter<Landmark>{

    private final Activity context;
    private final ArrayList<Landmark> landmarks;
    private final String IMAGES_LOCATION="/sdcard/PocketWorld_images/";
    private static DBManager db_man;

    public DistanceArrayAdapter( Activity context, ArrayList<Landmark> landmarks) {
        super(context, R.layout.landmark_distance_item, landmarks);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.landmarks=landmarks;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.landmark_distance_item, null,true);
        final Landmark item=landmarks.get(position);
        String img;
        Double dist=item.getDistanceThroughPoint();;

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item_dist);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon_dist);
        TextView distance=(TextView) rowView.findViewById(R.id.distance);

        txtTitle.setText(item.getName());
        distance.setText(String.valueOf(dist.intValue())+ " m");
        img= item.getName().replace(" ", "").toLowerCase();


        int id = this.context.getResources().getIdentifier(img, "drawable", this.context.getPackageName());

        if (id!=0){
            imageView.setImageResource(id);
        }else{
            try{

                Bitmap newBitmap = BitmapFactory.decodeFile(IMAGES_LOCATION+img+".jpg");
                imageView.setImageBitmap(newBitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return rowView;

    }


}
