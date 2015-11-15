package org.lasarobotics.vision.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import org.jetbrains.annotations.Nullable;
import org.lasarobotics.vision.android.Camera;
import org.lasarobotics.vision.android.Cameras;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Size;

/**
 * Initiates a VisionEnabledActivity
 */
public abstract class VisionEnabledActivity extends Activity {
    public static CameraBridgeViewBase openCVCamera;

    /**
     * Initialize vision
     * Call this method from the onCreate() method after the super()
     * @param layoutID The ID of the primary layout (e.g. R.id.layout_robotcontroller)
     * @param camera The camera to use in vision processing
     * @param frameSize The target frame size. If null, it will get the optimal size.
     */
    protected final void initializeVision(int layoutID, Cameras camera, Size frameSize)
    {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setLayoutParams(new LinearLayout.LayoutParams(
                (int) frameSize.width, (int) frameSize.height));

        JavaCameraView cameraView = new JavaCameraView(this, camera.getID());
        cameraView.setVisibility(View.VISIBLE);

        layout.addView(cameraView);
        layout.setVisibility(View.VISIBLE);

        openCVCamera = cameraView;

        LinearLayout primaryLayout = (LinearLayout) this.findViewById(layoutID);
        primaryLayout.addView(layout);
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
