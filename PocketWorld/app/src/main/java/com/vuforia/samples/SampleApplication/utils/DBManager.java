package com.vuforia.samples.SampleApplication.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by pntro on 10/15/2017.
 */

public class DBManager extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION=1;
    private static final String DB_NAME= "PocketWorld.db";
    private static String DB_PATH ;
    private static final String TABLE_LANDMARKS="Landmarks";

    private static final String KEY_NAME="Name";
    private static final String KEY_WIKI="Wiki_site";
    private static final String KEY_LATITUDE="Latitude";
    private static final String KEY_LONGITUDE="Longitude";
    private static final String KEY_CITY="City";

    private static boolean FLAG=false;
    private Context mycontext;
    private static SQLiteDatabase db;
    private static String LOGSOUT="DBManager";

    public DBManager(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.mycontext=context;
        DB_PATH="data/data/"+mycontext.getPackageName()+"/databases/";
        Log.d(LOGSOUT,DB_PATH);
    }

    public void createDataBase() throws IOException{
        boolean dbExist=checkDataBase();
        if (!FLAG) {
            copyDataBase();
            FLAG = true;
        }

        if (dbExist){
            this.getReadableDatabase();
        }
    }

    private boolean checkDataBase(){
        SQLiteDatabase checkDB=null;
        try{
            String myPath=DB_PATH+DB_NAME;
            checkDB=SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){

        }
        if (checkDB!=null){
            checkDB.close();
        }

        return checkDB!=null?true:false;
    }

    private void copyDataBase()throws IOException{
        InputStream is=mycontext.getAssets().open(DB_NAME);
        String outFileName=DB_PATH+DB_NAME;
        OutputStream myOutput=new FileOutputStream(outFileName);

        byte[] buffer=new byte[10];
        int length;
        while((length=is.read(buffer))>0){
            myOutput.write(buffer,0,length);
        }
        myOutput.flush();
        myOutput.close();
        is.close();
    }

    public void openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close(){
        if(db!=null)
            db.close();
        super.close();
    }

    public ArrayList<String> getNames() {
        SQLiteDatabase read_db=this.getReadableDatabase();
        ArrayList<String> landmark_names=new ArrayList<String>();
        String s;
        Cursor cursor=read_db.rawQuery("SELECT "+ KEY_NAME +" FROM "+TABLE_LANDMARKS,null);
        if (cursor.moveToFirst()){
            do{
                s=cursor.getString(cursor.getColumnIndex(KEY_NAME));
                landmark_names.add(s);
            }while (cursor.moveToNext());
        }
        cursor.close();
//        db.close();
        return landmark_names;
    }
    public void insertLandmark(String name, String city, String wiki, Double latitude,Double longitude){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        System.out.println(name);
        values.put(KEY_NAME, name);
        values.put(KEY_CITY, city);
        values.put(KEY_WIKI, wiki);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);

        db.insert(TABLE_LANDMARKS, null, values);
    }
    public String getWikiByName(String name){
        SQLiteDatabase read_db=this.getReadableDatabase();
        String command="SELECT " +KEY_WIKI+" FROM "+ TABLE_LANDMARKS +" WHERE "+KEY_NAME+"=\""+name+"\"";
        Cursor cursor=read_db.rawQuery(command,null);

        String s;

        if (cursor.moveToFirst()) {
            s=cursor.getString(0);
        }
        else
            s= " ";
        cursor.close();
        return s;
    }
    public Coordinates getLongLatByName(String name){
        SQLiteDatabase read_db=this.getReadableDatabase();
        String command="SELECT " +KEY_LATITUDE+" , "+KEY_LONGITUDE+" FROM "+ TABLE_LANDMARKS +" WHERE "+KEY_NAME+"=\""+name+"\"";
        Cursor cursor=read_db.rawQuery(command,null);

        Double lat;
        Double lng;

        if(cursor.moveToFirst()){
            lat=cursor.getDouble(0);
            lng=cursor.getDouble(1);
        }
        else {
            lat = 0.0;
            lng=0.0;
        }
        cursor.close();

        Coordinates c=new Coordinates(lat,lng);
        return c;
    }

    public ArrayList<Landmark> getAllLongLatName(String landmarkName,String city){
        ArrayList<Landmark> allCoordinates=new ArrayList<>();
        SQLiteDatabase read_db=this.getReadableDatabase();
        String command="SELECT "+KEY_LATITUDE+" , "+KEY_LONGITUDE+ " , "+KEY_NAME+" FROM "+ TABLE_LANDMARKS+
                " WHERE "+KEY_CITY+"=\""+city+"\""+
                " AND "+KEY_NAME+"!=\""+landmarkName+"\"";
        Cursor cursor=read_db.rawQuery(command,null);

        Double lat;
        Double lng;
        String name;


        if (cursor.moveToFirst()){
            do{
                lat =cursor.getDouble(0);
                lng=cursor.getDouble(1);
                name=cursor.getString(2);
                allCoordinates.add(new Landmark(lat,lng,name));
            }while (cursor.moveToNext());
        }
        cursor.close();

        return allCoordinates;
    }

    public String getCityOfLandmark(String name){
        SQLiteDatabase read_db=this.getReadableDatabase();
        String command="SELECT "+KEY_CITY+" FROM "+ TABLE_LANDMARKS+" WHERE "+KEY_NAME+"=\""+name+"\"";
        Cursor cursor=read_db.rawQuery(command,null);

        String city="";
        if(cursor.moveToFirst()){
            city=cursor.getString(0);
        }
        return city;
    }
    public boolean removeElementByName(String name) {
        SQLiteDatabase read_db=this.getReadableDatabase();

        return read_db.delete(TABLE_LANDMARKS,KEY_NAME+" = \""+name+"\"",null)>0;

    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
