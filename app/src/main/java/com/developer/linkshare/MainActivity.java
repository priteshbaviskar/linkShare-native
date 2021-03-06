package com.developer.linkshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.hardware.Camera.open;


public class MainActivity extends Activity {

    private CardTouchListener mCardTouchListener;
    private ClickListener fabListener;
    private Camera mCamera;
    private CameraView mCameraview;
    private CardView myCardView;
    public static final String TAG = "LinkShareActivity";

    private static final int DEGREES_0 = 0;
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Transition ts = new Explode();
        ts.setStartDelay(2000);
        ts.setDuration(5000);

        getWindow().setEnterTransition(ts);

        setContentView(R.layout.activity_main);

        myCardView = (CardView)findViewById(R.id.card_view);
        myCardView.setPadding(16, 16, 16, 16);

        ImageButton fab = (ImageButton)findViewById(R.id.fab);
        ClickListener fabListener = new ClickListener();
        fab.setOnClickListener(fabListener);

        startCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.release();
        mCamera =null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera =null;
    }

    private void startCamera() {

        if(checkIfDeviceHasCamera(this.getApplicationContext())) {

            mCamera =  getCameraInstance();
            mCameraview = new CameraView(this, mCamera);

            //mCameraview.setVisibility(View.INVISIBLE);
            configureCamera(getResources().getConfiguration());
            myCardView.addView(mCameraview);

        }
    }

    private Camera getCameraInstance() {

        Camera camera = null;
        try {
            //back facing camera by default. User open(camId) for other options.
            camera = open();
        }catch(Exception e) {
            Log.i(TAG, e.toString());
        }

        return camera;

    }

    private boolean checkIfDeviceHasCamera(Context context) {

        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
           return true;
        }
        else {
           return false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class CardTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(final View myCardV, MotionEvent event) {

            int  cx = (myCardView.getLeft() + myCardView.getRight() ) / 2;
            int  cy = (myCardView.getTop() + myCardView.getBottom() ) / 2;

            int finalRadius = myCardView.getWidth();

            Animator anim = ViewAnimationUtils.createCircularReveal(myCardView,cx,cy,0,finalRadius);

            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mCameraview.setVisibility(View.VISIBLE);

                }
            });

            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // take picture here, store temporarily on sdcard
            // process the image and then delete it
            Camera.PictureCallback jpegCallBack = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                    savePicture(data);
                   
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
                    camera.startPreview();
                }
            };

            mCamera.takePicture(null, null, jpegCallBack);

        }


    }

    private void savePicture(byte[] data) {

        File picture = getOutputMediaFile();

        if(picture != null) {
            try {
                FileOutputStream fos = new FileOutputStream(picture);
                fos.write(data);
                fos.close();

            }catch(FileNotFoundException e) {
                Log.i(TAG, "Image file not found." + e.getMessage());
            }catch (IOException e) {
                Log.i(TAG, "Unable to write to file." + e.getMessage());
            }
        }
        else {
            Log.i(TAG, "Something went wrong. Picture was null");
        }
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                        "LinkShareApp");
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                Log.i(TAG,"Failed to create DIR");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;

    }

    @SuppressWarnings("deprecation")
    private boolean configureCamera(Configuration configuration) {

        int width, height, displayOrientationDegrees;
        Camera.Size previewSize;

        List<Camera.Size> supporttedPreviewSizes;
        Display mDisplay = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if(mCamera != null) {
            width = mDisplay.getWidth(); //getScreenWidth();
            height = mDisplay.getHeight(); //getScreenHeight();

            displayOrientationDegrees = getDisplayOrientationDegrees(mDisplay);
            mCamera.setDisplayOrientation(displayOrientationDegrees);

            previewSize = mCamera.getParameters().getPreviewSize();
            supporttedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            Camera.Parameters cameraParams = mCamera.getParameters();
            cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            cameraParams.setPreviewSize(720, 480);

            mCamera.setParameters(cameraParams);

            float aspect = (float) previewSize.width / previewSize.height;

            for(Camera.Size supportedSize : supporttedPreviewSizes) {
                Log.i(MainActivity.TAG, "size: " + supportedSize.width + "x" + supportedSize.height);

            }
            ViewGroup.LayoutParams cameraHolderParams = myCardView.getLayoutParams();

            if(configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                cameraHolderParams.height = 480; //height/2;
                cameraHolderParams.width  = 640; //(int) (height / aspect);
            }
            else {
                cameraHolderParams.height = (int) (width / aspect);
                cameraHolderParams.width = width;
            }

            return true;
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private int getDisplayOrientationDegrees(Display display) {
        int displayOrientationDegrees;
        int orientation = getResources().getConfiguration().orientation;


        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    displayOrientationDegrees = DEGREES_90;
                else displayOrientationDegrees = DEGREES_0;
                break;
            case Surface.ROTATION_90:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    displayOrientationDegrees = DEGREES_0;
                else displayOrientationDegrees = DEGREES_270;
                break;
            case Surface.ROTATION_180:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    displayOrientationDegrees = DEGREES_270;
                else displayOrientationDegrees = DEGREES_180;
                break;
            case Surface.ROTATION_270:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    displayOrientationDegrees = DEGREES_180;
                else displayOrientationDegrees = DEGREES_90;
                break;
            default:
                displayOrientationDegrees = DEGREES_0;
        }

        return displayOrientationDegrees;
    }

}
