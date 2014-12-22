package com.developer.linkshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;

import static android.hardware.Camera.*;


public class MainActivity extends Activity {

    private CardTouchListener mCardTouchListener;
    private Camera mCamera;
    private CameraView mCameraview;
    private CardView myCardView;
    public static final String TAG = "LinkShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition ts = new Explode();
        ts.setStartDelay(2000);
        ts.setDuration(5000);

        getWindow().setEnterTransition(ts);

        setContentView(R.layout.activity_main);

        myCardView = (CardView)findViewById(R.id.card_view);
        mCardTouchListener = new CardTouchListener();
        myCardView.setOnTouchListener(mCardTouchListener);

    }

    private void startCamera() {


        if(checkIfDeviceHasCamera(this.getApplicationContext())) {

            mCamera =  getCameraInstance();
            mCameraview = new CameraView(this, mCamera);

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
        public boolean onTouch(final View myCardView, MotionEvent event) {

            int  cx = (myCardView.getLeft() + myCardView.getRight() ) / 2;
            int  cy = (myCardView.getTop() + myCardView.getBottom() ) / 2;

            int finalRadius = myCardView.getWidth();

            Animator anim = ViewAnimationUtils.createCircularReveal(myCardView,cx,cy,0,finalRadius);

            anim.start();
            myCardView.setBackgroundColor(Color.RED);
            anim.addListener(new AnimatorListenerAdapter() {


                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startCamera();
                }
            });

            return false;
        }
    }




}
