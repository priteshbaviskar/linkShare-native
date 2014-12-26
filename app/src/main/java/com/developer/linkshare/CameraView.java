package com.developer.linkshare;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by pritesh on 12/22/14.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int DEGREES_0 = 0;
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Display mDisplay;


    @SuppressWarnings("deprecation")
    public CameraView(Context context, Camera camera) {
        super(context);
        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mCamera = camera;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setKeepScreenOn(true);

       // configureCamera(getResources().getConfiguration());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch(IOException e) {
            Log.i(MainActivity.TAG, "Error setting camera preview" + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if(mSurfaceHolder.getSurface() ==null) {
            return;
        }

        try {
            mCamera.stopPreview();
        }catch (Exception e) {

            Log.i(MainActivity.TAG, e.getMessage());
        }

        try {
            mCamera.reconnect();
            mCamera.setPreviewDisplay(mSurfaceHolder);

            mCamera.startPreview();
        }catch (Exception e) {
            Log.i(MainActivity.TAG,"error starting camera preview" + e.getMessage());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }

    @SuppressWarnings("deprecation")
    private boolean configureCamera(Configuration configuration) {

        int width, height, displayOrientationDegrees;
        Camera.Size previewSize;
        if(mCamera != null) {
            width = mDisplay.getWidth(); //getScreenWidth();
            height = mDisplay.getHeight(); //getScreenHeight();

            displayOrientationDegrees = getDisplayOrientationDegrees(mDisplay);
            mCamera.setDisplayOrientation(displayOrientationDegrees);

            previewSize = mCamera.getParameters().getPreviewSize();
            float aspect = (float) previewSize.width / previewSize.height;

            ViewGroup.LayoutParams cameraHolderParams = getLayoutParams();
            if(configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                cameraHolderParams.height = height;
                cameraHolderParams.width  = (int) (height / aspect);
            }
            else {
                cameraHolderParams.height = (int) (width / aspect);
                cameraHolderParams.width = width;
            }


            setLayoutParams(cameraHolderParams);

            return true;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private int getScreenWidth() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2)
            return mDisplay.getWidth();
        else {
            Point size = new Point();
            mDisplay.getSize(size);
            return size.x;
        }
    }

    @SuppressWarnings("deprecation")
    private int getScreenHeight() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2)
            return mDisplay.getHeight();
        else {
            Point size = new Point();
            mDisplay.getSize(size);
            return size.y;
        }
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
