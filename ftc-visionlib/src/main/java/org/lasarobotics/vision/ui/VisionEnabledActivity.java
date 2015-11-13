package org.lasarobotics.vision.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

/**
 * Initiates a VisionEnabledActivity
 */
public abstract class VisionEnabledActivity extends Activity {
    public static CameraBridgeViewBase openCVCamera;

    protected final void initializeVision(int framePreview, boolean visible) {
        openCVCamera = (CameraBridgeViewBase) findViewById(framePreview);
        //DEBUG this is for debug purposes, you can comment out this line (though it is really nice)
        if (visible)
            openCVCamera.setVisibility(SurfaceView.VISIBLE);
        else
            openCVCamera.setVisibility(SurfaceView.INVISIBLE);
    }

    public void onDestroy() {
        super.onDestroy();
        if (openCVCamera != null)
            openCVCamera.disableView();
    }
}
