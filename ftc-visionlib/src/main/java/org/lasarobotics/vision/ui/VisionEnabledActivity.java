package org.lasarobotics.vision.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

/**
 * Initiates a VisionEnabledActivity
 */
public abstract class VisionEnabledActivity extends Activity {
    public static CameraBridgeViewBase openCVCamera;

    protected final void initializeVision(int layoutID)
    {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        //-1(LayoutParams.MATCH_PARENT) is fill_parent or match_parent since API level 8
        //-2(LayoutParams.WRAP_CONTENT) is wrap_content
        //TODO get width amd height of frame
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                480, 480));
        /*tView = new TextView(this);
        tView.setText("Hello, This is a view created programmatically! " +
                "You CANNOT change me that easily :-)");
        tView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        lLayout.addView(tView);*/

        JavaCameraView cameraView = new JavaCameraView(this, 0);
        cameraView.setVisibility(View.VISIBLE);

        layout.addView(cameraView);
        layout.setVisibility(View.VISIBLE);

        LinearLayout primaryLayout = (LinearLayout) this.findViewById(layoutID);
        primaryLayout.addView(layout);

        openCVCamera = cameraView;
    }

    /*protected final void initializeVision(int framePreview) {
        openCVCamera = (CameraBridgeViewBase) findViewById(framePreview);
        openCVCamera.setVisibility(SurfaceView.VISIBLE);
    }*/

    public void onDestroy() {
        super.onDestroy();
        if (openCVCamera != null)
            openCVCamera.disableView();
    }
}
