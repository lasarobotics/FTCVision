package org.lasarobotics.vision.opmode;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.lasarobotics.vision.android.Camera;
import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.android.Sensors;
import org.lasarobotics.vision.ftc.resq.Constants;
import org.lasarobotics.vision.util.FPS;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * Core OpMode class containing most OpenCV functionality
 */
abstract class VisionOpModeCore extends OpMode implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final int initialMaxSize = 1200;
    protected static JavaCameraView openCVCamera;
    private static boolean initialized = false;
    private static boolean openCVInitialized = false;
    protected final BaseLoaderCallback openCVLoaderCallback = new BaseLoaderCallback(hardwareMap.appContext) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    //Woohoo!
                    Log.d("OpenCV", "OpenCV Manager connected!");
                    openCVInitialized = true;
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    public int width, height;
    public FPS fps;
    public Sensors sensors;

    protected boolean isInitialized() {
        return initialized;
    }

    public void setCamera(Cameras camera) {
        if (openCVCamera == null)
            return;
        openCVCamera.disableView();
        if (initialized) openCVCamera.disconnectCamera();
        openCVCamera.setCameraIndex(camera.getID());
        setCameraInfo(camera.getID());
        if (initialized) openCVCamera.connectCamera(width, height);
        openCVCamera.enableView();
    }

    public void setFrameSize(Size frameSize) {
        if (openCVCamera == null)
            return;

        openCVCamera.disableView();
        if (initialized) openCVCamera.disconnectCamera();
        openCVCamera.setMaxFrameSize((int) frameSize.width, (int) frameSize.height);
        if (initialized) openCVCamera.connectCamera((int) frameSize.width, (int) frameSize.height);
        openCVCamera.enableView();

        width = openCVCamera.getFrameWidth();
        height = openCVCamera.getFrameHeight();
    }

    private void setCameraInfo(int cameraID)
    {
        //Prepare camera information
        Camera c = new Camera(cameraID);
        android.hardware.Camera.Parameters pam = c.getCamera().getParameters();
        Constants.CAMERA_HOR_VANGLE = pam.getHorizontalViewAngle() * Math.PI/180.0;
        Constants.CAMERA_VERT_VANGLE = pam.getVerticalViewAngle() * Math.PI/180.0;

        //Release the camera for later use
        c.unlock();
        c.release();
    }

    @Override
    public void init() {
        //Initialize camera view
        final Activity activity = (Activity) hardwareMap.appContext;
        final VisionOpModeCore t = this;

        setCameraInfo(0);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            boolean success = OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, hardwareMap.appContext, openCVLoaderCallback);
            if (!success)
                Log.e("OpenCV", "Asynchronous initialization failed!");
            else
                Log.d("OpenCV", "Asynchronous initialization succeeded!");
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            openCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        while (!openCVInitialized) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);

                layout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                openCVCamera = new JavaCameraView(hardwareMap.appContext, 0);

                layout.addView(openCVCamera);
                layout.setVisibility(View.VISIBLE);

                openCVCamera.setCvCameraViewListener(t);
                if (openCVCamera != null)
                    openCVCamera.disableView();
                openCVCamera.enableView();
                openCVCamera.connectCamera(initialMaxSize, initialMaxSize);

                //Initialize FPS counter and sensors
                fps = new FPS();
                sensors = new Sensors();

                //Done!
                width = openCVCamera.getFrameWidth();
                height = openCVCamera.getFrameHeight();
                initialized = true;
            }
        });

        while (!initialized) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {
        super.stop();

        if (openCVCamera != null)
            openCVCamera.disableView();
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

        telemetry.addData("Vision Status", "Ready!");

        fps.update();
        return frame(inputFrame.rgba(), inputFrame.gray());
    }

    public abstract Mat frame(Mat rgba, Mat gray);
}
