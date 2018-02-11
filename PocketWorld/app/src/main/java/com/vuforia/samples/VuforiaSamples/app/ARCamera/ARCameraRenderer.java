/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ARCamera;

import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.AppRenderer;
import com.vuforia.samples.SampleApplication.ApplicationSession;
import com.vuforia.samples.SampleApplication.AppRendererControl;
import com.vuforia.samples.SampleApplication.utils.DBManager;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.LandmarkInfoActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the ARCamera sample.
public class ARCameraRenderer implements GLSurfaceView.Renderer, AppRendererControl
{
    private static final String LOGTAG = "ARCameraRenderer";
    
    private ApplicationSession vuforiaAppSession;
    private ARCamera mActivity;
    private AppRenderer mSampleAppRenderer;
    private DBManager db_man;

    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;
    private int trackableLimit=50;
    private int trackableTolerance=5;
    private int trackableCounter;
    private int debounceCounter;
    private String lastTrackable=null;

    private float kBuildingScale = 0.012f;

    private boolean mIsActive = false;
    private boolean mModelIsLoaded = false;

    private static final float OBJECT_SCALE_FLOAT = 0.003f;

    
    public ARCameraRenderer(ARCamera activity, ApplicationSession session)
    {

        mActivity = activity;
        vuforiaAppSession = session;
        //initialize counters with 0 every time activity starts
        trackableCounter=0;
        debounceCounter=0;

        // AppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new AppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.01f , 5f);
        db_man=new DBManager(activity);
    }
    

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our function to render content from AppRenderer class
        mSampleAppRenderer.render();
    }


    public void setActive(boolean active)
    {
        mIsActive = active;

        if(mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        initRendering();
    }


    // Function for initializing the renderer.
    private void initRendering()
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");

        if(!mModelIsLoaded) {

            // Hide the Loading Dialog
            mActivity.loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        }

    }

    public void updateConfiguration()
    {
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
    }

    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by AppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix)
    {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);

            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            countUserData(trackable);

            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            if (!mActivity.isExtendedTrackingActive()) {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                        OBJECT_SCALE_FLOAT);
                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                        OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
            } else {
                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                        kBuildingScale, kBuildingScale);
            }
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);


            SampleUtils.checkGLError("Render Frame");

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:\"" + userData + "\"");
    }
    private String brakeToLastDelimiter(String s,String delimiter){
        int p=s.lastIndexOf(delimiter);
        return s.substring(0,p);
    }
    private void countUserData(Trackable trackable){
        String landmark_name=brakeToLastDelimiter((String)trackable.getUserData(),"_");
        if (lastTrackable==null){
            lastTrackable = landmark_name;
            trackableCounter++;
        }
        else{
            if (landmark_name.equals(lastTrackable)){
                trackableCounter++;
            }
            else{
                if(debounce()){
                    lastTrackable=landmark_name;
                    debounceCounter=0;
                }
            }
        }
        if (trackableCounter==trackableLimit){
            trackableCounter=0;
            //clean name and show info about dataset
            cleanAndShowInfo(landmark_name);
        }

    }
    private void cleanAndShowInfo(String s){
        String landmark= capitalizeAll((s.replace("Current Dataset : ","")).replace("_"," "));
        startInfoActivity(landmark);
    }
    public static String capitalizeAll(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            Log.d(LOGTAG,arr[i]);
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
    private boolean debounce() {
        debounceCounter++;
        if (debounceCounter == trackableTolerance) {
            debounceCounter=0;
            return true;
        } else {
            return false;
        }

    }

    private void startInfoActivity(String landmark){
        //start new activity
        Intent intent = new Intent(mActivity, LandmarkInfoActivity.class);
        intent.putExtra("LANDMARK_NAME",landmark);
        intent.putExtra("WIKI",db_man.getWikiByName(landmark));
        mActivity.startActivity(intent);
    }

}
