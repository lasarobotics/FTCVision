package com.lasarobotics.tests.camera;

import android.os.Bundle;
import android.view.WindowManager;

import org.lasarobotics.vision.opmode.VisionEnabledActivity;

public class CameraTestActivity extends VisionEnabledActivity {

    public CameraTestActivity() {
        super();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_cameratest);

        initializeVision(R.id.surfaceView, new CameraTestVisionOpMode());
    }
}