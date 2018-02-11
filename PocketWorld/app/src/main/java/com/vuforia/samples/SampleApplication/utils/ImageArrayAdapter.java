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
 * Created by pntro on 10/15/2017.
 */

public class ImageArrayAdapter extends ArrayAdapter<String> {

        private final Activity context;
        private final ArrayList<String> itemname;
        private final String IMAGES_LOCATION="/sdcard/PocketWorld_images/";
        private static DBManager db_man;

        public ImageArrayAdapter( Activity context, ArrayList<String> itemname) {
            super(context, R.layout.landmark_item, itemname);
            // TODO Auto-generated constructor stub

            this.context=context;
            this.itemname=itemname;
            db_man= new DBManager(context);
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.landmark_item, null,true);
            final String item=itemname.get(position);
            String img;
            TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

            txtTitle.setText(item);
            img= item.replace(" ", "").toLowerCase();



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
