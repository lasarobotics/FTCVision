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

    protected final void initializeVision(int framePreview) {
        openCVCamera = (CameraBridgeViewBase) findViewById(framePreview);
        openCVCamera.setVisibility(SurfaceView.VISIBLE);
    }

    public void onDestroy() {
        super.onDestroy();
        if (openCVCamera != null)
            openCVCamera.disableView();
    }
}
