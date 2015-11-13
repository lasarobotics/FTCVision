package org.lasarobotics.vision.ui;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.util.FPS;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public abstract class VisionOpMode extends OpMode implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase openCVCamera;
    protected int width, height;
    private boolean initialized = false;

    protected FPS fps;

    @Override
    public final void init() {
        //Initialize camera view
        openCVCamera = VisionEnabledActivity.openCVCamera;
        openCVCamera.setCameraIndex(0); //SET BACK (MAIN) CAMERA
        openCVCamera.setCvCameraViewListener(this);
        if (openCVCamera != null)
            openCVCamera.disableView();
        openCVCamera.enableView();

        //Initialize FPS counter
        fps = new FPS();

        //Done!
        init(width, height);
        initialized = true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (!initialized) {
            return inputFrame.rgba();
        }

        fps.update();
        return frame(inputFrame.rgba(), inputFrame.gray());
    }

    public abstract void init(int width, int height);
    @Override
    public abstract void loop();
    public abstract Mat frame(Mat rgba, Mat gray);
}
